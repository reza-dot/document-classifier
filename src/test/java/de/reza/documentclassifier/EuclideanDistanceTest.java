package de.reza.documentclassifier;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
public class EuclideanDistanceTest {

    @Autowired
    MathUtils mathUtils;

    @Test
    public void calculateDistanceBetweenTwoPoints(){

        Token token1 = new Token("Apfelkuchen", 40.2, 10.7);
        Token token2 = new Token("Butterkuchen", 70.9, 1.7);

        double distance = mathUtils.euclideanDistance(token1, token2);
        // rounded up to 2 decimal places
        BigDecimal roundedDistance = new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP);
        Assertions.assertEquals(31.99,roundedDistance.doubleValue());
    }
}
