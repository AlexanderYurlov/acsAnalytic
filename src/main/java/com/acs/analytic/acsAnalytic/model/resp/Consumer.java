package com.acs.analytic.acsAnalytic.model.resp;

import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Getter
@Setter
public class Consumer {

    Double startTime;
    Double endTime;
    Double powerConsumption;

    public Consumer(Vehicle vehicle) {
        startTime = vehicle.getActStartChargeT();
        endTime = vehicle.getActComplT();
    }
}
