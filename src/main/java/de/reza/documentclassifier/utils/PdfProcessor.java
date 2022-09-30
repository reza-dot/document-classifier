package de.reza.documentclassifier.utils;

import de.reza.documentclassifier.pojo.Token;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class PdfProcessor {


    private PDDocument document;

    private List<Token> tokenList;
    @Autowired
    XmlProcessor xmlProcessor;


    public void getCoordinates(String pathToTrainingFiles, String uuid) throws IOException {

        File[] files = new File(pathToTrainingFiles).listFiles();

        for (File file : files) {

            List<Token> tokenList = new ArrayList<>();

            try (InputStream resource = new FileInputStream(pathToTrainingFiles + file.getName())) {

                PDDocument document = PDDocument.load(resource);
                PDFTextStripper stripper = new GetTokenLocationAndSize(tokenList);
                stripper.setSortByPosition(true);
                stripper.setStartPage(0);
                stripper.setEndPage( document.getNumberOfPages() );
                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                stripper.writeText(document, dummy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String fileNameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));
            xmlProcessor.generateTokenXmlFile(tokenList, uuid, fileNameWithoutExtension);
        }
        FileSystemUtils.deleteRecursively(Paths.get(pathToTrainingFiles));
    }

    public String predict(PDDocument document, List<Token> tokenList) {

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
        return "Probability: " + String.format("%.2f",(double) amoutOfFoundedTokens/(double) tokenList.size()) +
                "\nNumber of founded tokens: " + amoutOfFoundedTokens + "\nNumber of total tokens: " + tokenList.size();
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
