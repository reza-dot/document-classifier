package de.reza.documentclassifier;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.utils.OcrProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class OcrIntegrationTest {

    @Autowired
    OcrProcessor ocrProcessor;
    @Autowired
    Classifier predictor;

    /**
     * Checks if PDF document is searchable, in order to extract and tokenize the texts from the image document in the next step.
     */
    //@Test
    public void readTextFromImage() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("test/Image_document_300_dpi.pdf");
        assertTrue(classPathResource.getFile().exists());
        PDDocument document = PDDocument.load(classPathResource.getInputStream());
        assertFalse(ocrProcessor.isReadable(document));
        List<Token> tokenSet = ocrProcessor.doOcr(document);
        List<Token> tokenSetClass = new ArrayList<>();
        tokenSetClass.add(new Token("Das", 71, 74));
        tokenSetClass.add(new Token("ist", 179, 99));
        tokenSetClass.add(new Token("nur", 212, 175));
        tokenSetClass.add(new Token("ein", 283, 225));
        tokenSetClass.add(new Token("Test", 306, 227));
        tokenSetClass.add(new Token("OCR", 354, 280));
        tokenSetClass.add(new Token("funktioniert", 141, 367));
        Assertions.assertEquals(tokenSetClass.size(), tokenSet.size());
        Prediction probability =  predictor.predict(tokenSet, "Test", tokenSetClass, false);
        Assertions.assertEquals(1.0, probability.getProbability());
    }
}
