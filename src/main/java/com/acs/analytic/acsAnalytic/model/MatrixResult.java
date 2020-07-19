package com.acs.analytic.acsAnalytic.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MatrixResult {

    boolean isResolved;

    public static MatrixResult isNotResolved() {
        return new MatrixResult(false);
    }
}
