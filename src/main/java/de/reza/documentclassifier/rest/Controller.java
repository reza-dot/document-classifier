package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.classification.Training;
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class Controller {

    DatasetProcessor datasetProcessor;
    Classifier predictor;
    Training trainer;
    XmlProcessor xmlProcessor;
    OcrProcessor ocrProcessor;
    
    public Controller(DatasetProcessor datasetProcessor, Classifier predicter, Training trainer, XmlProcessor xmlProcessor, OcrProcessor ocrProcessor){
        this.datasetProcessor = datasetProcessor;
        this.predictor = predicter;
        this.trainer = trainer;
        this.xmlProcessor = xmlProcessor;
        this.ocrProcessor = ocrProcessor;
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
            PDDocument document = PDDocument.load(file.getInputStream());
            Optional<File[]> files = Optional.ofNullable(new File("models/" + uuid).listFiles());
            Map<String, HashSet<Token>> allClasses = new HashMap<>();
            files.ifPresent(xmlFiles -> Arrays.stream(xmlFiles).toList().forEach(xmlFile -> {
                allClasses.put(xmlFile.getName(), xmlProcessor.readXmlFile(xmlFile));

            }));
            List<Prediction> predictions = new ArrayList<>();
            if (!ocrProcessor.isReadable(document)) {
                HashSet<Token> tokenListOcr = ocrProcessor.doOcr(document);
                allClasses.forEach((classname, tokenSetClass) -> {
                    predictions.add(predictor.predict(tokenListOcr, classname, tokenSetClass));
                });
            } else {
                allClasses.forEach((classname, tokenSetClass) -> {
                    predictions.add(predictor.predict(document, classname, tokenSetClass));
                });
            }
            document.close();
            return predictions;
        } catch (IOException e) {
            log.error("Not supported filetype");
            return null;
        }
    }
}
