package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.classification.Training;
import de.reza.documentclassifier.pdfutils.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.ocrutils.OcrProcessor;
import de.reza.documentclassifier.utils.XmlProcessor;
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
    XmlProcessor xmlProcessor;
    OcrProcessor ocrProcessor;

    PdfProcessor pdfProcessor;
    
    public Controller(DatasetProcessor datasetProcessor, Classifier classifier, Training trainer, XmlProcessor xmlProcessor, OcrProcessor ocrProcessor, PdfProcessor pdfProcessor){
        this.datasetProcessor = datasetProcessor;
        this.classifier = classifier;
        this.trainer = trainer;
        this.xmlProcessor = xmlProcessor;
        this.ocrProcessor = ocrProcessor;
        this.pdfProcessor = pdfProcessor;
    }


    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")@NonNull MultipartFile file) throws IOException {

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
            Map<String, HashSet<Token>> allClasses = new HashMap<>();
            files.ifPresent(xmlFiles -> Arrays.stream(xmlFiles).toList().forEach(xmlFile -> {
                allClasses.put(xmlFile.getName(), xmlProcessor.readXmlFile(xmlFile));

            }));
            List<Prediction> predictions = new ArrayList<>();
            if (!ocrProcessor.isReadable(document)) {
                HashSet<Token> tokenSetOcr = ocrProcessor.doOcr(document);
                allClasses.forEach((classname, tokenSetClass) -> {
                    predictions.add(classifier.predict(tokenSetOcr, classname, tokenSetClass, false));
                });
            } else {
                HashSet<Token> tokenSetPdf = pdfProcessor.getTokensFromPdf(document);
                allClasses.forEach((classname, tokenSetClass) -> {
                    predictions.add(classifier.predict(tokenSetPdf, classname, tokenSetClass, true));
                });
            }
            document.close();
            log.info("time = {} milliseconds", (System.currentTimeMillis() - start));
            return predictions;
        } catch (IOException e) {
            log.error("Not supported filetype");
            return null;
        }
    }
}
