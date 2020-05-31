package com.acs.analytic.acsAnalytic.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Matrix;
import com.acs.analytic.acsAnalytic.model.MatrixResult;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.Vehicle;

@Service
public class QueueProcessSimulationService {

    private final MatrixResolver matrixResolver;

    QueueProcessSimulationService(MatrixResolver matrixResolver) {
        this.matrixResolver = matrixResolver;
    }

    public List<Vehicle> simulate(List<Vehicle> vehicles, Map<Integer, List<TierPump>> tierPumpsMap) {

        for (int num = 0; num < vehicles.size(); num++) {

            // TODO
            int k = num + 1;
            var vehicle = vehicles.get(num);
            for (Double chargeT : vehicle.getChargT()) {
                Matrix matrix = MatrixCreatorHelper.create(k, chargeT, vehicle.getArrT(), vehicle.getDeadlT());
                MatrixResult result = matrixResolver.resolve(matrix);
                // TODO
            }
        }
        // TODO

        return vehicles;
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
