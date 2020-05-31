package com.acs.analytic.acsAnalytic.model;

import lombok.Builder;
import lombok.Data;

import com.acs.analytic.acsAnalytic.model.enums.Sense;

@Data
@Builder
public class Matrix {

    int[][] a;
    Sense[] senses;
    double[] b;

}
