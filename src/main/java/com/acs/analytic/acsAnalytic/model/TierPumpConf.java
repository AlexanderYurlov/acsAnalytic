package com.acs.analytic.acsAnalytic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
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

    public TierPumpConf(InitialData initialData, List<TierPump> tierPumps) {
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
}
