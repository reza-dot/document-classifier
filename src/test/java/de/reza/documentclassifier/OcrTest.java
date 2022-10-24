package de.reza.documentclassifier;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.pdf.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class OcrTest {

    @Autowired
    PdfProcessor pdfProcessor;
    @Autowired
    Classifier predictor;

    /**
     * Checks if PDF document is searchable, in order to extract and tokenize the texts from the image document in the next step.
     */
    @Test
    public void readTextFromImage() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("test/OcrTest_Image_document_300_dpi.pdf");
        assertTrue(classPathResource.getFile().exists());
        PDDocument document = PDDocument.load(classPathResource.getInputStream());
        assertFalse(pdfProcessor.isSearchable(document));
        List<Token> tokenList = pdfProcessor.getTokensFromPdfWithOcr(document);
        List<Token> tokenListClass = new ArrayList<>();
        tokenListClass.add(new Token("Das", 71, 74));
        tokenListClass.add(new Token("ist", 179, 99));
        tokenListClass.add(new Token("nur", 212, 175));
        tokenListClass.add(new Token("ein", 283, 225));
        tokenListClass.add(new Token("Test", 306, 227));
        tokenListClass.add(new Token("OCR", 354, 280));
        tokenListClass.add(new Token("funktioniert", 141, 367));
        Assertions.assertEquals(tokenListClass.size(), tokenList.size());
        Prediction probability =  predictor.predict(tokenList, "Test", tokenListClass, false);
        Assertions.assertEquals(1.0, probability.getProbability());
    }
}
