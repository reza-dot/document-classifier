package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pdf.PdfProcessor;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.JsonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Processes PDF training documents into classes (JSON files)
 */
@Component
@Slf4j
public class Training {

    private final PdfProcessor pdfProcessor;

    private final JsonProcessor jsonProcessor;

    public Training(PdfProcessor pdfProcessor, JsonProcessor jsonProcessor){
        this.pdfProcessor = pdfProcessor;
        this.jsonProcessor = jsonProcessor;
    }

    /**
     * Recognizes tokens with their corresponding coordinates on a searchable PDF document.
     * Saves this in the next step as a JSON file in the model
     * @param pathToTrainingFiles   path to the PDF documents
     * @param uuid                  {@link UUID} for a model
     */
    public void startTraining(String pathToTrainingFiles, String uuid){

        Optional<File[]> files = Optional.ofNullable(new File(pathToTrainingFiles).listFiles());

        files.ifPresent(pdfFiles -> Arrays.stream(pdfFiles).toList().forEach(pdfFile -> {
            try {
                List<Token> tokenList = pdfProcessor.getTokensFromSearchablePdf(PDDocument.load(pdfFile));
                String fileNameWithoutExtension = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf('.'));
                jsonProcessor.createJsonFile(tokenList, uuid, fileNameWithoutExtension);
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

