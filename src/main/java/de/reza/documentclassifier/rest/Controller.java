package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.classification.Prediction;
import de.reza.documentclassifier.classification.Training;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.ocr.OcrProcessing;
import de.reza.documentclassifier.utils.XmlProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
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
    OcrProcessing ocrProcessing;

    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")MultipartFile file) throws IOException {

        String uuid = UUID.randomUUID().toString();
        String pathToTrainingfiles = datasetProcessor.unzip(file, uuid);
        trainer.startTraining(pathToTrainingfiles, uuid);
        return "Your trained model " + uuid;
    }

    @GetMapping("/api/predict/{uuid}")
    public String predict(@PathVariable("uuid") String uuid, @RequestParam("document") MultipartFile file) throws IOException {


        PDDocument document = PDDocument.load(file.getInputStream());
        File[] files = new File("models/"+ uuid).listFiles();
        Map<String,List<Token>> allTokenLists = new HashMap<>();
        for (File xmlFile : files) {
            allTokenLists.put(xmlFile.getName() ,xmlProcessor.readXmlFile(xmlFile));
        }

        String[] probs = new String[files.length];
        int counter = 0;

        int numberOfFoundFonts = ocrProcessing.checkForOcr(document);

        if(numberOfFoundFonts == 0){
            List<Token> tokenListOcr = ocrProcessing.doOcr(document);
            for (Map.Entry<String,List<Token>> entry : allTokenLists.entrySet()) {
                probs[counter] = predicter.predict(tokenListOcr, entry.getKey(),entry.getValue());
                counter++;
            }
            document.close();
            return Arrays.toString(probs);
        }else {

            for (Map.Entry<String,List<Token>> entry : allTokenLists.entrySet()) {
                probs[counter] = predicter.predict(document, entry.getKey(), entry.getValue());
                counter++;
            }
            document.close();
            return Arrays.toString(probs);
        }
    }
}
