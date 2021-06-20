package com.acs.analytic.acsAnalytic.service.reservation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Service
public class UnoptimizedReserveFinder extends AbstractReserveFinder {

    @Override
    protected List<List<Vehicle>> getAllCombination(Vehicle veh, List<Vehicle> vehicles) {
        List<List<Vehicle>> result = new LinkedList<>();
        int size = vehicles.size();
        for (int i = 0; i <= size; i++) {
            List newList = new LinkedList(vehicles);
            newList.add(i, veh);
            result.add(newList);
        }
        return result;
    }

    public static void main(String[] args) {
        List<Integer> vehicles = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        Integer veh = 0;
        List<List<Integer>> result = new LinkedList<>();
        int size = vehicles.size();
        for (int i = 0; i <= size; i++) {
            List newList = new LinkedList(vehicles);
            newList.add(i, veh);
            result.add(newList);
        }
        result.forEach(System.out::println);
    }
}
