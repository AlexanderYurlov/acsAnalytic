package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.List;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

public interface ReserveFinder {

    ReservationResult tryToReserve(Vehicle veh, List<Vehicle> vehicles, double gapTime, int tierId, int pumpId, Boolean sharableState);
}
