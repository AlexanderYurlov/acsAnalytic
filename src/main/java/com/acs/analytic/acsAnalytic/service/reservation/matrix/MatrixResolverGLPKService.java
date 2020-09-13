package com.acs.analytic.acsAnalytic.service.reservation.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.model.enums.Sense.LESS_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.MORE_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.create;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.createB;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareListVehicles;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareVehicle;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.printMatrix;
import static org.gnu.glpk.GLPKConstants.GLP_ON;

@Service
public class MatrixResolverGLPKService implements MatrixResolver {

    public static final Integer MAX = 100000;
    public static final Integer MIN = -100000;

    @Override
    public ReservationResult resolve(Matrix matrix, double[] b) {
        glp_prob lp;
        glp_iocp parm;

        // Solve model
        parm = new glp_iocp();
        GLPK.glp_init_iocp(parm);
        parm.setPresolve(GLP_ON);
        // Create task
        lp = GLPK.glp_create_prob();
        GLPK.glp_set_prob_name(lp, "K" + matrix.getK());

        int k2 = matrix.getK() + 1;
        int nElements = matrix.getA()[0].length;
        int nRows = matrix.getA().length - 2 * k2;

        try {

            // Define columns
            int columns = matrix.getA()[0].length;
            GLPK.glp_add_cols(lp, columns);
            for (int i = 0; i < k2; i++) {
                int index = i + 1;
                // x
                GLPK.glp_set_col_name(lp, index, "x" + index);
                GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, b[i + k2], MAX);
                // y
                GLPK.glp_set_col_name(lp, k2 + index, "y" + index);
                GLPK.glp_set_col_kind(lp, k2 + index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, k2 + index, GLPKConstants.GLP_DB, 0, b[i + 2 * k2]);
                // z
                GLPK.glp_set_col_name(lp, 2 * k2 + index, "z" + index);
                GLPK.glp_set_col_kind(lp, 2 * k2 + index, GLPKConstants.GLP_IV);
                GLPK.glp_set_col_bnds(lp, 2 * k2 + index, GLPKConstants.GLP_DB, 0, 1);
            }

            // v
            for (int i = k2 * 3; i < matrix.getA()[0].length; i++) {
                int index = i + 1;
                GLPK.glp_set_col_name(lp, index, "v" + index);
                GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, MAX);
            }

            // Create constraints

            GLPK.glp_add_rows(lp, nRows);

            List<Integer> ia = new ArrayList<>();
            List<Integer> ja = new ArrayList<>();
            List<Double> ar = new ArrayList<>();

            int rowI = 0;
            // nRows: 3 * k2 - nRows
            for (int i = 0; i < matrix.getA().length; i++) {
                if (i >= k2 && i < 3 * k2) {
                    continue;
                }
//                SWIGTYPE_p_int ind = GLPK.new_intArray(nElements);
//                SWIGTYPE_p_double val = GLPK.new_doubleArray(nElements);
                rowI++;
                String name = "c" + rowI;
                GLPK.glp_set_row_name(lp, rowI, name);

                if (matrix.getSenses()[i] == MORE_OR_EQUAL) {
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, b[i], MAX);
                } else if (matrix.getSenses()[i] == LESS_OR_EQUAL) {
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, MIN, b[i]);
                } else {
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_FX, b[i], b[i]);
                }

                int[] aRow = matrix.getA()[i];
                System.out.println("name = " + name + ", aRow = " + Arrays.toString(aRow));

                for (int j = 0; j < aRow.length; j++) {
                    if (aRow[j] != 0) {
                        ia.add(rowI);
                        ja.add(j + 1);
                        ar.add((double) aRow[j]);
                    }
                }

//                for (int j = 0; j < aRow.length; j++) {
//                    int index = j + 1;
//                    GLPK.intArray_setitem(ind, index, index);
//                }
//
//                for (int j = 0; j < nElements; j++) {
//                    int index = j + 1;
//                    GLPK.doubleArray_setitem(val, index, aRow[j]);
//                }
//                GLPK.glp_set_mat_row(lp, rowI, columns, ind, val);
            }
            SWIGTYPE_p_int arrayIa = toIntArray(ia);
            SWIGTYPE_p_int arrayJa = toIntArray(ja);
            SWIGTYPE_p_double arrayAr = toDoubleArray(ar);

            GLPK.glp_load_matrix(lp, ia.size(), arrayIa, arrayJa, arrayAr);

            // Define objective
            GLPK.glp_set_obj_name(lp, "f");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            for (int i = 0; i < 2 * k2; i++) {
                int index = i + 1;
                if (i >= k2 && i < 2 * k2) {
                    GLPK.glp_set_obj_coef(lp, index, 1);
                }
//                else {
//                    GLPK.glp_set_obj_coef(lp, index, 0);
//                    System.out.println(0);
//                }
            }

            GLPK.glp_print_mip(lp, "glp_print_mip");
            GLPK.glp_print_ipt(lp, "glp_print_ipt");
            GLPK.glp_print_sol(lp, "glp_print_sol");

            int err = GLPK.glp_intopt(lp, parm);
            System.out.println("err = " + err);
            // Retrieve solution
//            if (err == 0) {
            write_lp_solution(lp);
//            } else {
//                System.out.println("The problem could not be solved. Err code = " + err);
//                return MatrixResult.isNotResolved();
//            }
            // Free memory
            GLPK.glp_delete_prob(lp);
        } catch (
                GlpkException ex) {
            ex.printStackTrace();
        }
        return new ReservationResult();
    }

    private SWIGTYPE_p_int toIntArray(List<Integer> list) {
        int arrSize = list.size();
        SWIGTYPE_p_int array = GLPK.new_intArray(arrSize);
        for (int i = 0; i < arrSize; i++) {
            GLPK.intArray_setitem(array, i + 1, list.get(i));
        }
        return array;
    }

    private SWIGTYPE_p_double toDoubleArray(List<Double> list) {
        int arrSize = list.size();
        SWIGTYPE_p_double array = GLPK.new_doubleArray(arrSize);
        for (int i = 0; i < arrSize; i++) {
            GLPK.doubleArray_setitem(array, i + 1, list.get(i));
        }
        return array;
    }

    static void write_lp_solution(glp_prob lp) {

//        String name = GLPK.glp_get_obj_name(lp);
//        double val = GLPK.glp_get_obj_val(lp);
//        System.out.print(name);
//        System.out.print(" = ");
//        System.out.println(val);
        int n = GLPK.glp_get_num_cols(lp);
        for (int i = 1; i <= n; i++) {
            String name = GLPK.glp_get_col_name(lp, i);
            double val = GLPK.glp_mip_col_val(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }

//    public static void main(String[] args) {
//
//        Vehicle veh = prepareVehicle(14.23d, List.of(151.51d), 12.59d, 249.11);
//        List<Vehicle> vehicles = prepareListVehicles();
//        int tierId = 0;
//        var b = createB(veh, vehicles, tierId);
//        var matrix = create(2);
//        printMatrix(matrix, b);
//        MatrixResolver matrixResolver = new MatrixResolverGLPKService();
//        matrixResolver.resolve(matrix, b);
//    }

}
