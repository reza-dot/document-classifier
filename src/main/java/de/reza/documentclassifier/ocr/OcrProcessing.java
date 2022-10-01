package de.reza.documentclassifier.ocr;

import de.reza.documentclassifier.pojo.Token;
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
public class OcrProcessing {

    @Value("${TESSDATA_PREFIX}")
    private String tessdata;

    public int checkForOcr(PDDocument doc) {

        PDPage page = doc.getPage(0); // 0 based
        PDResources resources = page.getResources();

        AtomicInteger number= new AtomicInteger();
        resources.getFontNames().iterator().forEachRemaining(font -> number.getAndIncrement());
        return number.get();
    }

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
                tokenList.add(new Token(word.getText(), boundingBox.getX()*0.24 , boundingBox.getY()*0.24, boundingBox.getWidth()));
                System.out.println("Token: [" + word.getText() + "] X= " + boundingBox.getX() * 0.24  + " Y= " + boundingBox.getY() *0.24 + " Width=" + boundingBox.getWidth() + " Height=" + boundingBox.getHeight());

            }
        }

        System.out.println("time = " + (System.currentTimeMillis() - start)/1000);
        return tokenList;
    }

}
