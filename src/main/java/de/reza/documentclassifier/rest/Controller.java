package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.utils.PdfProcessor;
import de.reza.documentclassifier.utils.XmlProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    PdfProcessor pdfProcessor;

    @Autowired
    XmlProcessor xmlProcessor;

    @Value("${TESSDATA_PREFIX}")
    private String tessdata;

    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")MultipartFile file) throws IOException {

        String uuid = UUID.randomUUID().toString();
        String pathToTrainingfiles = datasetProcessor.unzip(file, uuid);
        pdfProcessor.getCoordinates(pathToTrainingfiles, uuid);
        return "Your trained model " + uuid;
    }

    @GetMapping("/api/predict/{uuid}")
    public String predict(@PathVariable("uuid") String uuid, @RequestParam("document") MultipartFile file) throws IOException {


        PDDocument document = PDDocument.load(file.getInputStream());
        File[] files = new File("models/"+ uuid).listFiles();
        Set<List<Token>> allTokenLists = new HashSet<>();
        for (File xmlFile : files) {
            allTokenLists.add(xmlProcessor.readXmlFile(xmlFile));
        }


        Iterator<List<Token>> it = allTokenLists.iterator();
        String[] probs = new String[files.length];
        int counter=0;

        while (it.hasNext()) {
            probs[counter] = pdfProcessor.predict(document,it.next());
            counter++;
        }

        System.out.println(Arrays.toString(probs));

        document.close();

        return null;
    }
}
