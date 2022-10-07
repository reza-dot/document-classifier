package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.pdfutils.GetTokenLocationAndSize;
import de.reza.documentclassifier.utils.XmlProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class Training {


    XmlProcessor xmlProcessor;

    public Training(XmlProcessor xmlProcessor){
        this.xmlProcessor = xmlProcessor;
    }

    /**
     * Recognizes tokens with their corresponding coordinates on the PDF document.
     * Saves them as XML file in the next step as a class in the model.
     * @param pathToTrainingFiles   PDF documents
     * @param uuid                  Identification number for a model
     */
    public void startTraining(String pathToTrainingFiles, String uuid) {

        Optional<File[]> files = Optional.ofNullable(new File(pathToTrainingFiles).listFiles());

        if (files.isPresent()) {
            for (File file : files.get()) {
                HashSet<Token> tokenSet = new HashSet<>();
                try (InputStream resource = new FileInputStream(pathToTrainingFiles + file.getName())) {
                    PDDocument document = PDDocument.load(resource);
                    PDFTextStripper stripper = new GetTokenLocationAndSize(tokenSet);
                    stripper.setSortByPosition(true);
                    stripper.setStartPage(0);
                    stripper.setEndPage(document.getNumberOfPages());
                    Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                    stripper.writeText(document, dummy);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String fileNameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));
                xmlProcessor.generateTokenXmlFile(tokenSet, uuid, fileNameWithoutExtension);
            }
            try {
                FileSystemUtils.deleteRecursively(Paths.get(pathToTrainingFiles));
            } catch (IOException e) {
                log.error("Training files could not be deleted");
            }
        }
        else{
            log.error("Path to training files not found");
        }
    }
}
