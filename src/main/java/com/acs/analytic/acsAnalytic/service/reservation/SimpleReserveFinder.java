package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareListVehicles;
import static com.acs.analytic.acsAnalytic.service.reservation.matrix.MatrixCreatorHelper.prepareVehicle;

public class SimpleReserveFinder implements ReserveFinder {

    @Override
    public ReservationResult tryToReserve(Vehicle veh, List<Vehicle> vehicles, int tierId) {
        List<List<Vehicle>> allCombination = getAllCombination(veh, vehicles);
        System.out.println("allCombination size = " + allCombination.size());

        for (List<Vehicle> combination : allCombination) {
            ReservationResult result = tryToReserve(combination, tierId);
            if (result.isReserved()) {
                return result;
            }
        }
        return new ReservationResult(false, null);
    }

    private static ReservationResult tryToReserve(List<Vehicle> combination, int tierId) {
        double currTime = 0d;
        for (Vehicle v : combination) {
            Double eArrT = v.getEArrT();
            Double chargT = v.getChargT().get(tierId);
            double resComplT;
            double resArrT;
            if (currTime >= eArrT) {
                resComplT = currTime + chargT;
                resArrT = currTime;
            } else {
                resComplT = eArrT + chargT;
                resArrT = eArrT;
            }
            if (resComplT <= v.getDeadlT()) {
                v.setResArrT(resArrT);
                v.setResComplT(resComplT);
                currTime = resComplT;
            } else {
                return new ReservationResult(false, null);
            }
        }
        return new ReservationResult(true, combination);
    }

    public static List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> list) {
        List<List<Vehicle>> all = new ArrayList<>();
        List<Vehicle> newList = new LinkedList<>();
        newList.add(veh);
        all.add(newList);
        for (Vehicle i : list) {
            all = getAll(all, i);
        }
        return all;
    }

    private static List<List<Vehicle>> getAll(List<List<Vehicle>> all, Vehicle e) {
        List<List<Vehicle>> result = new LinkedList<>();
        for (List<Vehicle> list : all) {
            int size = list.size();
            for (int i = 0; i <= size; i++) {
                List newList = new LinkedList(list);
                newList.add(i, e);
                result.add(newList);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Vehicle veh = prepareVehicle(14.23d, List.of(151.51d), 12.59d, 249.11);
        List<Vehicle> vehicles = prepareListVehicles();
        int tierId = 0;
        ReservationResult result = new SimpleReserveFinder().tryToReserve(veh, vehicles, tierId);
        System.out.println(result.isReserved());
        if (result.isReserved()) {
            for (Vehicle v : result.getCombination()) {
                System.out.println(v);
            }
        }
    }
}