package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Match;
import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;

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
        Map<Token, Match> foundTokens = new HashMap<>();
        Set<Token> notFoundTokens = new HashSet<>();
        log.info("Start predicting for {}", classname);
        
        tokenListClass.forEach(tokenClass -> {

            Map<Token, Match> candidateMatches = new HashMap<>();
            tokenListPdf.forEach(tokenPdf -> {
                if(tokenPdf.getTokenName().equals(tokenClass.getTokenName()) && mathUtils.euclideanDistance(tokenPdf, tokenClass) <= distance){
                    candidateMatches.put(tokenPdf, new Match(tokenPdf, tokenClass, mathUtils.euclideanDistance(tokenPdf, tokenClass)));
                }
            } );

            if(candidateMatches.size()!=0) {
                modifiedNearestNeighborSearch(candidateMatches, foundTokens, notFoundTokens);
            }
            else {
                notFoundTokens.add(tokenClass);
            }
        });
        return new Prediction(classname, foundTokens.size(), tokenListClass.size(), foundTokens, notFoundTokens);
    }

    /**
     * Application of the algorithm from the bachelor thesis chapter 3.6 'Ermittlung der Tokens'.
     * @param candidateMatches  {@link Token} which are within a radius with identical {@link Token#getTokenName()} to the class {@link Token}
     * @param foundTokens       Already found {@link Token} with their distance to a class {@link Token}
     */
    private void modifiedNearestNeighborSearch(Map<Token, Match> candidateMatches, Map<Token, Match> foundTokens, Set<Token> notFoundToken){

        Match nnMatch= candidateMatches.values().stream().min(Comparator.comparing(Match::getDistance)).orElse(null);

        if(foundTokens.containsKey(Objects.requireNonNull(nnMatch).getTokenPdf())){

            double previousEuclideanDistance = foundTokens.get(nnMatch.getTokenPdf()).getDistance();
            if(previousEuclideanDistance > nnMatch.getDistance()){

                notFoundToken.add(foundTokens.get(nnMatch.getTokenPdf()).getTokenClass());
                foundTokens.put(nnMatch.getTokenPdf(), nnMatch);
            }else{
                notFoundToken.add(nnMatch.getTokenClass());
            }
        }else{

            foundTokens.put(nnMatch.getTokenPdf(), nnMatch);
        }
    }

    /**
     * Gets the correct distance based on whether the document is searchable or not
     * @param isSearchable      searchability of the document
     * @return                  distance profile
     */
    private int getDistanceProfile(boolean isSearchable){
        if(isSearchable){
            return maxDistance;
        }
        else {
            return maxDistanceOcr;
        }
    }
}
