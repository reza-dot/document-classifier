package de.reza.documentclassifier.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
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

    public Prediction(String classname, int numberOfFoundTokens, int numberOfTokensInClass){
        this.classname = classname.split("\\.")[0];
        this.numberOfFoundTokens = numberOfFoundTokens;
        this.numberOfTokensInClass = numberOfTokensInClass;
        this.probability = (double) numberOfFoundTokens /(double) numberOfTokensInClass;
    }
}
