package de.reza.documentclassifier.pojo;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public final class Token {

    /**
     * A tokenized word from a PDF document
     */
    private String tokenKey;

    /**
     * X-axis of the Bounding Box
     */
    private double xAxis;

    /**
     * y-axis of the Bounding Box
     */
    private double yAxis;
}
