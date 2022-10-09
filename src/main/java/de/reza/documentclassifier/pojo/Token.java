package de.reza.documentclassifier.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Token {

    private String tokeName;

    private double xAxis;

    private double yAxis;

    private double width;
}
