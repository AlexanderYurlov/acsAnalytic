package com.acs.analytic.acsAnalytic.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResult {

    Integer pumpId;

    boolean isReserved;

    Double time;

    List<Vehicle> combination;

    public ReservationResult(boolean reserveStatus) {
        isReserved = reserveStatus;
    }
}
