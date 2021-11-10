package com.acs.analytic.acsAnalytic.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TierPumpConf {

    /**
     * Конфигурация разделяемые пампы
     */
    Map<Integer, List<TierPump>> sharableTierPumpsMap;

    /**
     * Конфигурация не разделяемые пампы
     */
    Map<Integer, List<TierPump>> tierPumpsMap;

    /**
     * Все уровни
     */
    List<Tier> tiers;

    public TierPumpConf(InitialData initialData, List<TierPump> tierPumps) {
        tiers = initialData.getTiers();
        sharableTierPumpsMap = new HashMap<>();
        tierPumpsMap = new HashMap<>();
        for (Tier t : initialData.getTiers()) {
            sharableTierPumpsMap.put(t.getId(), new ArrayList<>());
            tierPumpsMap.put(t.getId(), new ArrayList<>());
        }
        for (TierPump tp : tierPumps) {
            if (tp.isShareable) {
                sharableTierPumpsMap.get(tp.getTier().id).add(tp);
            } else {
                tierPumpsMap.get(tp.getTier().id).add(tp);
            }
        }
    }

    public TierPumpConf(TierPumpConf tierPumpConf, List<Tier> tiers, int tierId, int regularPump, int sharablePump) {
        this.tiers = tiers;
        Map tierPumpsMap = tierPumpConf.getTierPumpsMap() == null ? new HashMap() : tierPumpConf.getTierPumpsMap();
        setTierPumpsMap(new HashMap<>(tierPumpsMap));
        Map sharableTierPumpsMap = tierPumpConf.getSharableTierPumpsMap() == null ? new HashMap() : tierPumpConf.getSharableTierPumpsMap();
        setSharableTierPumpsMap(new HashMap<>(sharableTierPumpsMap));
        int id = lastAvailableId(tierPumpConf);
        id = putPumps(tierId, tiers, regularPump, false, id);
        putPumps(tierId, tiers, sharablePump, true, id);
    }

    private int putPumps(int tierId, List<Tier> tiers, int pumpQuantity, boolean isSharable, int id) {
        List<TierPump> tierPumpList = new ArrayList<>();
        for (int i = 1; i <= pumpQuantity; i++) {
            id++;
            tierPumpList.add(new TierPump(id, tiers.get(tierId - 1), isSharable));
        }
        if (isSharable) {
            getSharableTierPumpsMap().put(tierId, tierPumpList);
        } else {
            getTierPumpsMap().put(tierId, tierPumpList);
        }
        return id;
    }

    private int lastAvailableId(TierPumpConf tierPumpConf) {
        return Math.max(findMaxId(tierPumpConf.getTierPumpsMap()), findMaxId(tierPumpConf.getSharableTierPumpsMap()));
    }

    private Integer findMaxId(Map<Integer, List<TierPump>> pumpsMap) {
        if (CollectionUtils.isEmpty(pumpsMap)) {
            return 0;
        } else {
            int max = 1;
            for (List<TierPump> tierPumpList : pumpsMap.values()) {
                for (TierPump tierPump : tierPumpList) {
                    max = Math.max(max, tierPump.id);
                }
            }
            return max;
        }
    }

    public Boolean isSharable(int tierId, Integer pumpId) {
        if (sharableTierPumpsMap != null && sharableTierPumpsMap.get(tierId) != null) {
            for (TierPump pump : sharableTierPumpsMap.get(tierId)) {
                if (pump.getId() == pumpId) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return pumpMapToStr(tierPumpsMap) + "__\n" + pumpMapToStr(sharableTierPumpsMap);
    }

    private String pumpMapToStr(Map<Integer, List<TierPump>> sharableTierPumpsMap) {
        StringWriter sw = new StringWriter();
        sharableTierPumpsMap.keySet().forEach(
                k -> {
                    sw.append(k.toString()).append("\n");
                    sharableTierPumpsMap.get(k).forEach(tpK -> {
                        sw.append("     [" + tpK.tier.id + "-" + tpK.id + "-" + tpK.isShareable + "] \n");
                    });
                }
        );
        return sw.toString();
    }
}
