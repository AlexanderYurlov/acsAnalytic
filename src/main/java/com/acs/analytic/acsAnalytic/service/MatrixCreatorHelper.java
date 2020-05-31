package com.acs.analytic.acsAnalytic.service;

import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.enums.Sense;

import static com.acs.analytic.acsAnalytic.model.enums.Sense.EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.LESS_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.MORE_OR_EQUAL;

public class MatrixCreatorHelper {

    public static Matrix create(int k, double chargT, double arrT, double deadlT) {

        int length = 5 * k + 3;
        // Create f array
        int[] f = new int[length];
        for (int i = 0; i < length; i++) {
            if (i >= 2 * (k + 1) && i < 3 * (k + 1)) {
                f[i] = 1;
            }
        }

        // Create arrays
        // Y-X = charge_t
        // X > arr_t
        // Y > deadl_t
        int height = 11 * k + 4;
        int k2 = k + 1;
        int[][] a = new int[height][length];
        double[] b = new double[height];
        Sense[] senses = new Sense[height];

        for (int i = 0; i < height; i++) {
            if (i < k2) {
                a[i][i] = -1;
                a[i][i + k2] = 1;
                // b
                b[i] = chargT;
                // sense
                senses[i] = EQUAL;
            } else if (i < 3 * k2) {
                a[i][i - k2] = 1;
                // b
                // sense
                if (i < 2 * k2) {
                    b[i] = arrT;
                    senses[i] = MORE_OR_EQUAL;
                } else if (i < 3 * k2) {
                    b[i] = deadlT;
                    senses[i] = LESS_OR_EQUAL;
                }
            }
            // first block
            else if (i == 3 * k2) {
                a[i][1] = 1;
                a[i][3 * k2] = -1;
                // b
                b[i] = 0;
                // sense
                senses[i] = MORE_OR_EQUAL;
            } else if (i == 3 * k2 + 1) {
                a[i][2 * k2 - 1] = -1;
                a[i][2 * k2] = -10000;
                a[i][3 * k2] = 1;
                // b
                b[i] = -10000d;
                // sense
                senses[i] = MORE_OR_EQUAL;
            } else if (i == 3 * k2 + 2) {
                a[i][2 * k2 - 1] = -1;
                a[i][3 * k2] = 1;
                // b
                b[i] = 0;
                // sense
                senses[i] = LESS_OR_EQUAL;
            } else if (i == 3 * k2 + 3) {
                a[i][2 * k2] = -10000;
                a[i][3 * k2] = 1;
                // b
                b[i] = 0;
                // sense
                senses[i] = LESS_OR_EQUAL;
            }
            // Midle block
            else if (i >= 3 * k2 + 4 && i <= height - 5 - k) {
                int dif = i - 3 * k2 - 4;
                int midleK = dif / 7 + 1;
                int midleI = dif % 7;

                // X2-Y1+V12-VN2
                if (midleI == 0) {
                    a[i][midleK] = 1;
                    a[i][k2 - 1 + midleK] = -1;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    a[i][k2 * 3 + midleK * 2] = -1;
                    // b
                    b[i] = 0;
                    // sense
                    senses[i] = MORE_OR_EQUAL;
                }
                // V12-Y1-10000*Z2
                else if (midleI == 1) {
                    a[i][k2 - 1 + midleK] = -1;
                    a[i][k2 * 2 + midleK] = -10000;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // b
                    b[i] = -10000;
                    // sense
                    senses[i] = MORE_OR_EQUAL;

                }
                // V12-Y1
                else if (midleI == 2) {
                    a[i][k2 - 1 + midleK] = -1;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // b
                    b[i] = 0;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // V12-10000*Z2
                else if (midleI == 3) {
                    a[i][k2 * 2 + midleK] = -10000;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // b
                    b[i] = 0;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // VN2-YN-10000*Z2
                else if (midleI == 4) {
                    a[i][k2 * 2 - 1] = -1;
                    a[i][k2 * 2 + midleK] = -10000;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // b
                    b[i] = -10000;
                    // sense
                    senses[i] = MORE_OR_EQUAL;
                }
                // VN2-YN
                else if (midleI == 5) {
                    a[i][k2 * 2 - 1] = -1;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // b
                    b[i] = 0;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // VN2-10000*Z2
                else if (midleI == 6) {
                    a[i][k2 * 2 + midleK] = -10000;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // b
                    b[i] = 0;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
            }
            // xn block
            else if ((i <= height - 5) && (i > height - 5 - k)) {
                a[i][k] = 1;
                int vPosition = length - 1 - 2 * ((height - 5) - i);
                a[i][vPosition] = -1;
                // b
                b[i] = 0;
                // sense
                senses[i] = MORE_OR_EQUAL;
            }
            // V1L-Y1-10000*ZL
            else if (i == height - 4) {
                a[i][k2 * 2 - 2] = -1;
                a[i][k2 * 3 - 1] = -10000;
                a[i][length - 1] = 1;
                // b
                b[i] = -10000;
                // sense
                senses[i] = MORE_OR_EQUAL;
            }
            //V1L-Y1
            else if (i == height - 3) {
                a[i][k2 * 2 - 2] = -1;
                a[i][length - 1] = 1;
                // b
                b[i] = 0;
                // sense
                senses[i] = LESS_OR_EQUAL;
            }
            //V1L-10000*ZL
            else if (i == height - 2) {
                a[i][k2 * 3 - 1] = -10000;
                a[i][length - 1] = 1;
                // b
                b[i] = 0;
                // sense
                senses[i] = LESS_OR_EQUAL;
            }
            // last string (Z)
            else if (i == height - 1) {
                a[i] = f;
                // b
                b[i] = 1;
                // sense
                senses[i] = EQUAL;
            }
        }

        return Matrix.builder()
                .a(a)
                .b(b)
                .senses(senses)
                .build();
    }

    private static void printMatrix(Matrix matrix) {

        int[][] a = matrix.getA();
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(" " + String.format("%-8s", "[" + a[i][j] + "]"));
            }
            System.out.print(" " + matrix.getSenses()[i].getCode() + " ");
            System.out.print(matrix.getB()[i]);
            System.out.println();
        }

//            for (int j = 0; j < f.length; j++) {
//                System.out.print(" [" + f[j] + "]");
//            }
    }

    public static void main(String[] args) {
        var matrix = create(2, 10d, 11.1d, 12.2d);
        printMatrix(matrix);

    }
}
