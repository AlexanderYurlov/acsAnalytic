package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.List;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;

public abstract class AbstractReserveFinder implements ReserveFinder {

    protected abstract List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> vehicles);

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
        for (List<Vehicle> combination : allCombination) {
            ReservationResult result = tryToReserve(combination, remChargeTime, deltaTime, tierId, pumpId);
            if (result.isReserved()) {
                if (bestResult.getTime() == null || bestResult.getTime() > result.getTime()) {
                    if (checkOptimalResult(bestResult, result)) {
                        bestResult = result;
                        bestResult.activateDraft();
//                    try {
//                        System.out.println(new ObjectMapper().writeValueAsString(bestResult.getCombination()));
//                    } catch (JsonProcessingException e) {
//                        e.printStackTrace();
//                    }
                    }
                }
            }
        }
        return bestResult;
    }

    private boolean checkOptimalResult(ReservationResult bestResult, ReservationResult result) {
        if (bestResult.getCombination() == null) {
            return true;
        }
        Double resultSumResComplT = getSumResComplT(result);
        Double bestResultSumResComplT = getSumResComplT(bestResult);
        return resultSumResComplT < bestResultSumResComplT;
    }

    private Double getSumResComplT(ReservationResult result) {
        if (result.getSumResComplT() != null) {
            return result.getSumResComplT();
        }
        double sumResComplT = 0;
        for (Vehicle v : result.getCombination()) {
            sumResComplT = sumResComplT + v.getResComplT();
        }
        result.setSumResComplT(sumResComplT);
        return sumResComplT;
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
    private static ReservationResult tryToReserve(List<Vehicle> combination, double remChargeTime, double deltaTime, int tierId, int pumpId) {
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
