package de.reza.documentclassifier.utils;

import de.reza.documentclassifier.pojo.Token;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MathUtils {

    /**
     * Calculates the distance between two points
     * @param firstToken    Token of a class
     * @param secondToken   Token of given pdf document
     * @return              distance between two points
     */
    public double euclideanDistance(Token firstToken, Token secondToken) {
        return Math.sqrt((firstToken.getYAxis() - secondToken.getYAxis()) * (firstToken.getYAxis() - secondToken.getYAxis()) +
                        (firstToken.getXAxis() - secondToken.getXAxis()) * (firstToken.getXAxis()  - secondToken.getXAxis()));
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
