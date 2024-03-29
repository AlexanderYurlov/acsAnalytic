package com.acs.analytic.acsAnalytic.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.dto.InitialDataDto;
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

    public InitialDataDto getInitialDataDto() {
        return InitialDataDto.builder()
                .name("Check_10_50,30,20_0,0,0.csv")
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(480)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(300)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(120)
                                .build()
                        )
                )
                .vehMax(1000)
                .rw(0.0f)
                .rr(1.0f)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(.5f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.3f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.2f)
                                .tierIndex(3)
                                .build()
                        )
                )
                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
                .sharablePumps(Map.of(1, 0, 2, 0, 3, 0))
                .arrivalRate(10f)
                .build();
    }

}
