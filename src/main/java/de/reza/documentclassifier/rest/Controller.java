package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.classification.Training;
import de.reza.documentclassifier.pdf.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.utils.JsonProcessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@Slf4j
public class Controller {

    private final DatasetProcessor datasetProcessor;
    private final Classifier classifier;
    private final Training trainer;
    private final PdfProcessor pdfProcessor;
    private final JsonProcessor jsonProcessor;
    
    public Controller(DatasetProcessor datasetProcessor, Classifier classifier, Training trainer, PdfProcessor pdfProcessor, JsonProcessor jsonProcessor){
        this.datasetProcessor = datasetProcessor;
        this.classifier = classifier;
        this.trainer = trainer;
        this.pdfProcessor = pdfProcessor;
        this.jsonProcessor = jsonProcessor;
    }

    /**
     *  API Endpoint for training a model
     * @param file  ZIP file, which includes the pdf documents for training
     * @return      uuid of the trained model
     */
    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")@NonNull MultipartFile file) {

        String uuid = UUID.randomUUID().toString();
        return Optional.ofNullable(datasetProcessor.unzip(file, uuid)).map(
                dataset -> {
                    trainer.startTraining(dataset, uuid);
                    return uuid;
                }
        ).orElse("Please provide a valid dataset");
    }

    /**
     * API endpoint for classifying a given document
     * @param uuid     uuid of a trained model
     * @param file     PDF document which should be classified
     * @return         {@link List<Prediction>}
     */
    @GetMapping("/api/predict/{uuid}")
    public List<Prediction> predict(@PathVariable("uuid") @NonNull String uuid, @RequestParam("document") @NonNull MultipartFile file)  {

        try
        {
            long start = System.currentTimeMillis();
            PDDocument document = PDDocument.load(file.getInputStream());
            Optional<File[]> files = Optional.ofNullable(new File("models/" + uuid).listFiles());
            Map<String, List<Token>> allClasses = new HashMap<>();
            files.ifPresent(jsonFiles -> Arrays.stream(jsonFiles).toList().forEach(jsonFile -> allClasses.put(jsonFile.getName(), jsonProcessor.readJsonFile(jsonFile))));

            List<Prediction> predictionList = new ArrayList<>();
            if (pdfProcessor.isSearchable(document)) {
                List<Token> tokenList = pdfProcessor.getTokensFromSearchablePdf(document);
                allClasses.forEach((classname, tokenSetClass) -> predictionList.add(classifier.predict(tokenList, classname, tokenSetClass, true)));
            } else {
                List<Token> tokenListOcr = pdfProcessor.getTokensFromPdfWithOcr(document);
                allClasses.forEach((classname, tokenSetClass) -> predictionList.add(classifier.predict(tokenListOcr, classname, tokenSetClass, false)));
            }

            document.close();
            log.info("computing time = {} milliseconds", (System.currentTimeMillis() - start));
            return predictionList;
        } catch (IOException e) {
            throw new RuntimeException("Not supported filetype or no file provided");
        }
    }
}
