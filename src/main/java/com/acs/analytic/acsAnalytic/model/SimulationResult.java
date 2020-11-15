package com.acs.analytic.acsAnalytic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Data
public class SimulationResult {

    /**
     * авто стоящие на зарядке в данный момент
     */
    Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap;

    /**
     * авто поставленные в очередь
     */
    Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap;

    /**
     * Отказано в зарядке
     */
    List<Vehicle> rejectedVehicles;


    public SimulationResult(TierPumpConf tierPumpConf) {
        this.chargingVehiclesMap = new HashMap<>();
        this.inProgressVehiclesMap = prepareVehiclesMap(tierPumpConf);
        this.rejectedVehicles = new ArrayList<>();
    }

    private Map<Integer, Map<Integer, List<Vehicle>>> prepareVehiclesMap(TierPumpConf tierPumpConf) {
        Map<Integer, Map<Integer, List<Vehicle>>> vehiclesMap = new HashMap<>();

        Map<Integer, List<TierPump>> tierPumpsMap = tierPumpConf.getTierPumpsMap();
        Map<Integer, List<TierPump>> sharableTierPumpsMap = tierPumpConf.getSharableTierPumpsMap();

        addPumps(vehiclesMap, tierPumpsMap);
        addPumps(vehiclesMap, sharableTierPumpsMap);
        return vehiclesMap;
    }

    private void addPumps(Map<Integer, Map<Integer, List<Vehicle>>> vehiclesMap, Map<Integer, List<TierPump>> tierPumpsMap) {
        for (Integer tierId : tierPumpsMap.keySet()) {
            Map<Integer, List<Vehicle>> tierVehiclesMap = vehiclesMap.get(tierId);
            if (tierVehiclesMap == null) {
                tierVehiclesMap = new HashMap<>();
            }
            List<TierPump> pumpList = tierPumpsMap.get(tierId);
            if (pumpList != null) {
                for (TierPump pump : pumpList) {
                    tierVehiclesMap.put(pump.getId(), new ArrayList<>());
                }
            }
            vehiclesMap.put(tierId, tierVehiclesMap);
        }
    }

}
