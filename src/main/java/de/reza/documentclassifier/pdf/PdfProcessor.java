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
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processes PDF documents in order to get {@link Token}
 */
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
     * Create a list of {@link Token} from a searchable PDF document
     * @param document      pdf document
     * @return              list of all {@link Token}
     */
    public List<Token> getTokensFromSearchablePdf(PDDocument document)  {

        List<Token> tokenList = new ArrayList<>();
        try {
            PDFTextStripper stripper = new SearchablePdfUtil(tokenList);
            stripper.setSortByPosition(true);
            stripper.setStartPage(0);
            stripper.setEndPage(document.getNumberOfPages());
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
            document.close();
            return tokenList;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Performs OCR and create a list of {@Token} from a PDF document
     * @param document      Given document
     * @return              List of {@link Token}
     */
    public List<Token> getTokensFromPdfWithOcr(PDDocument document)  {

        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<Token> tokenList = new ArrayList<>();
        try {
        for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                tesseractConfig.getInstance().getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD).forEach(word -> {
                    // tess4j recognize for some reason whitespaces as words. Seems to be a bug.
                    if(!word.getText().equals(" ")) {
                        tokenList.add(new Token(word.getText(), mathUtils.round(word.getBoundingBox().getX() * scaleFactorX) ,  mathUtils.round(word.getBoundingBox().getY() * scaleFactorY)));
                        log.info("Token: [" + word.getText() + "] X= " + mathUtils.round(word.getBoundingBox().getX() * scaleFactorX) + " Y= " + mathUtils.round(word.getBoundingBox().getY() * scaleFactorY));
                    }
                });
            }
            return tokenList;
        } catch (IOException e) {
            return null;
        }
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
