package de.reza.documentclassifier;

import de.reza.documentclassifier.classification.Classifier;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.EuclideanDistance;
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

    @Test
    public void getTokenWithLowestDistance(){

        Token token = new Token("A", 10, 15, 10);
        HashMap<Token, Double> allMatches = new HashMap<>();
        Token firstCandidate = new Token("A", 11, 12, 10);
        allMatches.put(firstCandidate, EuclideanDistance.calculateDistanceBetweenPoints(token, firstCandidate));
        Token secondCandidate = new Token("A", 20, 25, 10);
        allMatches.put(firstCandidate, EuclideanDistance.calculateDistanceBetweenPoints(token, secondCandidate));
        Token tokenWithLowestDistance= Collections.min(allMatches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
        Assertions.assertEquals(
                EuclideanDistance.calculateDistanceBetweenPoints(token, firstCandidate),
                EuclideanDistance.calculateDistanceBetweenPoints(token, tokenWithLowestDistance)
                );


    }
}
