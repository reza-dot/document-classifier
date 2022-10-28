package de.reza.documentclassifier.utils;

import de.reza.documentclassifier.pojo.Token;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MathUtils {

    /**
     * Calculates the distance between two points
     * @param tokenClass    Token of a class
     * @param token         Token of given pdf document
     * @return              distance between two points
     */
    public double euclideanDistance(Token tokenClass, Token token) {
        return Math.sqrt((tokenClass.getYAxis() - token.getYAxis()) * (tokenClass.getYAxis()- token.getYAxis()) + (tokenClass.getXAxis() - token.getXAxis()) * (tokenClass.getXAxis()  - token.getXAxis()));
    }

    /**
     * Rounded by two decimal places
     * @param round         value which should be rounded
     * @return              rounded value
     */
    public double round(double round){

        BigDecimal bd = BigDecimal.valueOf(round);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
