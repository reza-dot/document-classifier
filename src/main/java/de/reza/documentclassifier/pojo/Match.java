package de.reza.documentclassifier.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public final class Match {

    /**
     * A {@link Token} within a pdf document
     */
    private Token tokenPdf;

    /**
     * A {@link Token} of a token class
     */
    private Token tokenClass;

    /**
     * {@link de.reza.documentclassifier.utils.MathUtils#euclideanDistance(Token, Token)} distance between two {@link Token}
     */
    private double distance;
}
