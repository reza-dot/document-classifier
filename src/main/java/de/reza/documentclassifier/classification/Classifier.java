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
     * @param tokenListDocument         list of recognized tokens by OCR from the given document
     * @param classname                 The classname of {@tokenListClass}
     * @param tokenListClass            Included tokens in the class
     * @return                          Returns {@link Prediction}
     */
    public Prediction predict(List<Token> tokenListDocument, String classname, List<Token> tokenListClass, boolean isSearchable){

        int distance = getDistanceProfile(isSearchable);
        Map<Token, Match> foundDocumentToken = new HashMap<>();
        Set<Token> notFoundClassTokens = new HashSet<>();
        log.info("Start predicting for {}", classname);
        
        tokenListClass.forEach(tokenClass -> {

            Map<Token, Match> candidateMatches = new HashMap<>();
            tokenListDocument.forEach(tokenDocument -> {
                if(tokenDocument.getTokenName().equals(tokenClass.getTokenName()) && mathUtils.round(mathUtils.euclideanDistance(tokenDocument, tokenClass)) <= distance){
                    
                    candidateMatches.put(tokenDocument, new Match(tokenDocument, tokenClass, mathUtils.round(mathUtils.euclideanDistance(tokenDocument, tokenClass))));
                }
            });

            if(candidateMatches.size()!=0) {
                modifiedNearestNeighborSearch(candidateMatches, foundDocumentToken, notFoundClassTokens);
            }
            else {
                notFoundClassTokens.add(tokenClass);
            }
        });
        return new Prediction(classname, foundDocumentToken.size(), tokenListClass.size(), foundDocumentToken, notFoundClassTokens);
    }

    /**
     * Application of the algorithm from the bachelor thesis chapter 3.5 'Ermittlung der Tokens'.
     * @param candidateMatches          document {@link Token} which are within a radius with identical {@link Token#getTokenName()} to a class {@link Token}
     * @param foundDocumentToken        found document {@link Token} with corresponding {@link Match}
     */
    private void modifiedNearestNeighborSearch(Map<Token, Match> candidateMatches, Map<Token, Match> foundDocumentToken, Set<Token> notFoundToken){

        Match nnMatch= candidateMatches.values().stream().min(Comparator.comparing(Match::getDistance)).orElse(null);

        if(foundDocumentToken.containsKey(Objects.requireNonNull(nnMatch).getTokenDocument())){

            double previousEuclideanDistance = foundDocumentToken.get(nnMatch.getTokenDocument()).getDistance();
            if(previousEuclideanDistance > nnMatch.getDistance()){

                notFoundToken.add(foundDocumentToken.get(nnMatch.getTokenDocument()).getTokenClass());
                foundDocumentToken.put(nnMatch.getTokenDocument(), nnMatch);
            }else{
                notFoundToken.add(nnMatch.getTokenClass());
            }
        }else{

            foundDocumentToken.put(nnMatch.getTokenDocument(), nnMatch);
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
