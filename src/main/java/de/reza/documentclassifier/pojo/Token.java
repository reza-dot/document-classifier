package de.reza.documentclassifier.pojo;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
public final class Token {

    /**
     * A tokenized word from a document
     */
    private String tokenName;

    /**
     * X-axis of the Bounding Box
     */
    private double xAxis;

    /**
     * y-axis of the Bounding Box
     */
    private double yAxis;
}
