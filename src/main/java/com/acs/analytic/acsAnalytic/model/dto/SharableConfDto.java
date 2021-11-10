package com.acs.analytic.acsAnalytic.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Data
@NoArgsConstructor
public class SharableConfDto {

    private Map<Integer, SharableConfTierDto> confPerTier = new HashMap<>();
    private Map<Integer, Integer> rejectedMap = new HashMap<>();
    private Map<Integer, Integer> utilizationMap = new HashMap<>();
    /**
     * temporary param
     */
    private List<Integer> rejectedIds;


    public SharableConfDto(TierPumpConf tpConf, List<Vehicle> vehicles) {
        putAllData(tpConf.getSharableTierPumpsMap(), true);
        putAllData(tpConf.getTierPumpsMap(), false);
        rejectedMap = calculateRejectedMap(tpConf.getTiers(), vehicles);
        utilizationMap = calculateUtilizationMap(tpConf, vehicles);
    }

    private Map<Integer, Integer> calculateUtilizationMap(TierPumpConf tpConf, List<Vehicle> vehicles) {
        Map<Integer, Double> pumpChargeTime = new HashMap<>();
        Map<Integer, Vehicle> lastVehicle = new HashMap<>();
        vehicles.forEach(v -> {
            if (v.getChargedTierId() != 0) {
                var t = v.getActComplT() - v.getActStartChargeT();
                var pumpId = v.getPumpId();
                var time = pumpChargeTime.getOrDefault(pumpId, 0d);
                pumpChargeTime.put(pumpId, time + t);
                if (lastVehicle.get(pumpId) == null || lastVehicle.get(pumpId).getActComplT() < v.getActComplT()) {
                    lastVehicle.put(pumpId, v);
                }
            }
        });
        return calculateUtilizationTime(pumpChargeTime, lastVehicle, tpConf);
    }

    private Map<Integer, Integer> calculateUtilizationTime(Map<Integer, Double> pumpChargeTime, Map<Integer, Vehicle> lastVehicle, TierPumpConf tpConf) {
        Map<Integer, List<TierPump>> tierPumpsMap = new HashMap<>();
        tpConf.getTiers().stream().map(Tier::getId).forEach(k -> {
            List<TierPump> tierPumpsList = tierPumpsMap.getOrDefault(k, new ArrayList<>());
            tierPumpsList.addAll(tpConf.getTierPumpsMap().get(k));
            tierPumpsList.addAll(tpConf.getSharableTierPumpsMap().get(k));
            tierPumpsMap.put(k, tierPumpsList);
        });

        Map<Integer, Integer> utilizationTimeMap = new HashMap<>();
        tierPumpsMap.keySet().forEach(k -> {
            Double defaultTotalTime = null;
            double workTime = 0d;
            double totalTime = 0d;
            int xDefaultTotalTime = 0;
            for (TierPump tierPump : tierPumpsMap.get(k)) {
                var pumpId = tierPump.getId();
                if (pumpChargeTime.get(pumpId) == null) {
                    xDefaultTotalTime = xDefaultTotalTime + 1;
                    continue;
                }
                workTime = workTime + pumpChargeTime.get(pumpId);
                totalTime = totalTime + lastVehicle.get(pumpId).getActComplT();
                if (defaultTotalTime == null) {
                    defaultTotalTime = lastVehicle.get(pumpId).getActComplT();
                } else {
                    defaultTotalTime = (defaultTotalTime + lastVehicle.get(pumpId).getActComplT()) / 2;
                }
            }
            if (xDefaultTotalTime != 0) {
                totalTime = totalTime + xDefaultTotalTime * defaultTotalTime;
            }
            utilizationTimeMap.put(k, (int) (workTime * 100 / totalTime));
        });
        return utilizationTimeMap;
    }

    private void putAllData(Map<Integer, List<TierPump>> tierPumpsMap, boolean isSharable) {
        for (Map.Entry<Integer, List<TierPump>> entry : tierPumpsMap.entrySet()) {
            SharableConfTierDto confTier = confPerTier.get(entry.getKey());
            if (confTier == null) {
                confTier = new SharableConfTierDto();
                confPerTier.put(entry.getKey(), confTier);
            }
            if (isSharable) {
                confTier.setSharable(entry.getValue().size());
            } else {
                confTier.setRegular(entry.getValue().size());
            }
        }
    }

    private Map<Integer, Integer> calculateRejectedMap(List<Tier> tiers, List<Vehicle> vehicles) {
        rejectedIds = new ArrayList<>();
        var rejectedMap = new HashMap<Integer, Integer>();
        tiers.forEach(t -> rejectedMap.put(t.getId(), 0));
        vehicles.forEach(v -> {
            var tierId = v.getTierId();
            if (v.getChargedTierId() == 0) {
                var rejectedByTier = rejectedMap.getOrDefault(tierId, 0);
                rejectedMap.put(tierId, ++rejectedByTier);
                rejectedIds.add(v.getId());
            }
        });
        return rejectedMap;
    }

}
