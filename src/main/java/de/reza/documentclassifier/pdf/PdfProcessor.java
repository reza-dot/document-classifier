package de.reza.documentclassifier.pdf;

import de.reza.documentclassifier.config.TesseractConfig;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class PdfProcessor {

    @Value("${SCALE_FACTOR_X}")
    private double scaleFactorX;

    @Value("${SCALE_FACTOR_Y}")
    private double scaleFactorY;

    @Value("${DPI}")
    private int dpi;

    private final MathUtils mathUtils;

    private final TesseractConfig tesseractConfig;

    public PdfProcessor(MathUtils mathUtils, TesseractConfig tesseractConfig){
        this.mathUtils = mathUtils;
        this.tesseractConfig = tesseractConfig;

    }

    /**
     * Generate a list of {@link Token} from a searchable PDF document
     * @param document      pdf document
     * @return              list of all {@link Token}
     * @throws IOException  file is not valid
     */
    public List<Token> getTokensFromSearchablePdf(PDDocument document) throws IOException {

        List<Token> tokenListPdf = new ArrayList<>();
        try {
            PDFTextStripper stripper = new SearchablePdfUtil(tokenListPdf);
            stripper.setSortByPosition(true);
            stripper.setStartPage(0);stripper.setEndPage(document.getNumberOfPages());
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
        } catch (IOException e) {
            return null;
        }
        document.close();
        return tokenListPdf;
    }

    /**
     * Performs OCR and generate a list of {@Token} from a PDF document
     * @param document      Given document
     * @return              List of {@link Token}
     * @throws IOException  OCR processing does not work
     */
    public List<Token> getTokensFromPdfWithOcr(PDDocument document) throws IOException {

        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<Token> tokenList = new ArrayList<>();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
            tesseractConfig.getInstance().getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD).forEach(word -> {
                // tess4j recognize for some reason whitespaces as words. Seems to be a bug.
                if(!word.getText().equals(" ")) {
                    Rectangle2D boundingBox = new Rectangle2D.Double(word.getBoundingBox().getX(), word.getBoundingBox().getY(), word.getBoundingBox().getWidth(), word.getBoundingBox().getHeight());
                    tokenList.add(new Token(word.getText(), mathUtils.round(boundingBox.getX()) * scaleFactorX,  mathUtils.round(boundingBox.getY()) * scaleFactorY));
                    log.info("Token: [" + word.getText() + "] X= " + mathUtils.round(boundingBox.getX()) * scaleFactorX + " Y= " + mathUtils.round(boundingBox.getY()) * scaleFactorY);
                }
            });
        }
        return tokenList;
    }

    /**
     * Checks if the provided document is searchable
     * @param document  PDF document
     * @return          true: is searchable ; false: is not searchable
     */
    public boolean isSearchable(PDDocument document) {

        PDPage page = document.getPage(0);
        PDResources resources = page.getResources();
        AtomicInteger number= new AtomicInteger();
        resources.getFontNames().iterator().forEachRemaining(font -> number.getAndIncrement());
        return number.get() != 0;
    }
}
