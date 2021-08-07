package com.acs.analytic.acsAnalytic.service.simulation;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;

@Service
public class QueueProcessSimulationService extends AbstractQueueProcessSimulationService {

    private final ReserveFinder reserveFinder;

    QueueProcessSimulationService(@Qualifier("simpleReserveFinder") ReserveFinder reserveFinder) {
        this.reserveFinder = reserveFinder;
    }

    @Override
    protected ReserveFinder getReserveFinder() {
        return reserveFinder;
    }

    public void simulateSingleReport(InitializedData initializedData, SimulationStatus finalPartState) {
        Date startTime = new Date();

        List<Vehicle> vehicles = initializedData.getVehicles();
        TierPumpConf tierPumpConf = new TierPumpConf(initializedData.getInitialData(), initializedData.getTierPumps());

        simulateVehicles(vehicles, tierPumpConf);

        Date endTime = new Date();
        System.out.println("Total execution time: " + (endTime.getTime() - startTime.getTime()) + "ms");

        initializedData.setStatus(finalPartState);
        initializedData.setStartTime(startTime);
        initializedData.setEndTime(endTime);
    }

    public List<Vehicle> simulatePartOfMultiReport(InitializedData initializedData, TierPumpConf tierPumpConf) {

        System.out.println("[" + initializedData.getId() + "] Started tierPumpConf: " + tierPumpConf);

        Date startTime = new Date();

        List<Vehicle> vehicles = initializedData.getVehicles();
        simulateVehicles(vehicles, tierPumpConf);

        Date endTime = new Date();
        System.out.println("Total execution time: " + (endTime.getTime() - startTime.getTime()) + "ms");

        return vehicles;
    }

}
