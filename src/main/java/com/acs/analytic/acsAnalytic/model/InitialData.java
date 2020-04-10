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


    /**
     * AutoTraffic
     */


    /**
     * Charging requests’ arrival rate
     * константа зависит от города/мегаполиса/местности
     */
    Float arrivalRate;

    /**
     * типы зарядок автомобилей и их соотношение ( сумма = 1)
     */
    Map<Integer, Float> r;

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

    /**
     * Период времени расчёта
     */
    Integer timeGeneration;

}