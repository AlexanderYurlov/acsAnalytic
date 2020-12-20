package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareListVehicles;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareVehicle;

@Service
public class SimpleReserveFinder implements ReserveFinder {

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
        return bestResult;
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
                currTime = resComplT;
            } else {
                return new ReservationResult(false);
            }
        }
        return new ReservationResult(null, true, currTime, combination);
    }

    /**
     * Получаем все возможные комбинации
     *
     * @param veh - авто, которое пытаемся поставить в очередь.
     * @param list - автомобили, которые уже стоят в очереди.
     *
     * @return Список всех возможных комбинаций
     */
    private static List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> list) {
        List<List<Vehicle>> all = new ArrayList<>();
        List<Vehicle> newList = new LinkedList<>();
        newList.add(veh);
        all.add(newList);
        for (Vehicle i : list) {
            all = getAll(all, i);
        }
        return all;
    }

    /**
     * Для генерации новых комбинаций. Входной авто в один и тот же список ставится поочередно в новое место.
     * Каждая постановка - новая коомбинация
     *
     * @param all - список входных комбинаций
     * @param v - входной авто.
     *
     * @return
     */
    private static List<List<Vehicle>> getAll(List<List<Vehicle>> all, Vehicle v) {
        List<List<Vehicle>> result = new LinkedList<>();
        for (List<Vehicle> list : all) {
            int size = list.size();
            for (int i = 0; i <= size; i++) {
                List newList = new LinkedList(list);
                newList.add(i, v);
                result.add(newList);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Vehicle veh = prepareVehicle(14.23d, List.of(151.51d), 12.59d, 249.11);
        List<Vehicle> vehicles = prepareListVehicles();
        int tierId = 1;
        ReservationResult result = new SimpleReserveFinder().tryToReserve(veh, vehicles, 0d, tierId, 1);
        System.out.println(result.isReserved());
        if (result.isReserved()) {
            for (Vehicle v : result.getCombination()) {
                System.out.println(v);
            }
        }
    }
}
