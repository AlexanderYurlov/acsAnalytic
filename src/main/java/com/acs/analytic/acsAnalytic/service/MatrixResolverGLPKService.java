package com.acs.analytic.acsAnalytic.service;

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

import static com.acs.analytic.acsAnalytic.model.enums.Sense.MORE_OR_EQUAL;
import static com.acs.analytic.acsAnalytic.service.MatrixCreatorHelper.printMatrix;

@Service
public class MatrixResolverGLPKService implements MatrixResolver {

    public static final Integer max = 10000000;
    public static final Integer min = -10000000;

    @Override
    public MatrixResult resolve(Matrix matrix) {
        glp_prob lp;
        glp_smcp parm;
//        SWIGTYPE_p_int ind;
//        SWIGTYPE_p_double val;
        int ret;
        try {
            // Create task
            lp = GLPK.glp_create_prob();
            GLPK.glp_set_prob_name(lp, "matrix k = " + matrix.getK());

            // Define columns
            int columns = matrix.getA()[0].length;
            GLPK.glp_add_cols(lp, columns);
            int k2 = matrix.getK() + 1;
            for (int i = 0; i < k2; i++) {
                int index = i + 1;
                // x
                GLPK.glp_set_col_name(lp, index, "x" + index);
                GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, matrix.getB()[i + k2], max);
                // y
                GLPK.glp_set_col_name(lp, k2 + index, "y" + index);
                GLPK.glp_set_col_kind(lp, k2 + index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, k2 + index, GLPKConstants.GLP_DB, 0, matrix.getB()[i + 2 * k2]);
                // z
                GLPK.glp_set_col_name(lp, 2 * k2 + index, "z" + index);
                GLPK.glp_set_col_kind(lp, 2 * k2 + index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, 2 * k2 + index, GLPKConstants.GLP_DB, 0, max);
            }

            // v
            for (int i = k2 * 3; i < matrix.getA()[0].length; i++) {
                int index = i + 1;
                GLPK.glp_set_col_name(lp, index, "v" + index);
                GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, max);
            }

            // Create constraints
            int nElements = matrix.getA()[0].length + 1;
            int nRows = matrix.getA().length - 2 * k2;
            System.out.println("nElements = " + nElements);
            System.out.println("nRows = " + nRows);

            GLPK.glp_add_rows(lp, nRows);

            // nRows: 0 - k2
            for (int i = 0; i < k2; i++) {
                int rowI = i + 1;
                GLPK.glp_set_row_name(lp, rowI, "c" + rowI);
                // Sense ==
                GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_FX, matrix.getB()[i], matrix.getB()[i]);

                int[] aRow = matrix.getA()[i];

                SWIGTYPE_p_int ind = GLPK.new_intArray(nElements);
                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.intArray_setitem(ind, index, index);
                }

                SWIGTYPE_p_double val = GLPK.new_doubleArray(nElements);
                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.doubleArray_setitem(val, index, aRow[j]);
                }
                GLPK.glp_set_mat_row(lp, rowI, columns, ind, val);
            }

            // nRows: 3 * k2 - nRows
            for (int i = 3 * k2; i < nRows; i++) {
                int rowI = i + 1;
                GLPK.glp_set_row_name(lp, rowI, "c" + rowI);

                if (matrix.getSenses()[i] == MORE_OR_EQUAL) {
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, matrix.getB()[i], max);
                } else {
                    GLPK.glp_set_row_bnds(lp, rowI, GLPKConstants.GLP_DB, min, matrix.getB()[i]);
                }

                int[] aRow = matrix.getA()[i];

                SWIGTYPE_p_int ind = GLPK.new_intArray(nElements);
                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.intArray_setitem(ind, index, index);
                }

                SWIGTYPE_p_double val = GLPK.new_doubleArray(nElements);
                for (int j = 0; j < aRow.length; j++) {
                    int index = j + 1;
                    GLPK.doubleArray_setitem(val, index, aRow[j]);
                }
                GLPK.glp_set_mat_row(lp, rowI, columns, ind, val);
            }

            // Define objective
            GLPK.glp_set_obj_name(lp, "f");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            for (int i = 0; i < matrix.getF().length; i++) {
                int rowI = i + 1;
                GLPK.glp_set_obj_coef(lp, columns, 1);
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

        var matrix = MatrixCreatorHelper.create(1, 2d, 3.1d, 12.2d);
        printMatrix(matrix);
        new MatrixResolverGLPKService().resolve(matrix);

    }

    static void write_lp_solution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;
        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_get_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        n = GLPK.glp_get_num_cols(lp);
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_get_col_prim(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }

}
