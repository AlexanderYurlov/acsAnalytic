package com.acs.analytic.acsAnalytic.service;

import java.util.List;

import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.enums.Sense;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.model.enums.Sense.EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.LESS_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.MORE_OR_EQUAL;

public class MatrixCreatorHelper {

    public static final int NEGATIVE_INFINITY_INT = -100000;
    public static final double NEGATIVE_INFINITY = -100000;

    public static double[] createB(Vehicle veh, List<Vehicle> vehicles, int tierId) {

        int k = vehicles.size();
        int k2 = k + 1;
        int height = 11 * k + 4;

        double[] b = new double[height];

        for (int i = 0; i < height; i++) {
            if (i < k) {
                b[i] = vehicles.get(i).getChargT().get(tierId);
            } else if (i == k) {
                b[i] = veh.getChargT().get(tierId);
            } else if (i < 2 * k2 - 1) {
                b[i] = vehicles.get(i - k2).getEArrT();
            } else if (i == 2 * k2 - 1) {
                b[i] = veh.getEArrT();
            } else if (i < 3 * k2 - 1) {
                b[i] = vehicles.get(i - 2 * k2).getDeadlT();
            } else if (i == 3 * k2 - 1) {
                b[i] = veh.getDeadlT();
            }

            // first block
            else if (i == 3 * k2) {
                b[i] = 0;
            } else if (i == 3 * k2 + 1) {
                b[i] = NEGATIVE_INFINITY;
            } else if (i == 3 * k2 + 2) {
                b[i] = 0;
            } else if (i == 3 * k2 + 3) {
                b[i] = 0;
            }
            // Midle block
            else if (i >= 3 * k2 + 4 && i <= height - 5 - k) {
                int dif = i - 3 * k2 - 4;
                int midleI = dif % 7;

                if (midleI == 1) {
                    b[i] = NEGATIVE_INFINITY;
                } else if (midleI == 2) {
                    b[i] = 0;
                }
                // VN2-YN-10000*Z2
                else if (midleI == 4) {
                    b[i] = NEGATIVE_INFINITY;
                }
            }
            if (i == height - 4) {
                b[i] = NEGATIVE_INFINITY;
            }
            // last string (Z)
            else if (i == height - 1) {
                b[i] = 1;
            }
        }
        return b;
    }


