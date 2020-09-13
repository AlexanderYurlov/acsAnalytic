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
    TierPump pump;

    /**
     * Промежуточное время окончания зарядки. compl_t. Если автомобиль зарядился = 0
     */
    Double resComplT;

    /**
     * Фактическое время начала зарядки
     */
    Double actArrT;

    /**
     * Фактическое время окончания зарядки
     */
    Double actComplT;

    /**
     * Обновляемое время прибытия на станцию
     *  'res' means 'reset' or 'recalculated
     */
    Double resArrT;

}
