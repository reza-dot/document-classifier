package de.reza.documentclassifier.classification;

import de.reza.documentclassifier.pojo.Prediction;
import de.reza.documentclassifier.pojo.Token;
import de.reza.documentclassifier.utils.EuclideanDistance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger numberOfFoundToken = new AtomicInteger();
        tokenSetOcr.forEach(token -> {
                boolean match = tokenSetClass.stream().anyMatch(tokenClass -> (
                        tokenClass.getTokeName().contains(token.getTokeName()) && EuclideanDistance.calculateDistanceBetweenPoints(tokenClass, token) <= distance
                        ));
                if (match){
                    numberOfFoundToken.incrementAndGet();
                }
        });
        return new Prediction(classname, numberOfFoundToken.get(), tokenSetClass.size());
    }
}
