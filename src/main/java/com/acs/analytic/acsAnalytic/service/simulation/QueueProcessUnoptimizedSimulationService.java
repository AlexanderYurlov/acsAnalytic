package com.acs.analytic.acsAnalytic.service.simulation;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;

@Service
public class QueueProcessUnoptimizedSimulationService extends AbstractQueueProcessSimulationService {

    private final ReserveFinder reserveFinder;

    QueueProcessUnoptimizedSimulationService(@Qualifier("unoptimizedReserveFinder") ReserveFinder reserveFinder) {
        this.reserveFinder = reserveFinder;
    }

    @Override
    protected ReserveFinder getReserveFinder() {
        return reserveFinder;
    }

    /**
     * Запуск процесса симуляции
     *
     * @return
     */
    public ReportDetailsDataDto simulate(InitializedData initializedData) {

        Date startTime = new Date();

        List<Vehicle> vehicles = initializedData.getVehicles();
        TierPumpConf tierPumpConf = new TierPumpConf(initializedData.getInitialData(), initializedData.getTierPumps());

        simulateVehicles(vehicles, tierPumpConf);

        Date endTime = new Date();
        System.out.println("Total execution time: " + (endTime.getTime() - startTime.getTime()) + "ms");

        initializedData.setStatus(SimulationStatus.COMPLETED);
        initializedData.setStartTime(startTime);
        initializedData.setEndTime(endTime);
        return new ReportDetailsDataDto(initializedData, vehicles);
    }

}
