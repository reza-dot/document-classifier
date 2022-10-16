package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.classification.Training;
import de.reza.documentclassifier.pdfutils.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.utils.OcrProcessor;
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

    DatasetProcessor datasetProcessor;
    Classifier classifier;
    Training trainer;
    OcrProcessor ocrProcessor;
    PdfProcessor pdfProcessor;
    JsonProcessor jsonProcessor;
    
    public Controller(DatasetProcessor datasetProcessor, Classifier classifier, Training trainer, OcrProcessor ocrProcessor, PdfProcessor pdfProcessor, JsonProcessor jsonProcessor){
        this.datasetProcessor = datasetProcessor;
        this.classifier = classifier;
        this.trainer = trainer;
        this.ocrProcessor = ocrProcessor;
        this.pdfProcessor = pdfProcessor;
        this.jsonProcessor = jsonProcessor;
    }


    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")@NonNull MultipartFile file) {

        String uuid = UUID.randomUUID().toString();
        String pathToTrainingfiles = datasetProcessor.unzip(file, uuid);
        trainer.startTraining(pathToTrainingfiles, uuid);
        return "Your trained model " + uuid;
    }

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
            if (!ocrProcessor.isReadable(document)) {
                List<Token> tokenSetOcr = ocrProcessor.doOcr(document);
                allClasses.forEach((classname, tokenSetClass) -> predictionList.add(classifier.predict(tokenSetOcr, classname, tokenSetClass, false)));
            } else {
                List<Token> tokenSetPdf = pdfProcessor.getTokensFromPdf(document);
                allClasses.forEach((classname, tokenSetClass) -> predictionList.add(classifier.predict(tokenSetPdf, classname, tokenSetClass, true)));
            }
            document.close();
            log.info("computing time = {} milliseconds", (System.currentTimeMillis() - start));
            return predictionList;
        } catch (IOException e) {
            log.error("Not supported filetype or no file provided");
            return null;
        }
    }
}
