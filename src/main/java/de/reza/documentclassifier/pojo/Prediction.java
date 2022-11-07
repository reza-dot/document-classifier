package de.reza.documentclassifier.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

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
    private int numberOfFoundTokensInPdf;

    /**
     * The number of {@link Token} a class has
     */
    private int numberOfTokensInClass;

    /**
     * The relative frequency from {@link #numberOfFoundTokensInPdf} / {@link #numberOfTokensInClass}
     */
    private double probability;

    /**
     * All found tokens within the document, which match the tokens from the class.
     */
    @JsonIgnore
    private Map<Token, Match> foundPdfTokens;

    /**
     * Set of all not found token of the class
     */
    @JsonIgnore
    private Set<Token> notFoundClassTokens;

    public Prediction(String classname, int numberOfFoundTokensInPdf, int numberOfTokensInClass, Map<Token, Match>  foundPdfTokens, Set<Token> notFoundClassTokens){
        this.classname = classname.split("\\.")[0];
        this.numberOfFoundTokensInPdf = numberOfFoundTokensInPdf;
        this.numberOfTokensInClass = numberOfTokensInClass;
        this.probability = (double) numberOfFoundTokensInPdf /(double) numberOfTokensInClass;
        this.foundPdfTokens = foundPdfTokens;
        this.notFoundClassTokens = notFoundClassTokens;
    }
}
