package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.TextPositionSequence;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Prediction {

    @Value("${MAX_DISTANCE}")
    private int maxDistance;

    /**
     * Predicting a given token list based on the token lists of the classes.
     * @param tokenListOcr      Recognized tokens by OCR from the given document
     * @param className         The classname of {@tokenListClass}
     * @param tokenListClass    Included tokens in the class
     * @return                  Returns the relative frequency of the found tokens in the document
     */
    public String predict(List<Token> tokenListOcr, String className, List<Token> tokenListClass){

        int amoutOfFoundedTokens=0;
        int totalTokens = tokenListClass.size();

        for (int i=0; i<=tokenListOcr.size()-1; i++){
            for(int j=0; j<= tokenListClass.size()-1; j++){
                if(tokenListOcr.get(i).getTokeName().equals(tokenListClass.get(j).getTokeName())){
                    double distance = calculateDistanceBetweenPoints(tokenListClass.get(j), tokenListOcr.get(i));
                    if(distance <= maxDistance) {
                        amoutOfFoundedTokens = amoutOfFoundedTokens + 1;
                        tokenListClass.remove(j);
                    }
                }
            }
        }
        return "\nClass: " + className + " "
                + "\nProbability of class: " +String.format("%.2f",(double) amoutOfFoundedTokens/(double) totalTokens)
                + "\nNumber of found tokens within document: " + amoutOfFoundedTokens + "\nNumber of total tokens in class: " + totalTokens
                + "\n---\n\n";
    }

    /**
     * Calculates the distance between two points
     * @param tokenClass    Token of a class
     * @param token         Token of given document
     * @return              distance between two points
     */
    public double calculateDistanceBetweenPoints(Token tokenClass, Token token) {
        return Math.sqrt((tokenClass.getYAxis() - token.getYAxis()) * (tokenClass.getYAxis()- token.getYAxis()) + (tokenClass.getXAxis() - token.getXAxis()) * (tokenClass.getXAxis()  - token.getXAxis()));
    }

    /**
     * Predicting a given document based on the token lists of the classes.
     * @param document      Given document
     * @param classname     The classname of {@tokenListClass}
     * @param tokenList     Included tokens in the class
     * @return              Returns the relative frequency of the found tokens in the document
     */
    public String predict(PDDocument document, String classname, List<Token> tokenList) {

        int amoutOfFoundedTokens = 0;
        for (Token token : tokenList) {

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                List<TextPositionSequence> hits = findWord(document, page, token.getTokeName());
                for (TextPositionSequence hit : hits) {
                    double dinstance = calculateDistanceBetweenPoints(hit, token);
                    if (dinstance <= maxDistance) {
                        amoutOfFoundedTokens = amoutOfFoundedTokens + 1;
                    }
                }
            }
        }
        return "\nClass: " + classname + " "
                + "\nProbability of class: " +String.format("%.2f",(double) amoutOfFoundedTokens/(double) tokenList.size())
                + "\nNumber of found tokens within document: " + amoutOfFoundedTokens + "\nNumber of total tokens in class: " + tokenList.size()
                + "\n---\n\n";
    }

    /**
     * Calculates the distance between two points
     * @param hit    Token of a class
     * @param token         Token of given document
     * @return              distance between two points
     */
    public double calculateDistanceBetweenPoints(TextPositionSequence hit, Token token) {
        return Math.sqrt((hit.getY() - token.getYAxis()) * (hit.getY()- token.getYAxis()) + (hit.getX() - token.getXAxis()) * (hit.getX() - token.getXAxis()));
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
        PDFTextStripper stripper = null;
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

        final List<TextPositionSequence> hits = new ArrayList<TextPositionSequence>();
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
