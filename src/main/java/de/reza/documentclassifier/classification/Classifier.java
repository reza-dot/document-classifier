package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.pdfutils.TextPositionSequence;
import de.reza.documentclassifier.utils.EuclideanDistance;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class Classifier {

    @Value("${MAX_DISTANCE}")
    private int maxDistance;
    @Value("${MAX_DISTANCE_OCR}")
    private int maxDistanceOcr;

    /**
     * Predicting a given token set based on the token set of a class.
     * @param tokenSetOcr       Set of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenSetClass}
     * @param tokenSetClass     Included tokens in the class
     * @return                  Returns the relative frequency of the found tokens in the document
     */
    public Prediction predict(HashSet<Token> tokenSetOcr, String classname, HashSet<Token> tokenSetClass){

        AtomicInteger numberOfFoundToken = new AtomicInteger();
        tokenSetOcr.forEach(token -> {
                boolean match = tokenSetClass.stream().anyMatch(tokenClass -> (
                        tokenClass.getTokeName().contains(token.getTokeName()) && EuclideanDistance.calculateDistanceBetweenPoints(tokenClass, token) <= maxDistanceOcr
                        ));
                if (match){
                    numberOfFoundToken.incrementAndGet();
                }
        });
        return new Prediction(classname, numberOfFoundToken.get(), tokenSetClass.size());
    }


    /**
     * Predicting a given document based on the token set of the class.
     * @param document      Given document
     * @param classname     The classname of {@tokenSetClass}
     * @param tokenSetClass Included tokens in the class
     * @return              Returns the relative frequency of the found tokens in the document
     */
    public Prediction predict(PDDocument document, String classname, HashSet<Token> tokenSetClass) {

        AtomicInteger numberOfFoundTokens = new AtomicInteger();
        tokenSetClass.forEach(token -> {
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
            List<TextPositionSequence> hits = findWord(document, page, token.getTokeName());
            hits.forEach(hit -> {
                boolean match = EuclideanDistance.calculateDistanceBetweenPoints(hit, token) <= maxDistance;
                if(match){
                    numberOfFoundTokens.incrementAndGet();
                }
            });
        }});
        return new Prediction(classname, numberOfFoundTokens.get(), tokenSetClass.size());
    }

    /**
     * Finding searched word on a given page within the document
     * @param document      Given document
     * @param page          Page of the document 
     * @param searchWord    searched word
     * @return              List of {@link TextPositionSequence} of the searched word
     */
    static List<TextPositionSequence> findWord(PDDocument document, int page, String searchWord)
    {
        final List<TextPosition> allTextPositions = new ArrayList<>();
        PDFTextStripper stripper;
        try {
            stripper = new PDFTextStripper()
            {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException
                {
                    allTextPositions.addAll(textPositions);
                    super.writeString(text, textPositions);
                }

                @Override
                protected void writeLineSeparator() throws IOException {
                    if (!allTextPositions.isEmpty()) {
                        TextPosition last = allTextPositions.get(allTextPositions.size() - 1);
                        if (!" ".equals(last.getUnicode())) {
                            Matrix textMatrix = last.getTextMatrix().clone();
                            textMatrix.setValue(2, 0, last.getEndX());
                            textMatrix.setValue(2, 1, last.getEndY());
                            TextPosition separatorSpace = new TextPosition(last.getRotation(), last.getPageWidth(), last.getPageHeight(),
                                    textMatrix, last.getEndX(), last.getEndY(), last.getHeight(), 0, last.getWidthOfSpace(), " ",
                                    new int[] {' '}, last.getFont(), last.getFontSize(), (int) last.getFontSizeInPt());
                            allTextPositions.add(separatorSpace);
                        }
                    }
                    super.writeLineSeparator();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        try {
            stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final List<TextPositionSequence> hits = new ArrayList<>();
        TextPositionSequence word = new TextPositionSequence(allTextPositions);
        String string = word.toString();

        int fromIndex = 0;
        int index;
        while ((index = string.indexOf(searchWord, fromIndex)) > -1)
        {
            hits.add(word.subSequence(index, index + searchWord.length()));
            fromIndex = index + 1;
        }

        return hits;
    }


}
