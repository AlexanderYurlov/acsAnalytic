package com.acs.analytic.acsAnalytic.service.simulation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

}
