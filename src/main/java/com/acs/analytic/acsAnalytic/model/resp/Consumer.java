package com.acs.analytic.acsAnalytic.model.resp;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Getter
@Setter
public class Consumer {

    /**
     * id потребителя
     */
    Integer id;

    /**
     * с колёс/ через запрос
     */
    VehicleRequestType type;

    /**
     * Зарядка используемая по умолчанию
     */
    Integer tierId;

    /**
     * Зарядка используемая по умолчанию
     */
    Integer chargedTierId;

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
     * Верхняя граница времени окончания зарядки
     */
    Double deadlT;

    /**
     * Используемаый зарядник(pump)
     */
    Integer pumpId;

    /**
     * Фактическое время начала зарядки
     */
    Double actStartChargeT;

    /**
     * Фактическое время окончания зарядки
     */
    Double actComplT;

    public Consumer(Vehicle vehicle) {
        id = vehicle.getId();
        type = vehicle.getType();
        tierId = vehicle.getTierId();
        chargedTierId = vehicle.getChargedTierId();
        arrT = vehicle.getArrT();
        chargT = vehicle.getChargT();
        earliestArrT = vehicle.getEarliestArrT();
        deadlT = vehicle.getDeadlT();
        pumpId = vehicle.getPumpId();
        actStartChargeT = vehicle.getActStartChargeT();
        actComplT = vehicle.getActComplT();
    }
}
