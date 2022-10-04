package de.reza.documentclassifier;

import de.reza.documentclassifier.utils.EuclideanDistance;
import de.reza.documentclassifier.pojo.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
public class EuclideanDistanceTest {

    @Test
    public void calculateDistanceBetweenTwoPoints(){

        Token token1 = new Token("Apfelkuchen", 40.2f, 10.7f, 5f);
        Token token2 = new Token("Butterkuchen", 70.9f, 1.7f, 19f);

        double distance = EuclideanDistance.calculateDistanceBetweenPoints(token1, token2);
        // rounded up to 2 decimal places
        BigDecimal roundedDistance = new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP);
        Assertions.assertEquals(31.99,roundedDistance.doubleValue());
    }

}
