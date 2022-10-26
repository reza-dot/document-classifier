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
     * @param tokenListPdf      list of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenListClass}
     * @param tokenListClass    Included tokens in the class
     * @return                  Returns {@link Prediction}
     */
    public Prediction predict(List<Token> tokenListPdf, String classname, List<Token> tokenListClass, boolean isSearchable){

        int distance = getDistanceProfile(isSearchable);
        Map<Token, Double> foundTokens = new HashMap<>();
        log.info("Start predicting for {}", classname);
        
        tokenListClass.forEach(tokenClass -> {

            Map<Token, Double> matches = new HashMap<>();
            tokenListPdf.forEach(tokenPdf -> {
                if(tokenPdf.getTokenName().equals(tokenClass.getTokenName()) && mathUtils.euclideanDistance(tokenPdf, tokenClass) <= distance){

                    matches.put(tokenPdf, mathUtils.euclideanDistance(tokenPdf, tokenClass));
                }
            } );

            if(matches.size()!=0) {

                Token nnTokenPdf= Collections.min(matches.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();
                double newEuclideanDistance = mathUtils.euclideanDistance(nnTokenPdf, tokenClass);
                if(foundTokens.containsKey(nnTokenPdf)){
                    double distanceFromPreviousNnTokenPdf = foundTokens.get(nnTokenPdf);
                    if(distanceFromPreviousNnTokenPdf > newEuclideanDistance){
                        foundTokens.put(nnTokenPdf, newEuclideanDistance);
                    }
                }else {
                    foundTokens.put(nnTokenPdf, newEuclideanDistance);
                }
            }
        });
        return new Prediction(classname, foundTokens.size(), tokenListClass.size(), foundTokens);
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
