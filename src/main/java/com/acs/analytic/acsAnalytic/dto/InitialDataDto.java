package com.acs.analytic.acsAnalytic.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierVehicle;

/**
 * Used for InitialData entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitialDataDto {

    /**
     * Common settings
     */
    private String name;
    private Boolean isFullReport;

    /**
     * Common Data (Tiers Data) is actual for AutoTraffic
     */
    /**
     * Tiers for Pump and Vehicle
     */
    private List<Tier> tiers;

    /**
     * ACS Data
     */

    /**
     * Numbers of pumps per tier: Pump1, …, PumpN;
     */
    private Map<Integer, Integer> pumpMap;

    /**
     * Numbers of sharable pumps per tier: PS1, …, PSN;
     */
    private Map<Integer, Integer> sharablePumps;

    /**
     * AutoTraffic
     */

    /**
     * Charging requests’ arrival rate
     * константа зависит от города/мегаполиса/местности
     */
    private Float arrivalRate;

    /**
     * типы зарядок автомобилей и их соотношение - R ( сумма = 1)
     */
    private List<TierVehicle> r;

    private String rStr;

    /**
     * Walk-in client ratio (RW + RR = 1)
     */
    private Float rw;

    /**
     * Reservation requests ratio (RW + RR = 1)
     */
    private Float rr;

    /**
     * Maximum number of simulated: Vehmax.
     * Или может быть расчитана из maximum timeGeneration
     */
    private Integer vehMax;

    /**
     * step (delta) for rejectedReport difference between sharable and regular pumps for simulations
     */
    private Float rejectedReportDelta;

}
