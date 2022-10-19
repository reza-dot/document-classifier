package de.reza.documentclassifier.pojo;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public final class Token {

    private String tokenKey;

    private double xAxis;

    private double yAxis;
}
