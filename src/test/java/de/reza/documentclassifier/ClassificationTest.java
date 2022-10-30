package de.reza.documentclassifier;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.pdf.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Providing high distance in order to get all Tokens from the class.
@SpringBootTest(properties = { "MAX_DISTANCE=99999999" })
@Slf4j
public class ClassificationTest {

    @Autowired
    PdfProcessor pdfProcessor;

    @Autowired
    Classifier classifier;

    @Autowired
    MathUtils mathUtils;

    @Test
    public void getCorrectToken() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("test/ClassificationTest_Artikel.pdf");
        assertTrue(classPathResource.getFile().exists());

        // Contains only one Token: Token [Artikel-Nr.:]X= 354.07 Y=385.01
        List<Token> tokenList = pdfProcessor.getTokensFromSearchablePdf(PDDocument.load(classPathResource.getFile()));

        // Represents a class
        List<Token> tokenListClass = List.of(
                new Token("Artikel-Nr.:", 250.0, 380.4),
                new Token("Artikel-Nr.:", 200.0, 380.4),
                new Token("Artikel-Nr.:", 324.0, 11.4),
                new Token("Artikel-Nr.:", 355.0, 386.4), // correct Token, because nearest neighbor to class token
                new Token("Artikel-Nr.:", 124.0, 0.4),
                new Token("Artikel-Nr.:", 431, 99.2),
                new Token("Bestellnummer:", 11, 43.9)
        );
        Prediction prediction = classifier.predict(tokenList, "test", tokenListClass, true);
        // Assert that only one token is found.
        assertEquals(1, prediction.getFoundPdfTokens().size());
        log.info("Found tokens {}", prediction.getFoundPdfTokens());

        // Assert that 4 tokens are not found
        assertEquals(6, prediction.getNotFoundClassTokens().size());
        log.info("Not Found tokens {}", prediction.getNotFoundClassTokens());

        //Assert that the token class of the correct match is not in the set of notFoundClass
        assertFalse(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 355.0, 386.4)));

        //assert that the 4 wrong tokens are in the set.
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 200.0, 380.4)));
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 324.0, 11.4)));
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 250.0, 380.4)));
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 124.0, 0.4)));
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Artikel-Nr.:", 431, 99.2)));
        assertTrue(prediction.getNotFoundClassTokens().contains(new Token("Bestellnummer:", 11, 43.9)));

        // Get the only Token
        Token tokenFromPrediction = prediction.getFoundPdfTokens().entrySet().iterator().next().getKey();

        // Euclidean distance between the correct token from the list and the token from the pdf is about 1.67
        double distance = mathUtils.round(mathUtils.euclideanDistance(tokenFromPrediction, new Token("Artikel-Nr.:", 355.0, 386.4)));
        assertEquals(1.67, distance);
    }
}
