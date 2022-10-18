package de.reza.documentclassifier.pdf;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class SearchablePdfUtil extends PDFTextStripper {


    private final List<Token> tokenList;
    private final MathUtils mathUtils;

    public SearchablePdfUtil(List<Token> tokenList) throws IOException {
        this.tokenList = tokenList;
        this.mathUtils = new MathUtils();
    }

    /**
     * <a href="https://github.com/mkl-public/testarea-pdfbox2/blob/master/src/test/java/mkl/testarea/pdfbox2/extract/ExtractWordCoordinates.java">...</a>
     * @param string
     * @param textPositions
     */
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
            if (boundingBox == null) {
                boundingBox = box;
            }else {
                boundingBox.add(box);
            }
            token.append(text.getUnicode());
        }
        log.info("Token [{}]X= {} Y={}", token, mathUtils.round(boundingBox.getX()), mathUtils.round(boundingBox.getY()));
        tokenList.add(new Token(token.toString(), mathUtils.round(boundingBox.getX()), mathUtils.round(boundingBox.getY())));
    }
}

