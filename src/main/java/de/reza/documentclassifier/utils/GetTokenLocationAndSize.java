package de.reza.documentclassifier.utils;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GetTokenLocationAndSize extends PDFTextStripper {
    public GetTokenLocationAndSize() throws IOException {
    }


    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        String wordSeparator = getWordSeparator();
        List<TextPosition> word = new ArrayList<>();
        for (TextPosition text : textPositions) {
            String thisChar = text.getUnicode();
            if (thisChar != null) {
                if (thisChar.length() >= 1) {
                    if (!thisChar.equals(wordSeparator)) {
                        word.add(text);
                    } else if (!word.isEmpty()) {
                        printWord(word);
                        word.clear();
                    }
                }
            }
        }
        if (!word.isEmpty()) {
            printWord(word);
            word.clear();
        }
    }

    void printWord(List<TextPosition> word) {
        Rectangle2D boundingBox = null;
        StringBuilder token = new StringBuilder();
        for (TextPosition text : word) {
            Rectangle2D box = new Rectangle2D.Float(text.getXDirAdj(), text.getYDirAdj(), text.getWidthDirAdj(), text.getHeightDir());
            if (boundingBox == null)
                boundingBox = box;
            else
                boundingBox.add(box);
            token.append(text.getUnicode());
        }
        System.out.println("<"+ token + ">" + " [(X=" + round(boundingBox.getX()) + ",Y=" +  round(boundingBox.getY())
                + ") height=" +  round(boundingBox.getHeight()) + " width=" +  round(boundingBox.getWidth()) + "]");
    }

    protected double round(double round){

        BigDecimal bd = new BigDecimal(round).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
