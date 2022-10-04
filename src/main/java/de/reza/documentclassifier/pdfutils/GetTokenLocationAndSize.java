package de.reza.documentclassifier.pdfutils;

import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GetTokenLocationAndSize extends PDFTextStripper {

    private List<Token> tokenList;

    public GetTokenLocationAndSize(List<Token> tokenList) throws IOException {
        this.tokenList = tokenList;
    }


    @Override
    protected void writeString(String string, List<TextPosition> textPositions){
        String wordSeparator = getWordSeparator();
        List<TextPosition> word = new ArrayList<>();
        textPositions.forEach(w -> {
            String thisChar = w.getUnicode();
            if (thisChar != null && thisChar.length() >= 1 && !thisChar.equals(wordSeparator)) {
                word.add(w);
            } else if (!word.isEmpty()) {
                getTokenBoundingBox(word);
                word.clear();
            }

        });
        if (!word.isEmpty()) {
            getTokenBoundingBox(word);
            word.clear();
        }
    }

    void getTokenBoundingBox(List<TextPosition> word) {
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
        log.info("Token [{}]X= {} Y={} width={}", token, round(boundingBox.getX()), round(boundingBox.getY()), round(boundingBox.getWidth()));
        tokenList.add(new Token(token.toString(), round(boundingBox.getX()), round(boundingBox.getY()), round(boundingBox.getWidth())));
    }

    protected double round(double round){

        BigDecimal bd = new BigDecimal(round).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

