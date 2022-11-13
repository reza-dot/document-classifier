package de.reza.documentclassifier;

import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EuclideanDistanceTest {

    @Autowired
    MathUtils mathUtils;

    @Test
    public void calculateDistanceBetweenTwoPoints(){

        Token token1 = new Token("Apfelkuchen", 40.2, 10.7);
        Token token2 = new Token("Butterkuchen", 70.9, 1.7);

        double distance = mathUtils.round(mathUtils.euclideanDistance(token1, token2));
        Assertions.assertEquals(31.99, distance);
    }
}
