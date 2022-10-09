package de.reza.documentclassifier.utils;

import de.reza.documentclassifier.pojo.Token;

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
}
