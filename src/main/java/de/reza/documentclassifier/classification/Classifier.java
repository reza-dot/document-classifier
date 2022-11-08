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
     * Application of the algorithm from the bachelor thesis chapter 3.6 'Ermittlung der Tokens'.
     * @param tokenListDocument      list of recognized tokens by OCR from the given document
     * @param classname         The classname of {@tokenListClass}
     * @param tokenListClass    Included tokens in the class
     * @return                  Returns {@link Prediction}
     */
    public Prediction predict(List<Token> tokenListDocument, String classname, List<Token> tokenListClass, boolean isSearchable){

        int distance = getDistanceProfile(isSearchable);
        Map<Token, Match> foundTokens = new HashMap<>();
        Set<Token> notFoundTokens = new HashSet<>();
        log.info("Start predicting for {}", classname);
        
        tokenListClass.forEach(tokenClass -> {

            Map<Token, Match> candidateMatches = new HashMap<>();
            tokenListDocument.forEach(tokenDocument -> {
                if(tokenDocument.getTokenName().equals(tokenClass.getTokenName()) && mathUtils.round(mathUtils.euclideanDistance(tokenDocument, tokenClass)) <= distance){
                    
                    candidateMatches.put(tokenDocument, new Match(tokenDocument, tokenClass, mathUtils.round(mathUtils.euclideanDistance(tokenDocument, tokenClass))));
                }
            });

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

        if(foundTokens.containsKey(Objects.requireNonNull(nnMatch).getTokenDocument())){

            double previousEuclideanDistance = foundTokens.get(nnMatch.getTokenDocument()).getDistance();
            if(previousEuclideanDistance > nnMatch.getDistance()){

                notFoundToken.add(foundTokens.get(nnMatch.getTokenDocument()).getTokenClass());
                foundTokens.put(nnMatch.getTokenDocument(), nnMatch);
            }else{
                notFoundToken.add(nnMatch.getTokenClass());
            }
        }else{

            foundTokens.put(nnMatch.getTokenDocument(), nnMatch);
        }
    }

    /**
     * Gets the correct distance based on whether the document is searchable or not
     * @param isSearchable      searchability of the document
     * @return                  distance profile
     */
    private int getDistanceProfile(boolean isSearchable){

        return isSearchable ? maxDistance : maxDistanceOcr;
    }
}
