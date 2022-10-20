package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Comparator.comparingDouble;

@Component
@Slf4j
public class Classifier {

    @Value("${MAX_DISTANCE}")
    private int maxDistance;
    @Value("${MAX_DISTANCE_OCR}")
    private int maxDistanceOcr;

    private final MathUtils mathUtils;

    public Classifier(MathUtils mathUtils){
        this.mathUtils = mathUtils;
    }

    /**
     * Predicting a given token list based on the token list of a class.
     * @param tokenList      list of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenListClass}
     * @param tokenListClass    Included tokens in the class
     * @return                  Returns {@link Prediction}
     */
    public Prediction predict(List<Token> tokenList, String classname, List<Token> tokenListClass, boolean isSearchable){

        int distance = getDistanceProfile(isSearchable);
        HashMap<Token, Double> foundTokens = new HashMap<>();

        tokenListClass.forEach(tokenClass -> {

            HashMap<Token, Double> matches = new HashMap<>();
            tokenList.forEach(tokenPdf -> {
                if(tokenPdf.getTokenKey().equals(tokenClass.getTokenKey()) && mathUtils.euclideanDistance(tokenPdf, tokenClass) <= distance){

                    matches.put(tokenPdf, mathUtils.euclideanDistance(tokenPdf, tokenClass));
                }
            } );

            if(matches.size()!=0) {

                Token tokenWithLowestDistance= Collections.min(matches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
                double euclideanDistance = mathUtils.euclideanDistance(tokenWithLowestDistance, tokenClass);
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

    /**
     * Gets the correct distance based on whether the document is searchable or not
     * @param isSearchable      searchability of the document
     * @return                  distance profile
     */
    protected int getDistanceProfile(boolean isSearchable){
        if(isSearchable){
            return maxDistance;
        }
        else {
            return maxDistanceOcr;
        }
    }
}
