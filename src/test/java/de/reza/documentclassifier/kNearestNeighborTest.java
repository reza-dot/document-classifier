package de.reza.documentclassifier;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Comparator.comparingDouble;

@SpringBootTest
public class kNearestNeighborTest {

    @Autowired
    Classifier classifier;

    @Autowired
    MathUtils mathUtils;

    @Test
    public void getTokenWithLowestDistance(){

        Token token = new Token("A", 10, 15);
        HashMap<Token, Double> allMatches = new HashMap<>();
        Token firstCandidate = new Token("A", 11, 12);
        allMatches.put(firstCandidate, mathUtils.euclideanDistance(token, firstCandidate));
        Token secondCandidate = new Token("A", 20, 25);
        allMatches.put(firstCandidate, mathUtils.euclideanDistance(token, secondCandidate));
        Token tokenWithLowestDistance= Collections.min(allMatches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
        Assertions.assertEquals(
                mathUtils.euclideanDistance(token, firstCandidate),
                mathUtils.euclideanDistance(token, tokenWithLowestDistance)
                );


    }
}
