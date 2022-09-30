package de.reza.documentclassifier.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;


@Service
public class PdfProcessor{
    
    @Autowired
    XmlProcessor xmlProcessor;

    public void getCoordinates(String trainingFiles) throws IOException {

        File[] files = new File(trainingFiles).listFiles();

        for (File file : files) {

            try (InputStream resource = new FileInputStream(trainingFiles + file.getName())) {

                PDDocument document = PDDocument.load(resource);
                PDFTextStripper stripper = new GetTokenLocationAndSize();
                stripper.setSortByPosition( true );
                stripper.setStartPage( 0 );
                stripper.setEndPage( document.getNumberOfPages() );
                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                stripper.writeText(document, dummy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileSystemUtils.deleteRecursively(Paths.get(trainingFiles));
    }
}
