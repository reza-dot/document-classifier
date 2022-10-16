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
     * @param tokenListOcr       Set of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenListClass}
     * @param tokenListClass    Included tokens in the class
     * @return                  Returns the relative frequency of the found tokens in the document
     */
    public Prediction predict(List<Token> tokenListOcr, String classname, List<Token> tokenListClass, boolean isReadable){

        int distance = getDistanceProfile(isReadable);
        HashMap<Token, Double> foundTokens = new HashMap<>();

        tokenListClass.forEach(tokenClass -> {

            HashMap<Token, Double> matches = new HashMap<>();
            tokenListOcr.forEach(tokenPdf -> {
                if(tokenPdf.getTokeName().equals(tokenClass.getTokeName()) && EuclideanDistance.calculateDistanceBetweenPoints(tokenPdf, tokenClass) <= distance){

                    matches.put(tokenPdf, EuclideanDistance.calculateDistanceBetweenPoints(tokenPdf, tokenClass));
                }
            } );

            if(matches.size()!=0) {

                Token tokenWithLowestDistance= Collections.min(matches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
                double euclideanDistance = EuclideanDistance.calculateDistanceBetweenPoints(tokenWithLowestDistance, tokenClass);
                if(foundTokens.containsKey(tokenWithLowestDistance)){
                    double distanceToken = foundTokens.get(tokenWithLowestDistance);
                    if(distanceToken > euclideanDistance){
                        foundTokens.put(tokenWithLowestDistance, euclideanDistance);
                    }
                }else {
                    foundTokens.put(tokenWithLowestDistance, euclideanDistance);
                }
            }
        });
        return new Prediction(classname, foundTokens.size(), tokenListClass.size());
    }

    protected int getDistanceProfile(boolean isReadable){
        if(isReadable){
            return maxDistance;
        }
        else {
            return maxDistanceOcr;
        }
    }
}
