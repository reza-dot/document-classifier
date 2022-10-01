package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.TextPositionSequence;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Prediction {

    public String predict(List<Token> tokenListOcr, String className, List<Token> tokenListClass){

        int amoutOfFoundedTokens=0;
        int totalTokens = tokenListClass.size();

        for (int i=0; i<=tokenListOcr.size()-1; i++){

            for(int j=0; j<= tokenListClass.size()-1; j++){

                if(tokenListOcr.get(i).getTokeName().equals(tokenListClass.get(j).getTokeName())){
                    double distance = calculateDistanceBetweenPoints(tokenListClass.get(j), tokenListOcr.get(i));
                    if(distance <= 30) {
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

    public double calculateDistanceBetweenPoints(Token tokenClass, Token token) {
        return Math.sqrt((tokenClass.getYAxis() - token.getYAxis()) * (tokenClass.getYAxis()- token.getYAxis()) + (tokenClass.getXAxis() - token.getXAxis()) * (tokenClass.getXAxis()  - token.getXAxis()));
    }

    public String predict(PDDocument document, String classname, List<Token> tokenList) {

        int amoutOfFoundedTokens = 0;
        for (Token token : tokenList) {

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                List<TextPositionSequence> hits = findWord(document, page, token.getTokeName());
                for (TextPositionSequence hit : hits) {
                    double dinstance = calculateDistanceBetweenPoints(hit, token);
                    if (dinstance <= 0.5) {
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

    public double calculateDistanceBetweenPoints(TextPositionSequence hit, Token token) {
        return Math.sqrt((hit.getY() - token.getYAxis()) * (hit.getY()- token.getYAxis()) + (hit.getX() - token.getXAxis()) * (hit.getX() - token.getXAxis()));
    }

    static List<TextPositionSequence> findWord(PDDocument document, int page, String searchTerm)
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
        while ((index = string.indexOf(searchTerm, fromIndex)) > -1)
        {
            hits.add(word.subSequence(index, index + searchTerm.length()));
            fromIndex = index + 1;
        }

        return hits;
    }


}
