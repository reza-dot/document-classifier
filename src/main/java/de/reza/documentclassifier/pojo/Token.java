package de.reza.documentclassifier.pojo;

import lombok.*;

/**
 * Segmented word from a PDF document and the coordinates of the bounding box in which the segmented word is located.
 */
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
