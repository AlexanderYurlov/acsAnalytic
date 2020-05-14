package com.acs.analytic.acsAnalytic.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.Vehicle;

@Service
public class QueueProcessSimulationService {

    public List<Vehicle> simulate(List<Vehicle> vehicles, Map<Integer, List<TierPump>> tierPumpsMap) {

        for (Vehicle vehicle : vehicles) {
            Integer tierId = vehicle.getTierId();
            if (tierPumpsMap.get(tierId) != null) {
                for (TierPump tierPump : tierPumpsMap.get(tierId)) {

                }

            }

        }

        return vehicles;
    }


}
