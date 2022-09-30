package de.reza.documentclassifier.rest;

import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.utils.PdfProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
public class Controller {

    @Autowired
    DatasetProcessor datasetProcessor;

    @Autowired
    PdfProcessor pdfProcessor;

    @Value("${TESSDATA_PREFIX}")
    private String tessdata;

    @PostMapping("/api/train")
    public String training(@RequestParam("dataset")MultipartFile file) throws IOException {

        String uuid = UUID.randomUUID().toString();
        String pathToTrainingfiles = datasetProcessor.unzip(file, uuid);
        pdfProcessor.getCoordinates(pathToTrainingfiles);
        return "Your trained model " + uuid;
    }

    @GetMapping("/api/predict/{uuid}")
    public String predict(@PathVariable("uuid") String uuid, @RequestParam("document") MultipartFile file){


        return null;
    }
}
