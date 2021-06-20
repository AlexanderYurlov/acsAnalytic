package com.acs.analytic.acsAnalytic.service.simulation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
}