    public static Matrix create(int k) {

        int length = 5 * k + 3;

        // Create f array
        int[] f = new int[length];
        for (int i = 0; i < length; i++) {
            if (i >= (k + 1) && i < 2 * (k + 1)) {
                f[i] = 1;
            }
        }

        // Create z array
        int[] z = new int[length];
        for (int i = 0; i < length; i++) {
            if (i >= 2 * (k + 1) && i < 3 * (k + 1)) {
                z[i] = 1;
            }
        }

        // Create arrays
        // Y-X = charge_t
        // X > arr_t
        // Y > deadl_t
        int height = 11 * k + 4;
        int k2 = k + 1;
        int[][] a = new int[height][length];
        Sense[] senses = new Sense[height];

        for (int i = 0; i < height; i++) {
            if (i < k2) {
                a[i][i] = -1;
                a[i][i + k2] = 1;
                // sense
                senses[i] = EQUAL;
            } else if (i < 3 * k2) {
                a[i][i - k2] = 1;
                // sense
                if (i < 2 * k2) {
                    senses[i] = MORE_OR_EQUAL;
                } else if (i < 3 * k2) {
                    senses[i] = LESS_OR_EQUAL;
                }
            }
            // first block
            else if (i == 3 * k2) {
                a[i][0] = 1;
                a[i][3 * k2] = -1;
                // sense
                senses[i] = MORE_OR_EQUAL;
            } else if (i == 3 * k2 + 1) {
                a[i][2 * k2 - 1] = -1;
                a[i][2 * k2] = NEGATIVE_INFINITY_INT;
                a[i][3 * k2] = 1;
                // sense
                senses[i] = MORE_OR_EQUAL;
            } else if (i == 3 * k2 + 2) {
                a[i][2 * k2 - 1] = -1;
                a[i][3 * k2] = 1;
                // sense
                senses[i] = LESS_OR_EQUAL;
            } else if (i == 3 * k2 + 3) {
                a[i][2 * k2] = NEGATIVE_INFINITY_INT;
                a[i][3 * k2] = 1;
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
                    // sense
                    senses[i] = MORE_OR_EQUAL;
                }
                // V12-Y1-10000*Z2
                else if (midleI == 1) {
                    a[i][k2 - 1 + midleK] = -1;
                    a[i][k2 * 2 + midleK] = NEGATIVE_INFINITY_INT;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // sense
                    senses[i] = MORE_OR_EQUAL;

                }
                // V12-Y1
                else if (midleI == 2) {
                    a[i][k2 - 1 + midleK] = -1;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // V12-10000*Z2
                else if (midleI == 3) {
                    a[i][k2 * 2 + midleK] = NEGATIVE_INFINITY_INT;
                    a[i][k2 * 3 - 1 + midleK * 2] = 1;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // VN2-YN-10000*Z2
                else if (midleI == 4) {
                    a[i][k2 * 2 - 1] = -1;
                    a[i][k2 * 2 + midleK] = NEGATIVE_INFINITY_INT;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // sense
                    senses[i] = MORE_OR_EQUAL;
                }
                // VN2-YN
                else if (midleI == 5) {
                    a[i][k2 * 2 - 1] = -1;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
                // VN2-10000*Z2
                else if (midleI == 6) {
                    a[i][k2 * 2 + midleK] = NEGATIVE_INFINITY_INT;
                    a[i][k2 * 3 + midleK * 2] = 1;
                    // sense
                    senses[i] = LESS_OR_EQUAL;
                }
            }
            // xn block
            else if ((i <= height - 5) && (i > height - 5 - k)) {
                a[i][k] = 1;
                int vPosition = length - 1 - 2 * ((height - 5) - i);
                a[i][vPosition] = -1;
                // sense
                senses[i] = MORE_OR_EQUAL;
            }
            // V1L-Y1-10000*ZL
            else if (i == height - 4) {
                a[i][k2 * 2 - 2] = -1;
                a[i][k2 * 3 - 1] = NEGATIVE_INFINITY_INT;
                a[i][length - 1] = 1;
                // sense
                senses[i] = MORE_OR_EQUAL;
            }
            //V1L-Y1
            else if (i == height - 3) {
                a[i][k2 * 2 - 2] = -1;
                a[i][length - 1] = 1;
                // sense
                senses[i] = LESS_OR_EQUAL;
            }
            //V1L-10000*ZL
            else if (i == height - 2) {
                a[i][k2 * 3 - 1] = NEGATIVE_INFINITY_INT;
                a[i][length - 1] = 1;
                // sense
                senses[i] = LESS_OR_EQUAL;
            }
            // last string (Z)
            else if (i == height - 1) {
                a[i] = z;
                // sense
                senses[i] = EQUAL;
            }
        }
        return Matrix.builder()
                .k(k)
                .a(a)
                .senses(senses)
                .build();
    }

    public static void printMatrix(Matrix matrix, double[] b) {

        int[][] a = matrix.getA();
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(" " + String.format("%-8s", "[" + a[i][j] + "]"));
            }
            System.out.print(" " + matrix.getSenses()[i].getCode() + " ");
            System.out.print(b[i]);
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Vehicle veh = prepareVehicle(14.23d, List.of(151.51d), 12.59d, 249.11);
        List<Vehicle> vehicles = prepareListVehicles();
        int tierId = 0;
        var b = createB(veh, vehicles, tierId);
        var matrix = create(3);
        printMatrix(matrix, b);

    }

    public static List<Vehicle> prepareListVehicles() {
        return List.of(
                prepareVehicle(0d, List.of(34.7d), 25.84d, 413.51),
                prepareVehicle(13.05d, List.of(15.6d), 23.84d, 441.55)
//                prepareVehicle(13.17d, List.of(208.2d), 19.37d, 491.43)
        );
    }

    public static Vehicle prepareVehicle(Double arrT, List<Double> chargT, Double eArrT, Double deadlT) {
        return Vehicle.builder()
                .arrT(arrT)
                .chargT(chargT)
                .eArrT(eArrT)
                .deadlT(deadlT)
                .build();
    }
}
