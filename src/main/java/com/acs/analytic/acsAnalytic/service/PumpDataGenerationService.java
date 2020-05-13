package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.TierVehicle;

@Service
public class PumpDataGenerationService {

    public Map<Integer, List<TierPump>> generate(InitialData initialData) {

        var tiers = initialData.getTiers();
        var pumpMap = initialData.getPumpMap();
        var tetierPumpsMap = new HashMap();

        int id = 1;

        for (Tier tier : tiers) {
            if (pumpMap.get(tier.getIndex()) != null) {
                List<TierPump> tierPumpList = new ArrayList<>();
                Integer quantity = pumpMap.get(tier.getIndex());
                for (int j = 0; j < quantity; j++) {
                    TierPump tierPump = new TierPump(id++, tier);
                    tierPumpList.add(tierPump);
                }
                tetierPumpsMap.put(tier.getIndex(), tierPumpList);
            }
        }
        return tetierPumpsMap;
    }

    public static void main(String[] args) {
        InitialData initialData = InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .index(1)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(120)
                                .build(),
                        Tier.builder()
                                .index(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(300)
                                .build(),
                        Tier.builder()
                                .index(3)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(480)
                                .build()

                        )
                )
                .vehMax(1000)
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
        new PumpDataGenerationService().generate(initialData);
    }

}
