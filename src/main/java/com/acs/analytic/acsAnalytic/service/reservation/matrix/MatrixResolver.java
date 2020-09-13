package com.acs.analytic.acsAnalytic.service.reservation.matrix;

import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.ReservationResult;

public interface MatrixResolver {

    ReservationResult resolve(Matrix matrix, double[] b);

}
