package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Prediction;
import de.reza.documentclassifier.classification.Training;
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
    Prediction predictor;
    Training trainer;
    XmlProcessor xmlProcessor;
    OcrProcessor ocrProcessor;
    
    public Controller(DatasetProcessor datasetProcessor, Prediction predicter, Training trainer, XmlProcessor xmlProcessor, OcrProcessor ocrProcessor){
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
    public String predict(@PathVariable("uuid") @NonNull String uuid, @RequestParam("document") @NonNull Optional<MultipartFile> file)  {

        PDDocument document;
        try {
            document = PDDocument.load(file.get().getInputStream());
            Optional<File[]> files = Optional.ofNullable(new File("models/" + uuid).listFiles());

            Map<String, HashSet<Token>> allClasses = new HashMap<>();
            for (File xmlFile : files.get()) {
                allClasses.put(xmlFile.getName(), xmlProcessor.readXmlFile(xmlFile));
            }

            String[] probabilities = new String[files.get().length];
            AtomicInteger counter = new AtomicInteger();

            if (!ocrProcessor.isReadable(document)) {
                HashSet<Token> tokenListOcr = ocrProcessor.doOcr(document);
                for (Map.Entry<String, HashSet<Token>> entry : allClasses.entrySet()) {
                    probabilities[counter.get()] = predictor.predict(tokenListOcr, entry.getKey(), entry.getValue());
                    counter.incrementAndGet();
                }
            } else {

                for (Map.Entry<String, HashSet<Token>> entry : allClasses.entrySet()) {
                    probabilities[counter.get()] = predictor.predict(document, entry.getKey(), entry.getValue());
                    counter.incrementAndGet();
                }
            }
            document.close();
            return Arrays.toString(probabilities);
        } catch (IOException e) {
            log.error("No file provided");
            return "No file provided";
        }
    }
}
