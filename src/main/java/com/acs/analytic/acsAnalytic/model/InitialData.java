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
public class InitialData {

    /**
     * Common Data (Tiers Data) is actual for AutoTraffic
     */
    /**
     * Tiers for Pump and Vehicle
     */
    List<Tier> tiers;


    /**
     * ACS Data
     */

    /**
     * Numbers of pumps per tier: Pump1, …, PumpN;
     */
    Map<Integer, Integer> pumpMap;

    /**
     * Numbers of sharable pumps per tier: PS1, …, PSN;
     * TODO Это конкретные номера пампов в системе?
     */
    List<Integer> sharablePumps;


    /**
     * AutoTraffic
     */

    /**
     * Charging requests’ arrival rate
     * константа зависит от города/мегаполиса/местности
     */
    Float arrivalRate;

    /**
     * типы зарядок автомобилей и их соотношение - R ( сумма = 1)
     */
    List<TierVehicle> r;

    /**
     * Walk-in client ratio (RW + RR = 1)
     */
    Float rw;

    /**
     * Reservation requests ratio (RW + RR = 1)
     */
    Float rr;

    /**
     * Maximum number of simulated: Vehmax.
     * Или может быть расчитана из maximum timeGeneration
     */
    Integer vehMax;

    public Tier getTierByIndex(Integer tierIndex) {
        for (Tier tier : tiers) {
            if (tier.getId().equals(tierIndex)) {
                return tier;
            }
        }
        return null;
    }
}
