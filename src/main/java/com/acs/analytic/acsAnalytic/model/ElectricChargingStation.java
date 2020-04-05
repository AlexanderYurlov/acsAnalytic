package com.acs.analytic.acsAnalytic.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectricChargingStation {

    /**
     * Количество уровней зарядки (т)
     */
    Integer n;

    /**
     * The total number of pumps (pump_tot)
     */
    Integer pumpTotal;

    /**
     * Numbers of pumps per tier: Pump1, …, PumpN;
     */
    Map<Integer, Integer> pumpMap;

    /**
     * Numbers of sharable pumps per tier: PS1, …, PSN;
     * TODO Это конкретные номера пампов в системе?
     */
    List<Integer> sharablePumps;

}
