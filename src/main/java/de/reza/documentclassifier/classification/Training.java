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


    public void startTraining(String pathToTrainingFiles, String uuid) throws IOException {

        Optional<File[]> files = Optional.ofNullable(new File(pathToTrainingFiles).listFiles());

        if (files.isPresent()) {
            for (File file : files.get()) {
                List<Token> tokenList = new ArrayList<>();
                try (InputStream resource = new FileInputStream(pathToTrainingFiles + file.getName())) {
                    PDDocument document = PDDocument.load(resource);
                    PDFTextStripper stripper = new GetTokenLocationAndSize(tokenList);
                    stripper.setSortByPosition(true);
                    stripper.setStartPage(0);
                    stripper.setEndPage(document.getNumberOfPages());
                    Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                    stripper.writeText(document, dummy);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String fileNameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));
                xmlProcessor.generateTokenXmlFile(tokenList, uuid, fileNameWithoutExtension);
            }
            FileSystemUtils.deleteRecursively(Paths.get(pathToTrainingFiles));
        }
        else{
            log.error("Path to training files not found");
        }
    }
}
