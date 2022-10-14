package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.EuclideanDistance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Comparator.comparingDouble;

@Service
@Slf4j
public class Classifier {

    @Value("${MAX_DISTANCE}")
    private int maxDistance;
    @Value("${MAX_DISTANCE_OCR}")
    private int maxDistanceOcr;

    /**
     * Predicting a given token set based on the token set of a class.
     * @param tokenSetOcr       Set of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenSetClass}
     * @param tokenSetClass     Included tokens in the class
     * @return                  Returns the relative frequency of the found tokens in the document
     */
    public Prediction predict(HashSet<Token> tokenSetOcr, String classname, HashSet<Token> tokenSetClass, boolean isReadable){

        int distance;
        if(isReadable){
            distance = maxDistance;
        }
        else {
            distance = maxDistanceOcr;
        }
        List<Token> foundTokens = new ArrayList<>();
        tokenSetClass.forEach(tokenClass -> {

            HashMap<Token, Double> allMatches = new HashMap<>();
            tokenSetOcr.forEach(tokenPdf -> {
                if(tokenPdf.getTokeName().contains(tokenClass.getTokeName()) && EuclideanDistance.calculateDistanceBetweenPoints(tokenPdf, tokenClass) <= distance){
                    allMatches.put(tokenPdf, EuclideanDistance.calculateDistanceBetweenPoints(tokenPdf, tokenClass));
                }
            } );

            if(allMatches.size()!=0) {
                Token tokenWithLowestDistance= Collections.min(allMatches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
                foundTokens.add(tokenWithLowestDistance);
            }

        });
        return new Prediction(classname, foundTokens.size(), tokenSetClass.size());
    }
}
