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

    public List<TierPumpConf> generateList(InitialData initialData) {
        var tiers = initialData.getTiers();
        validate(initialData);
        var delta = initialData.getRejectedReportDelta();
        var total = initialData.getTotalPumpMap();
        var deltaMap = new HashMap<Integer, Integer>();
        for (Integer tierId : total.keySet()) {
            if (tierId.equals(0)) {
                //zero tier has no sharable pumps
                continue;
            }
            Integer intDelta = Math.round(total.get(tierId) * delta);
            deltaMap.put(tierId, intDelta);

        }
        List<TierPumpConf> tierPumpConfs = generateTierPumpConfs(tiers, deltaMap, total);
        return tierPumpConfs;
    }

    private List<TierPumpConf> generateTierPumpConfs(List<Tier> tiers, HashMap<Integer, Integer> deltaMap, Map<Integer, Integer> total) {
        List<TierPumpConf> tierPumpConfs = new ArrayList<>();
        for (Tier tier : tiers) {
            List<TierPumpConf> tierPumpConfsTemp = new ArrayList<>();
            var tierId = tier.getId();
            if (tierId == 1) {
                //first
                TierPumpConf tierPumpConf = new TierPumpConf(new TierPumpConf(), tiers, tierId, total.get(tierId), 0);
                tierPumpConfs.add(tierPumpConf);
                continue;
            }
            var sharablePumpByCurrentId = 0;
            var regularPump = total.get(tierId);
            while (sharablePumpByCurrentId <= total.get(tierId)) {
                for (TierPumpConf tierPumpConf : tierPumpConfs) {
                    tierPumpConfsTemp.add(new TierPumpConf(tierPumpConf, tiers, tierId, regularPump, sharablePumpByCurrentId));
                }
                sharablePumpByCurrentId = sharablePumpByCurrentId + deltaMap.get(tierId);
                regularPump = total.get(tierId) - sharablePumpByCurrentId;
            }
            tierPumpConfs = tierPumpConfsTemp;
        }
        return tierPumpConfs;
    }

    public TierPumpConf generate(InitialData initialData) {

        validate(initialData);
        var sharablePumpMap = initialData.getSharablePumps();
        var pumpMap = initialData.getPumpMap();

        Map sharableTierPumpsMap = new HashMap<>();
        Map tierPumpsMap = new HashMap<>();

        return fillPumps(initialData.getTiers(), sharablePumpMap, pumpMap, sharableTierPumpsMap, tierPumpsMap);

//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(new TierPumpConf(sharableTierPumpsMap, tierPumpsMap)));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

    }

    private TierPumpConf fillPumps(List<Tier> tiers, Map<Integer, Integer> sharablePumpMap, Map<Integer, Integer> pumpMap, Map sharableTierPumpsMap, Map tierPumpsMap) {

        if (pumpMap == null) {
            pumpMap = new HashMap<>();
        }
        if (sharablePumpMap == null) {
            sharablePumpMap = new HashMap<>();
        }

        int id = 1;
        for (Tier tier : tiers) {
            if (pumpMap.get(tier.getId()) != null) {
                List<TierPump> tierPumpList = getPumpList(pumpMap, tier, id, false);
                id = getLastId(tierPumpList, id);
                tierPumpsMap.put(tier.getId(), tierPumpList);
            }
            if (sharablePumpMap.get(tier.getId()) != null) {
                List<TierPump> sharableTierPumpList = getPumpList(sharablePumpMap, tier, id, true);
                sharableTierPumpsMap.put(tier.getId(), sharableTierPumpList);
                id = getLastId(sharableTierPumpList, id);

            }
        }
        System.out.println(tierPumpsMap);
        System.out.println(sharableTierPumpsMap);
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

    private List<TierPump> getPumpList(Map<Integer, Integer> pumpMap, Tier tier, int id, Boolean isShareable) {
        List<TierPump> tierPumpList = new ArrayList<>();
        Integer quantity = pumpMap.get(tier.getId());
        if (quantity != null) {
            for (int j = 0; j < quantity; j++) {
                TierPump tierPump = new TierPump(id++, tier, isShareable);
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
                .rejectedReportDelta(.1f)
//                .n(100)
//                .pumpTotal(3)
                .pumpMap      (Map.of(1, 13, 2, 6, 3, 3))
                .sharablePumps(Map.of(2, 3, 3, 2))
                .totalPumpMap (Map.of(1, 13, 2, 9, 3, 5))
                .arrivalRate(12f)
//                .timeGeneration()
//                .n(11)
                .build();
//        TierPumpConf tierPumpConf = new PumpDataGenerationService().generate(initialData);
//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(tierPumpConf.getTierPumpsMap()));
//            System.out.println(new ObjectMapper().writeValueAsString(tierPumpConf.getSharableTierPumpsMap()));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        List<TierPumpConf> tierPumpConfs = new PumpDataGenerationService().generateList(initialData);
        tierPumpConfs.forEach(t-> System.out.println(t + "____________"));
//        System.out.println(tierPumpConf.getTierPumpsMap().hashCode());
//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(tierPumpConfs));
//            System.out.println(new ObjectMapper().writeValueAsString(tierPumpConfs.getSharableTierPumpsMap()));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
    }

}
