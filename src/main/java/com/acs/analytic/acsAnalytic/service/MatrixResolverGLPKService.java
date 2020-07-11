package com.acs.analytic.acsAnalytic.service;

import java.util.Arrays;
import java.util.List;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;
import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.MatrixResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.model.enums.Sense.LESS_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.model.enums.Sense.MORE_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.create;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.createB;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.prepareListVehicles;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.prepareVehicle;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.printMatrix;

@Service
public class MatrixResolverGLPKService implements MatrixResolver {

    public static final Integer MAX = 10000;
    public static final Integer MIN = -10000;

    @Override
    public MatrixResult resolve(Matrix matrix, double[] b) {
        glp_prob lp;
        glp_smcp parm;
        int k2 = matrix.getK() + 1;
        int nElements = matrix.getA()[0].length;
        int nRows = matrix.getA().length - 2 * k2;

        System.out.println("nElements = " + nElements);
        System.out.println("nRows = " + nRows);

        SWIGTYPE_p_int ind  = GLPK.new_intArray(nElements);;
        SWIGTYPE_p_double val = GLPK.new_doubleArray(nElements);;
        int ret;
        try {
            // Create task
            lp = GLPK.glp_create_prob();
            GLPK.glp_set_prob_name(lp, "matrix k = " + matrix.getK());

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

            // nRows: 3 * k2 - nRows
            int rowI = 0;
            for (int i = 0; i < matrix.getA().length; i++) {
                if (i >= k2 && i < 3 * k2) {
                    continue;
                }
                rowI++;
                String name = "c" + rowI;
                GLPK.glp_set_row_name(lp, rowI, name);

                if (matrix.getSenses()[i] == MORE_OR_EQUAL) {
                    System.out.println("1 " + i);
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, b[i], MAX);
                } else if (matrix.getSenses()[i] == LESS_OR_EQUAL) {
                    System.out.println("2 " + i);
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, MIN, b[i]);
                } else {
                    System.out.println("3 " + i);
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_FX, b[i], b[i]);
                }

                int[] aRow = matrix.getA()[i];
                System.out.println("name = " + name + ", aRow = " + Arrays.toString(aRow));

                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.intArray_setitem(ind, index, index);
                }

                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.doubleArray_setitem(val, index, aRow[j]);
                }
                GLPK.glp_set_mat_row(lp, rowI, columns, ind, val);
            }

            // Define objective
            GLPK.glp_set_obj_name(lp, "f");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
//            rowI = 0;
            for (int i = k2; i < 2 * k2; i++) {
                rowI = i + 1;
                GLPK.glp_set_obj_coef(lp, rowI, 1);
            }
            // Solve model
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);

            GLPK.glp_print_mip(lp, "glp_print_mip");
            GLPK.glp_print_ipt(lp, "glp_print_ipt");
            GLPK.glp_print_sol(lp, "glp_print_sol");

            // Retrieve solution
            if (ret == 0) {
                write_lp_solution(lp);
            } else {
                System.out.println("The problem could not be solved");
            }
            // Free memory
            GLPK.glp_delete_prob(lp);
        } catch (
                GlpkException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        Vehicle veh = prepareVehicle(14.23d, List.of(151.51d), 12.59d, 249.11);
        List<Vehicle> vehicles = prepareListVehicles();
        int tierId = 0;
        var b = createB(veh, vehicles, tierId);
        var matrix = create(3);
        printMatrix(matrix, b);
        new MatrixResolverGLPKService().resolve(matrix, b);

    }

    static void write_lp_solution(glp_prob lp) {

        String name = GLPK.glp_get_obj_name(lp);
        double val = GLPK.glp_get_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        int n = GLPK.glp_get_num_cols(lp);
        for (int i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_get_col_prim(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }

}
