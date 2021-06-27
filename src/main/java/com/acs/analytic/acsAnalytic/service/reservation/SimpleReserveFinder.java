package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareListVehicles;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareVehicle;

@Service
public class SimpleReserveFinder extends AbstractReserveFinder {

    /**
     * Получаем все возможные комбинации
     *
     * @param veh - авто, которое пытаемся поставить в очередь.
     * @param list - автомобили, которые уже стоят в очереди.
     *
     * @return Список всех возможных комбинаций
     */
    protected List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> list) {
        List<List<Vehicle>> all = new ArrayList<>();
        List<Vehicle> newList = new LinkedList<>();
        newList.add(veh);
        all.add(newList);
        for (Vehicle i : list) {
            all = getAll(all, i);
        }
        return all;
    }

    @Override
    protected ReservationResult updateBestResult(ReservationResult bestResult, ReservationResult result) {
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
