package de.reza.documentclassifier.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public final class Match {

    /**
     * A {@link Token} within a document
     */
    private Token tokenDocument;

    /**
     * A {@link Token} of a class
     */
    private Token tokenClass;

    /**
     * {@link de.reza.documentclassifier.utils.MathUtils#euclideanDistance(Token, Token)} distance between two {@link Token}
     */
    private double distance;
}
