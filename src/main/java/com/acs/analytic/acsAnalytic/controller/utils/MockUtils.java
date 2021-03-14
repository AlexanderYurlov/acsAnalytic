package com.acs.analytic.acsAnalytic.controller.utils;

import java.util.List;
import java.util.Map;

import com.acs.analytic.acsAnalytic.dto.InitialDataDto;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierVehicle;

public class MockUtils {

    public static InitialDataDto getInitialDataDto() {
        return InitialDataDto.builder()
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
                .vehMax(1000)
                .rw(0.23f)
                .rr(0.77f)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(.45f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.33f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.22f)
                                .tierIndex(3)
                                .build()
                        )
                )
                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
                .sharablePumps(Map.of(1, 1, 2, 2))
                .arrivalRate(14f)
                .build();
    }
}
