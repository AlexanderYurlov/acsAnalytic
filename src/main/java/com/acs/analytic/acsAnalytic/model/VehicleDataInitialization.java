package com.acs.analytic.acsAnalytic.model;

import java.util.Map;

import lombok.Data;

@Data
public class VehicleDataInitialization {

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
    Integer n;

    /**
     * Период времени расчёта
     */
    Integer timeGeneration;

}
