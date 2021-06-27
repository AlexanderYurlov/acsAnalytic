package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.List;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;

public abstract class AbstractReserveFinder implements ReserveFinder {

    protected abstract List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> vehicles);

    protected abstract ReservationResult updateBestResult(ReservationResult bestResult, List<List<Vehicle>> allCombination, double remChargeTime, Double deltaTime, int tierId, int pumpId);

    /**
     * 1. Перебор всех комбинаций.
     * 2. Поочерёдно для каждой комбинации пытаемся все авто уместить в очередь.
     *
     * @param veh - автомобиль, который пытаемся поставить в очередь.
     * @param vehicles - автомобили, которые уже стоят в очереди.
     * @param remChargeTime - время, которое остаётся до оканчания зарядки автомобиля, зарежаемого в данный момент.
     * @param tierId - уровень, определяющий мощность и время зарядки
     *
     * @return результат true/false и комбинация
     */
    @Override
    public ReservationResult tryToReserve(Vehicle veh, List<Vehicle> vehicles, double remChargeTime, int tierId, int pumpId) {
        if (vehicles.size() >= 7) { // условие tier!=1
            System.out.println("Too mach combinations!");
            return new ReservationResult(false);
        }
        List<List<Vehicle>> allCombination = getAllCombination(veh, vehicles);

        ReservationResult bestResult = new ReservationResult(false);
        Double deltaTime = veh.getArrT();
        return updateBestResult(bestResult, allCombination, remChargeTime, deltaTime, tierId, pumpId);
    }

    /**
     * Попытка поставить в резерв комбинацию.
     *
     * @param combination - входная комбинация
     * @param remChargeTime - время, которое остаётся до оканчания зарядки автомобиля, зарежаемого в данный момент.
     * @param tierId - уровень, определяющий мощность и время зарядки
     *
     * @return результат попытки.
     */
    public ReservationResult tryToReserve(List<Vehicle> combination, double remChargeTime, double deltaTime, int tierId, int pumpId) {
        double currTime = remChargeTime;
        for (Vehicle v : combination) {
            Double resEarliestArrT = v.getResEarliestArrT();
            Double chargT = v.getChargT().get(tierId - 1);
            double resComplT;
            double newResStartChargeT;

            if (currTime >= resEarliestArrT) {
                resComplT = currTime + chargT;
                newResStartChargeT = currTime;
            } else {
                resComplT = resEarliestArrT + chargT;
                newResStartChargeT = resEarliestArrT;
            }
            if (resComplT <= v.getResDeadlT()) {
                v.setResStartChargeT(round(newResStartChargeT));
                v.setResComplT(round(resComplT));
                v.setDraftStartChargeT(round(deltaTime + newResStartChargeT));
                v.setDraftComplT(round(deltaTime + resComplT));
                v.setChargedTierId(tierId);
                v.setPumpId(pumpId);
//                v.setSharableState(v.getTierId() != tierId);
                currTime = resComplT;
            } else {
                return new ReservationResult(false);
            }
        }
        return new ReservationResult(null, true, currTime, null, combination);
    }

}
