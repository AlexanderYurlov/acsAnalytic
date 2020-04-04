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
public class PumpDataInitialization {

    /**
     * Количество уровней
     */
    Integer n;

    /**
     * The total number of pumps
     */
    Integer pump_tot;

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
