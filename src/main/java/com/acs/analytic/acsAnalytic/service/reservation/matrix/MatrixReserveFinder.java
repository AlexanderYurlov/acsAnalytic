//package com.acs.analytic.acsAnalytic.service.reservation.matrix;
//
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import com.acs.analytic.acsAnalytic.model.Matrix;
//import com.acs.analytic.acsAnalytic.model.ReservationResult;
//import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
//import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;
//
//@Service
//public class MatrixReserveFinder implements ReserveFinder {
//
//    private final MatrixResolver matrixResolver;
//
//    public MatrixReserveFinder(MatrixResolver matrixResolver) {
//        this.matrixResolver = matrixResolver;
//    }
//
//    @Override
//    public ReservationResult tryToReserve(Vehicle veh, List<Vehicle> vehicles, double chargingVeh, int tierId, int pumpId) {
//        double[] b = MatrixCreatorHelper.createB(veh, vehicles, tierId);
//        var k = vehicles.size();
//        Matrix matrix = MatrixCreatorHelper.create(k);
//        ReservationResult result = matrixResolver.resolve(matrix, b);
//        return result;
//    }
//}
