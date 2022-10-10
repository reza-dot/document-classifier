package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pdfutils.PdfProcessor;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.JsonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class Training {

    PdfProcessor pdfProcessor;

    JsonProcessor jsonProcessor;

    public Training(PdfProcessor pdfProcessor, JsonProcessor jsonProcessor){
        this.pdfProcessor = pdfProcessor;
        this.jsonProcessor = jsonProcessor;
    }

    /**
     * Recognizes tokens with their corresponding coordinates on the PDF document.
     * Saves them as XML file in the next step as a class in the model.
     * @param pathToTrainingFiles   PDF documents
     * @param uuid                  Identification number for a model
     */
    public void startTraining(String pathToTrainingFiles, String uuid){

        Optional<File[]> files = Optional.ofNullable(new File(pathToTrainingFiles).listFiles());

        files.ifPresent(pdfFiles -> Arrays.stream(pdfFiles).toList().forEach(pdfFile -> {
            try {
                HashSet<Token> tokenSet = pdfProcessor.getTokensFromPdf(PDDocument.load(pdfFile));
                String fileNameWithoutExtension = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf('.'));
                jsonProcessor.createJsonFile(tokenSet, uuid, fileNameWithoutExtension);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
            try {
                FileSystemUtils.deleteRecursively(Paths.get(pathToTrainingFiles));
            } catch (IOException e) {
                log.error("Training files could not be deleted");
            }
        }
}

