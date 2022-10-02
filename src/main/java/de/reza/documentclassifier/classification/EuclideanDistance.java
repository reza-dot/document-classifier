package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.pdfutils.TextPositionSequence;

public class EuclideanDistance {

    /**
     * Calculates the distance between two points
     * @param tokenClass    Token of a class
     * @param token         Token of given document
     * @return              distance between two points
     */
    public static double calculateDistanceBetweenPoints(Token tokenClass, Token token) {
        return Math.sqrt((tokenClass.getYAxis() - token.getYAxis()) * (tokenClass.getYAxis()- token.getYAxis()) + (tokenClass.getXAxis() - token.getXAxis()) * (tokenClass.getXAxis()  - token.getXAxis()));
    }

    /**
     * Calculates the distance between two points
     * @param hit    Token of a class
     * @param token         Token of given document
     * @return              distance between two points
     */
    public static double calculateDistanceBetweenPoints(TextPositionSequence hit, Token token) {
        return Math.sqrt((hit.getY() - token.getYAxis()) * (hit.getY()- token.getYAxis()) + (hit.getX() - token.getXAxis()) * (hit.getX() - token.getXAxis()));
    }
}
