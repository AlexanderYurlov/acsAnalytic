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
    private Map<Integer, Double> utilizationMap = new HashMap<>();
    /**
     * temporary param
     */
    private List<Integer> rejectedIds;


    public SharableConfDto(TierPumpConf tpConf, List<Vehicle> vehicles) {
        putAllData(tpConf.getSharableTierPumpsMap(), true);
        putAllData(tpConf.getTierPumpsMap(), false);
        rejectedMap = calculateRejectedMap(tpConf.getTiers(), vehicles);
        utilizationMap = calculateUtilizationMap(vehicles);
    }

    private Map<Integer, Double> calculateUtilizationMap(List<Vehicle> vehicles) {
        return null;
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
