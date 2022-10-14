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
import java.util.HashSet;

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
        HashSet<Token> tokenSet = ocrProcessor.doOcr(document);
        HashSet<Token> tokenSetClass = new HashSet<>();
        tokenSetClass.add(new Token("Das", 71, 74, 32));
        tokenSetClass.add(new Token("ist", 179, 99, 78));
        tokenSetClass.add(new Token("nur", 212, 175, 28));
        tokenSetClass.add(new Token("ein", 283, 225, 79));
        tokenSetClass.add(new Token("Test", 306, 227, 43));
        tokenSetClass.add(new Token("OCR", 354, 280, 118));
        tokenSetClass.add(new Token("funktioniert", 141, 367, 406));
        Assertions.assertEquals(tokenSetClass.size(), tokenSet.size());
        Prediction probability =  predictor.predict(tokenSet, "Test", tokenSetClass, false);
        Assertions.assertEquals(1.0, probability.getProbability());
    }
}
