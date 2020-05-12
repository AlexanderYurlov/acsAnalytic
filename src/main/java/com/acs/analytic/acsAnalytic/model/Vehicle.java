package com.acs.analytic.acsAnalytic.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;

@Data
public class Vehicle {

    Integer id;

    /**
     * с колёс/ через запрос
     */
    VehicleRequestType type;

//    /**
//     * Список используемых зарядок
//     */
//    List<Tier> tier;

    /**
     * Зарядка используемая по умолчанию
     */
    Integer tierIndex;

    /**
     * Время запроса на зарядку
     */
    Double arrT;

    /**
     * Время зарядки зарядкой по умолчанию, для различных зарядных устройств.
     */
    List<Double> chargT;

    /**
     * нижняя граница предпологаемого прибытия на станцию
     */
    Double eArrT;

    /**
     * Верхняя граница. Время окончания зарядки
     */
    Double deadlT;

    /**
     * Используемаый зарядник(pump)
     */
    PumpTier pump;

    /**
     * Промежуточное время окончания зарядки. compl_t. Если автомобиль зарядился = 0
     */
    Long complT;

    /**
     * Фактическое время начала зарядки
     */
    Long actArrT;

    /**
     * Фактическое время окончания зарядки
     */
    Long actComplT;

    /**
     * Обновляемое время прибытия на станцию
     */
    Long resArrT;

}
