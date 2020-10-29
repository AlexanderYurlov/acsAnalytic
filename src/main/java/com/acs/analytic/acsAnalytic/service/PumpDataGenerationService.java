package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.TierVehicle;

@Service
public class PumpDataGenerationService {

    public TierPumpConf generate(InitialData initialData) {

        var tiers = initialData.getTiers();
        validate(initialData);
        var sharablePumpMap = initialData.getSharablePumps();
        var pumpMap = initialData.getPumpMap();

        if (pumpMap == null) {
            pumpMap = new HashMap<>();
        }
        if (sharablePumpMap == null) {
            sharablePumpMap = new HashMap<>();
        }

        Map sharableTierPumpsMap = new HashMap<>();
        Map tierPumpsMap = new HashMap<>();

        int id = 1;
        for (Tier tier : tiers) {
            if (pumpMap.get(tier.getId()) != null) {

                List<TierPump> tierPumpList = getPumpList(pumpMap, tier, id);
                id = getLastId(tierPumpList, id);
                tierPumpsMap.put(tier.getId(), tierPumpList);
            }
            if (sharablePumpMap.get(tier.getId()) != null) {
                List<TierPump> sharableTierPumpList = getPumpList(sharablePumpMap, tier, id);
                sharableTierPumpsMap.put(tier.getId(), sharableTierPumpList);
                id = getLastId(sharableTierPumpList, id);

            }

        }

//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(new TierPumpConf(sharableTierPumpsMap, tierPumpsMap)));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        return new TierPumpConf(sharableTierPumpsMap, tierPumpsMap);
    }

    private void validate(InitialData initialData) {
        if (initialData.getPumpMap() == null && initialData.getSharablePumps() == null) {
            throw new ValidationException("Incorrect InitData");
        }
    }

    private int getLastId(List<TierPump> pumpList, int id) {
        if (!CollectionUtils.isEmpty(pumpList)) {
            id = pumpList.get(pumpList.size() - 1).getId() + 1;
        }
        return id;
    }

    private List<TierPump> getPumpList(Map<Integer, Integer> pumpMap, Tier tier, int id) {
        List<TierPump> tierPumpList = new ArrayList<>();
        Integer quantity = pumpMap.get(tier.getId());
        if (quantity != null) {
            for (int j = 0; j < quantity; j++) {
                TierPump tierPump = new TierPump(id++, tier);
                tierPumpList.add(tierPump);
            }
        }
        return tierPumpList;
    }


    public static void main(String[] args) {
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
                .sharablePumps(Map.of(1, 2, 2, 3))
                .arrivalRate(12f)
//                .timeGeneration()
//                .n(11)
                .build();
        new PumpDataGenerationService().generate(initialData);
    }

}
