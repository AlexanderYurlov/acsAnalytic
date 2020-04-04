package com.acs.analytic.acsAnalytic.model;

import lombok.Data;

@Data
public class Vehicle {

    Integer id;

    /**
     * с колёс/ через запрос
     */
    String type;

//    /**
//     * Список используемых зарядок
//     */
//    List<Tier> tier;

    /**
     * Зарядка используемая по умолчанию
     */
    Tier tier;

    /**
     * Время запроса на зарядку
     */
    Long arrT;

    /**
     * Время зарядки зарядкой по умолчанию
     */
    Long chargT;

    /**
     * нижняя граница предпологаемого прибытия на станцию
     */
    Long eArrT;

    /**
     * Верхняя граница. Время окончания зарядки
     */
    Long deadlT;

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
    Long act_compl_t;

    /**
     * Обновляемое время прибытия на станцию
     */
    Long resArrT;

}
