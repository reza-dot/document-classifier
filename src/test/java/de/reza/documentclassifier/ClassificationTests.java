package de.reza.documentclassifier;


import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.pdf.PdfProcessor;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.DatasetProcessor;
import de.reza.documentclassifier.utils.JsonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
public class ClassificationTests {

    private static final JsonProcessor jsonProcessor = new JsonProcessor();

    @Autowired
    DatasetProcessor datasetProcessor;

    @Autowired
    PdfProcessor pdfProcessor;

    @Autowired
    Classifier classifier;

    static Map<String, List<Token>> allClasses= new HashMap<>();


    @BeforeAll
    public static void getClasses()  {

        Optional<File[]> files = Optional.ofNullable(new File("models/" + "a1cc51ce-254f-4722-9ff6-9f78b3bcad10").listFiles());
        files.ifPresent(jsonFiles -> Arrays.stream(jsonFiles).toList().forEach(jsonFile -> allClasses.put(jsonFile.getName(), jsonProcessor.readJsonFile(jsonFile))));

    }

    //@Test
    public void classifyTest() throws IOException {

        long start = System.currentTimeMillis();
        ClassPathResource classPathResource = new ClassPathResource("test/Datev_withdata_Image.pdf");
        assertTrue(classPathResource.getFile().exists());
        PDDocument document = PDDocument.load(classPathResource.getFile());

        List<Prediction> predictionList = new ArrayList<>();
        List<Token> tokenList = pdfProcessor.getTokensFromPdfWithOcr(document);
        allClasses.forEach((classname, tokenSetClass) -> predictionList.add(classifier.predict(tokenList, classname, tokenSetClass, false)));
        Prediction highestPrediction = predictionList.stream().max(Comparator.comparing(Prediction::getProbability)).get();
        log.info("Classname {}, Found tokens {}, Not Found Tokens {}, Probability {}",
                highestPrediction.getClassname(),
                highestPrediction.getFoundPdfTokens().size(),
                highestPrediction.getNotFoundClassTokens().size(),
                highestPrediction.getProbability());
        log.info("computing time = {} milliseconds", (System.currentTimeMillis() - start));
    }
}
