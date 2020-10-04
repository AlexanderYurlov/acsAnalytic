package com.acs.analytic.acsAnalytic.model.vehicle;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    /**
     * actComplT = actArrT + chargT
     * actArrT >= arrT + eArrT
     * actComplT <= deadlT
     */

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
    Integer tierId;

    /**
     * Время запроса на зарядку
     */
    Double arrT;

    /**
     * Время зарядки зарядкой по умолчанию, для различных зарядных устройств.
     */
    List<Double> chargT;

    /**
     * Нижняя граница предпологаемого прибытия на станцию
     */
    Double earliestArrT;

    /**
     * Обновляемое время прибытия на станцию (нижняя граница)
     *  'res' means 'reset' or 'recalculated
     */
    Double resEarliestArrT;

    /**
     * Верхняя граница времени окончания зарядки
     */
    Double deadlT;

    /**
     * Обновлённая верхняя граница времени окончания зарядки
     */
    Double resDeadlT;

    /**
     * Используемаый зарядник(pump)
     */
    Integer pump;

    /**
     * Фактическое время начала зарядки
     */
    Double actStartChargeT;

    /**
     * Обновлённое время начала зарядки
     */
    Double resStartChargeT;

    /**
     * Фактическое время окончания зарядки
     */
    Double actComplT;

    /**
     * Обновлённое время окончания зарядки. compl_t. Если автомобиль зарядился = 0
     */
    Double resComplT;

}
