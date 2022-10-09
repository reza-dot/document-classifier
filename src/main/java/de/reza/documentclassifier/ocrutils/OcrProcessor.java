package de.reza.documentclassifier.ocrutils;

import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OcrProcessor {

    @Value("${SCALE_FACTOR_X}")
    private double scaleFactorX;

    @Value("${SCALE_FACTOR_Y}")
    private double scaleFactorY;

    @Value("${DPI}")
    private int dpi;

    /**
     * Checks if the PDF document is searchable. Counts the fonts that appear in the document.
     * @param doc   Given document
     * @return      True: No font occurs in the document. False: Font occurs in the document.
     */
    public boolean isReadable(PDDocument doc) {

        PDPage page = doc.getPage(0); // 0 based
        PDResources resources = page.getResources();
        AtomicInteger number= new AtomicInteger();
        resources.getFontNames().iterator().forEachRemaining(font -> number.getAndIncrement());
        return number.get() == 0 ? false : true;
    }

    /**
     * Performs OCR from the text and tokenizes the text. Coordinates of the tokens are determined.
     * @param document      Given document
     * @return              List of {@link Token}
     * @throws IOException  OCR processing does not work
     */
    public HashSet<Token> doOcr(PDDocument document) throws IOException {

        ITesseract instance = new Tesseract();
        File tessDataFolder = LoadLibs.extractTessResources("tessdata");
        instance.setDatapath(tessDataFolder.getPath());
        instance.setLanguage("deu");
        instance.setVariable("user_defined_dpi", String.valueOf(dpi));
        instance.setPageSegMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_LSTM_COMBINED);
        instance.setOcrEngineMode(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        HashSet<Token> tokenSet = new HashSet<>();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
            instance.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD).forEach(word -> {
                // tess4j recognize for some reason whitespaces as words. Seems to be a bug.
                if(!word.getText().equals(" ")) {
                    Rectangle2D boundingBox = new Rectangle2D.Double(word.getBoundingBox().getX(), word.getBoundingBox().getY(), word.getBoundingBox().getWidth(), word.getBoundingBox().getHeight());
                    tokenSet.add(new Token(word.getText(), round(boundingBox.getX()) * scaleFactorX,  round(boundingBox.getY()) * scaleFactorY, round(boundingBox.getWidth())));
                    log.info("Token: [" + word.getText() + "] X= " + round(boundingBox.getX()) * scaleFactorX + " Y= " + round(boundingBox.getY()) * scaleFactorY + " Width=" + round(boundingBox.getWidth()) + " Height=" + boundingBox.getHeight());
                }
            });
        }
        return tokenSet;
    }

    protected double round(double round){

        BigDecimal bd = new BigDecimal(round).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
