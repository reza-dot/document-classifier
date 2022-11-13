package de.reza.documentclassifier.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.reza.documentclassifier.utils.MathUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

/**
 * Includes the classification of the document for a given class
 * The attributes {@link Prediction#foundDocumentTokens} and {@link Prediction#notFoundClassTokens}
 * attributes can be commented out in order to obtain this information in the JSON response.
 */
@Getter
@ToString
public final class Prediction {

    /**
     * The class name without file extension json
     */
    private final String classname;

    /**
     * The found {@link Token} within a class
     */
    private final int numberOfFoundTokensInPdf;

    /**
     * The number of {@link Token} a class has
     */
    private final int numberOfTokensInClass;

    /**
     * The relative frequency from {@link #numberOfFoundTokensInPdf} / {@link #numberOfTokensInClass}
     */
    private final double probability;

    /**
     * All found tokens within the document, which match the tokens from the class.
     */
    @JsonIgnore
    private final Map<Token, Match> foundDocumentTokens;

    /**
     * Set of all not found token of the class.
     */
    @JsonIgnore
    private final Set<Token> notFoundClassTokens;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private final MathUtils mathUtils = new MathUtils();

    public Prediction(String classname, int numberOfFoundTokensInPdf, int numberOfTokensInClass, Map<Token, Match>  foundDocumentTokens, Set<Token> notFoundClassTokens){
        this.classname = classname.split("\\.")[0];
        this.numberOfFoundTokensInPdf = numberOfFoundTokensInPdf;
        this.numberOfTokensInClass = numberOfTokensInClass;
        this.probability =  mathUtils.round((double) numberOfFoundTokensInPdf /(double) numberOfTokensInClass);
        this.foundDocumentTokens = foundDocumentTokens;
        this.notFoundClassTokens = notFoundClassTokens;
    }
}
