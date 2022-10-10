package de.reza.documentclassifier.pojo;

import lombok.*;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public final class Token {

    private String tokeName;

    private double xAxis;

    private double yAxis;

    private double width;
}
