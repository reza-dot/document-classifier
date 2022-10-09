package de.reza.documentclassifier.pdfutils;

import de.reza.documentclassifier.pojo.Token;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;

@Service
public class PdfProcessor {

    /**
     * Generate {@link Token} from a searchable PDF document
     * @param document      pdf document
     * @return              list of all {@link Token}
     * @throws IOException  file is not valid
     */
    public HashSet<Token> getTokensFromPdf(PDDocument document) throws IOException {

        HashSet<Token> tokenSetPdf = new HashSet<>();
        try {
            PDFTextStripper stripper = new GetTokenLocationAndSize(tokenSetPdf);
            stripper.setSortByPosition(true);
            stripper.setStartPage(0);stripper.setEndPage(document.getNumberOfPages());
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
        } catch (IOException e) {
            return null;
        }
        document.close();
        return tokenSetPdf;
    }
}
