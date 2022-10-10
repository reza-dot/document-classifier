package de.reza.documentclassifier.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class Prediction {

    private String classname;

    private int numberOfFoundTokens;

    private int numberOfTokensInClass;

    private double probability;

    public Prediction(String classname, int numberOfFoundTokens, int numberOfTokensInClass){
        this.classname = classname.split("\\.")[0];
        this.numberOfFoundTokens = numberOfFoundTokens;
        this.numberOfTokensInClass = numberOfTokensInClass;
        this.probability = (double) numberOfFoundTokens /(double) numberOfTokensInClass;
    }
}
