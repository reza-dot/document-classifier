package de.reza.documentclassifier.ocr;

import de.reza.documentclassifier.pojo.Token;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OcrProcessor {

    @Value("${TESSDATA_PREFIX}")
    private String tessdata;

    /**
     * Checks if the PDF document is searchable. Counts the fonts that appear in the document.
     * @param doc   Given document
     * @return      True: No font occurs in the document. False: Font occurs in the document.
     */
    public boolean checkForOcr(PDDocument doc) {

        PDPage page = doc.getPage(0); // 0 based
        PDResources resources = page.getResources();
        AtomicInteger number= new AtomicInteger();
        resources.getFontNames().iterator().forEachRemaining(font -> number.getAndIncrement());
        return number.get() == 0;
    }

    /**
     * Performs OCR from the text and tokenizes the text. Coordinates of the tokens are determined.
     * @param document      Given document
     * @return              List of {@link Token}
     * @throws IOException  OCR processing does not work
     */
    public List<Token> doOcr(PDDocument document) throws IOException {

        long start = System.currentTimeMillis();
        ITesseract it = new Tesseract();
        File tessDataFolder = LoadLibs.extractTessResources(tessdata);
        it.setDatapath(tessDataFolder.getAbsolutePath());
        it.setLanguage("deu");
        it.setVariable("user_defined_dpi", "300");
        it.setPageSegMode(1);
        it.setOcrEngineMode(1);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<Token> tokenList = new ArrayList<>();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

            for (Word word : it.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD)) {

                Rectangle2D boundingBox = new Rectangle2D.Double(word.getBoundingBox().getX(), word.getBoundingBox().getY(), word.getBoundingBox().getWidth(), word.getBoundingBox().getHeight());
                tokenList.add(new Token(word.getText(), boundingBox.getX() * 0.24 , boundingBox.getY() *0.24, boundingBox.getWidth()));
                log.info("Token: [" + word.getText() + "] X= " + boundingBox.getX() * 0.24 + " Y= " + boundingBox.getY() * 0.24  + " Width=" + boundingBox.getWidth() + " Height=" + boundingBox.getHeight());

            }
        }
        log.info("time = {}", (System.currentTimeMillis() - start)/1000);
        return tokenList;
    }

}