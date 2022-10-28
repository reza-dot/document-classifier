package de.reza.documentclassifier.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import java.util.Map;

@AllArgsConstructor
@Getter
@ToString
public final class Prediction {

    /**
     * The class name without file extension json
     */
    private String classname;

    /**
     * The found {@link Token} within a class
     */
    private int numberOfFoundTokens;

    /**
     * The number of {@link Token} a class has
     */
    private int numberOfTokensInClass;

    /**
     * The relative frequency from {@link #numberOfFoundTokens} / {@link #numberOfTokensInClass}
     */
    private double probability;

    /**
     * All found tokens from the document, which match the tokens from the class.
     */
    //@JsonIgnore
    private Map<Token, Match> foundTokens;

    public Prediction(String classname, int numberOfFoundTokens, int numberOfTokensInClass, Map<Token, Match>  foundTokens){
        this.classname = classname.split("\\.")[0];
        this.numberOfFoundTokens = numberOfFoundTokens;
        this.numberOfTokensInClass = numberOfTokensInClass;
        this.probability = (double) numberOfFoundTokens /(double) numberOfTokensInClass;
        this.foundTokens = foundTokens;
    }
}
