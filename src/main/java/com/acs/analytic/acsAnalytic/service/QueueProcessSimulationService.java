package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.MatrixResult;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Service
public class QueueProcessSimulationService {

    private final MatrixResolver matrixResolver;

    QueueProcessSimulationService(MatrixResolver matrixResolver) {
        this.matrixResolver = matrixResolver;
    }

    public List<Vehicle> simulate(List<Vehicle> vehicles, Map<Integer, List<TierPump>> tierPumpsMap) {

        Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap = prepareVehiclesMap(tierPumpsMap);
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = prepareVehiclesMap(tierPumpsMap);
        List<Integer> rejectedVehicles = new ArrayList<>();

        for (int num = 0; num < vehicles.size(); num++) {

            var vehicle = vehicles.get(num);
            int tierId = vehicle.getTierId();
            var inProgress = inProgressVehiclesMap.get(tierId);
            var processed = processedVehiclesMap.get(tierId);

            boolean isReserved = tryReserve(vehicle, inProgress, processed, tierId);
        }
        return vehicles;
    }

    private boolean tryReserve(Vehicle vehicle,
                               Map<Integer, List<Vehicle>> inProgress,
                               Map<Integer, List<Vehicle>> charged,
                               int tierId) {

        arrange(vehicle, inProgress, charged);
        boolean isReserved = tryFastReserve(vehicle, inProgress, tierId);
        if (isReserved){
            return true;
        }

        return tryNormReserve(vehicle, inProgress, tierId);
    }

    private void arrange(Vehicle vehicle, Map<Integer, List<Vehicle>> inProgress, Map<Integer, List<Vehicle>> charged) {

        var deltaTime = vehicle.getArrT();

        for (Integer pumpId : inProgress.keySet()) {
            List<Vehicle> vehicles = inProgress.get(pumpId);
            Iterator<Vehicle> i = vehicles.iterator();
            while (i.hasNext()) {
                var veh = i.next();
                var resArrT = veh.getArrT() - deltaTime;
                if (resArrT <= 0) {
                    charged.get(pumpId).add(veh);
                    i.remove();
                } else {
                    veh.setResArrT(resArrT);
                    var complT = veh.getComplT() - deltaTime;
                    veh.setActComplT(complT);
                }
            }
        }
    }

    private boolean tryFastReserve(Vehicle vehicle, Map<Integer, List<Vehicle>> inProgress, int tierId) {

        for (Integer pumpId : inProgress.keySet()) {
            var listVehicles = inProgress.get(pumpId);
            if (listVehicles.isEmpty()) {
                reserve(vehicle, listVehicles, tierId);
                return true;
            }
        }
        return false;
    }

    private boolean tryNormReserve(Vehicle veh, Map<Integer, List<Vehicle>> inProgress, int tierId) {
        for (Integer pumpId : inProgress.keySet()) {
            var vehicles = inProgress.get(pumpId);
            double [] b = MatrixCreatorHelper.createB(veh, vehicles, tierId);
            var k = vehicles.size();
            Matrix matrix = MatrixCreatorHelper.create(k);
            MatrixResult result = matrixResolver.resolve(matrix, b);
        }
        return true;
    }

    private void reserve(Vehicle veh, List<Vehicle> listVehicles, int tierId) {
        var actArrTime = veh.getEArrT();
        var chargeTime = veh.getChargT().get(tierId);
        var actComplTime = actArrTime + chargeTime;
        veh.setActArrT(actArrTime);
        veh.setActComplT(actComplTime);
        listVehicles.add(veh);
    }

    private Map<Integer, Map<Integer, List<Vehicle>>> prepareVehiclesMap(Map<Integer, List<TierPump>> tierPumpsMap) {
        Map<Integer, Map<Integer, List<Vehicle>>> vehiclesMap = new HashMap<>();
        for (Integer tierId : tierPumpsMap.keySet()) {
            Map<Integer, List<Vehicle>> tierVehiclesMap = new HashMap<>();
            List<TierPump> pumpList = tierPumpsMap.get(tierId);
            for (TierPump pump : pumpList) {
                tierVehiclesMap.put(pump.getId(), new ArrayList<>());
            }
            vehiclesMap.put(tierId, tierVehiclesMap);
        }
        return vehiclesMap;
    }

    public static void main(String[] args) {
        var queueProcessSimulationService = new QueueProcessSimulationService(new MatrixResolverGLPKService());

        InitialData initialData = InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(120)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(300)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(480)
                                .build()

                        )
                )
                .vehMax(3)
                .rw(0.23f)
                .rr(0.77f)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(.22f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.33f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.45f)
                                .tierIndex(3)
                                .build()
                        )
                )
//                .n(100)
//                .pumpTotal(3)
                .pumpMap(Map.of(1, 3, 2, 7, 3, 10))
//                .sharablePumps()
                .arrivalRate(12f)
//                .timeGeneration()
//                .n(11)
                .build();
        var vehicles = new VehicleDataGenerationService().generate(initialData);
        var tierPumpsMap = new PumpDataGenerationService().generate(initialData);

        queueProcessSimulationService.simulate(vehicles, tierPumpsMap);

    }

}
