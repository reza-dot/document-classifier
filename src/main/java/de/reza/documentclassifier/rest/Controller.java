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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class Controller {

    @Autowired
    DatasetProcessor datasetProcessor;

    @Autowired
    Prediction predicter;

    @Autowired
    Training trainer;

    @Autowired
    XmlProcessor xmlProcessor;

    @Autowired
    OcrProcessor ocrProcessing;

    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")@NonNull MultipartFile file) {

        try {
            String uuid = UUID.randomUUID().toString();
            String pathToTrainingfiles = datasetProcessor.unzip(file, uuid);
            trainer.startTraining(pathToTrainingfiles, uuid);
            return "Your trained model " + uuid;
        } catch (IOException e) {
            log.error("No valid training data was transferred");
            return "No valid training data was transferred";
        }
    }

    @GetMapping("/api/predict/{uuid}")
    public String predict(@PathVariable("uuid") @NonNull String uuid, @RequestParam("document") @NonNull Optional<MultipartFile> file)  {

        PDDocument document = null;
        try {
            document = PDDocument.load(file.get().getInputStream());
            File[] files = new File("models/" + uuid).listFiles();
            Map<String, List<Token>> allClasses = new HashMap<>();
            for (File xmlFile : files) {
                allClasses.put(xmlFile.getName(), xmlProcessor.readXmlFile(xmlFile));
            }

            String[] probs = new String[files.length];
            AtomicInteger counter = new AtomicInteger();

            boolean numberOfFoundFonts = ocrProcessing.checkForOcr(document);

            if (numberOfFoundFonts) {
                List<Token> tokenListOcr = ocrProcessing.doOcr(document);
                for (Map.Entry<String, List<Token>> entry : allClasses.entrySet()) {
                    probs[counter.get()] = predicter.predict(tokenListOcr, entry.getKey(), entry.getValue());
                    counter.incrementAndGet();
                }
                document.close();
                return Arrays.toString(probs);
            } else {

                for (Map.Entry<String, List<Token>> entry : allClasses.entrySet()) {
                    probs[counter.get()] = predicter.predict(document, entry.getKey(), entry.getValue());
                    counter.incrementAndGet();
                }
                document.close();
                return Arrays.toString(probs);
            }
        } catch (IOException e) {
            log.error("No file provided");
            return "No file provided";
        }
    }
}
