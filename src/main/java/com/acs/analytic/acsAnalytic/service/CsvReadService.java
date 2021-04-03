package com.acs.analytic.acsAnalytic.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.utils.UtilsCsv.readCsv;

@Service
public class CsvReadService {

    public List<Vehicle> read() {
        var vehicles = readCsv();
        return vehicles;
    }

    public InitialData getInitialData() {
        return InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(720)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(600)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(300)
                                .build()
                        )
                )
                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
                .sharablePumps(Map.of(1, 0, 2, 2, 3, 2))
                .name("test_csv")
                .vehMax(999)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(0.4f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(0.5f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(0.1f)
                                .tierIndex(3)
                                .build()
                        )
                )
                .arrivalRate(12f)
                .build();
    }

}
