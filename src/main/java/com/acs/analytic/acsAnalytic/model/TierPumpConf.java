package com.acs.analytic.acsAnalytic.model;

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

    public Boolean isSharable(int tierId, Integer pumpId) {
        //TODO
        if (sharableTierPumpsMap.get(tierId) != null && sharableTierPumpsMap.get(pumpId) != null) {
            return true;
        }
        return false;
    }
}
