package com.acs.analytic.acsAnalytic.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.SimulationResult;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;
import com.acs.analytic.acsAnalytic.service.reservation.SimpleReserveFinder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.acs.analytic.acsAnalytic.Utils.round;
import static com.acs.analytic.acsAnalytic.UtilsCsv.writeToCSV;

@Service
public class QueueProcessSimulationService {

    public final ObjectMapper om = new ObjectMapper();

    private final ReserveFinder reserveFinder;

    QueueProcessSimulationService(ReserveFinder reserveFinder) {
        this.reserveFinder = reserveFinder;
    }

    /**
     * @param vehicles - все сгенерированные авто
     * @param tierPumpConf - конфигурация уровне зарядки и пампов на зарядной станции в том числе разделяемые зарядки
     *
     * @return
     */
    public List<Vehicle> simulate(List<Vehicle> vehicles, TierPumpConf tierPumpConf) throws JsonProcessingException {

        System.out.println(new ObjectMapper().writeValueAsString(tierPumpConf));

        long startTime = System.currentTimeMillis();

        SimulationResult simulationResult = new SimulationResult(tierPumpConf);

        // авто заряженные
        Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap = simulationResult.getProcessedVehiclesMap();

        // авто стоящие на зарядке в данный момент
        Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap = simulationResult.getChargingVehiclesMap();

        // авто поставленные в очередь
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = simulationResult.getInProgressVehiclesMap();

        // Отказано в зарядке
        List<Vehicle> rejectedVehicles = simulationResult.getRejectedVehicles();

        for (int num = 0; num < vehicles.size(); num++) {

            var vehicle = vehicles.get(num);
            arrange(vehicle, inProgressVehiclesMap, chargingVehiclesMap, processedVehiclesMap);

            boolean result = tryReserve(vehicle, simulationResult, tierPumpConf);
            if (!result) {
                vehicle.setPump(0);
                vehicle.setChargedTierId(0);
                rejectedVehicles.add(vehicle);
            }
        }
        finishChargingVehicles(chargingVehiclesMap, processedVehiclesMap);

        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");

        printResult(simulationResult, vehicles);

        return vehicles;
    }

    private void finishChargingVehicles(Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap, Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap) {
        for (Integer tierId : chargingVehiclesMap.keySet()) {
            for (Integer pumpId : chargingVehiclesMap.get(tierId).keySet()) {
                Vehicle veh = chargingVehiclesMap.get(tierId).get(pumpId);
                processedVehiclesMap.get(tierId).get(pumpId).add(veh);
            }
        }
    }

    /**
     * Попытка поставить авто в резерв
     *
     * @param vehicle - авто, которое пытаемся поставить в резерв
     * @param simulationResult - список авто
     * @param tierPumpConf
     *
     * @return результат попытки
     */
    private boolean tryReserve(Vehicle vehicle, SimulationResult simulationResult, TierPumpConf tierPumpConf) {
        Boolean result = tryNormReserve(vehicle, simulationResult, tierPumpConf);

        return result;
    }

    private boolean tryNormReserve(Vehicle veh, SimulationResult simulationResult, TierPumpConf tierPumpConf) {

        int tierId = veh.getTierId();
        boolean result = trySharablePumpsReserve(veh, simulationResult, tierPumpConf, tierId, false);
        if (result) {
            return true;
        }

//        Условие разделения розеток: более медленные авто могут заряжаться от более быстрых розеток, но не наоборот
        for (int chargeTierId = tierId; chargeTierId <= tierPumpConf.getSharableTierPumpsMap().size(); chargeTierId++) {
            result = trySharablePumpsReserve(veh, simulationResult, tierPumpConf, chargeTierId, true);
            if (result) {
                if (tierId != chargeTierId) {
                    System.out.println("trySharablePumpsReserve true " + tierId + " - " + chargeTierId);
                }
                return true;
            }
        }
        return false;
    }

    private boolean trySharablePumpsReserve(Vehicle veh, SimulationResult simulationResult, TierPumpConf tierPumpConf, int tierId, boolean isSharable) {
        Map<Integer, Map<Integer, List<Vehicle>>> inProgress = simulationResult.getInProgressVehiclesMap();
        Map<Integer, Map<Integer, Vehicle>> chargingVeh = simulationResult.getChargingVehiclesMap();
        for (Integer pumpId : inProgress.get(tierId).keySet()) {
            Boolean sharableState = tierPumpConf.isSharable(tierId, pumpId);
            if (isSharable && sharableState || !isSharable && !sharableState) {
                var vehicles = inProgress.get(tierId).get(pumpId);
                var charging = chargingVeh.get(tierId).get(pumpId);
                var remCharge = charging != null ? charging.getResComplT() : 0;
                ReservationResult result = reserveFinder.tryToReserve(veh, vehicles, remCharge, tierId, pumpId);
                if (result.isReserved()) {
                    inProgress.get(tierId).put(pumpId, result.getCombination());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Обновляем переменные с учётом хода времени.
     * Переносим авто перешедшие в статус зарядки из inProgressVehiclesMap в processedVehiclesMap.
     */
    private void arrange(Vehicle vehicle,
                         Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap,
                         Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap,
                         Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap) {
        vehicle.setResEarliestArrT(vehicle.getEarliestArrT());
        vehicle.setResDeadlT(vehicle.getDeadlT());
        var deltaTime = vehicle.getArrT();
        for (Integer tierId : inProgressVehiclesMap.keySet()) {
            Map<Integer, List<Vehicle>> inProgress = inProgressVehiclesMap.get(tierId);
            for (Integer pumpId : inProgress.keySet()) {
                List<Vehicle> vehicles = inProgress.get(pumpId);
                Iterator<Vehicle> i = vehicles.iterator();
                checkCharging(chargingVehiclesMap, tierId, pumpId, deltaTime, processedVehiclesMap);
                while (i.hasNext()) {
                    var veh = i.next();
                    resetTime(veh, deltaTime);
                    if (veh.getResStartChargeT() <= 0) {
                        if (veh.getResComplT() <= 0) {
                            veh.setPump(pumpId);
                            var charged = processedVehiclesMap.get(tierId);
                            charged.get(pumpId).add(veh);
                        } else {
                            var charging = chargingVehiclesMap.get(tierId);
                            charging.put(pumpId, veh);
                        }
                        i.remove();
                    }
                }
            }
        }
    }

    /**
     * Обновляем время для авто
     *
     * @param veh - авто для которого обновляем
     * @param deltaTime - разница, на которую обновляем
     */
    private void resetTime(Vehicle veh, Double deltaTime) {
        var resEarliestArrT = veh.getArrT() + veh.getEarliestArrT() - deltaTime;
        var resDeadlT = resEarliestArrT + veh.getDeadlT();
        var resStartChargeT = veh.getActStartChargeT() - deltaTime;
        var resComplT = veh.getActComplT() - deltaTime;
        veh.setResEarliestArrT(round(resEarliestArrT));
        veh.setResDeadlT(round(resDeadlT));
        veh.setResStartChargeT(round(resStartChargeT));
        veh.setResComplT(round(resComplT));
    }

    /**
     * Проверка зарядилось ли авто
     *
     * @param chargingVehiclesMap - авто в процессе
     * @param tierId - уровень
     * @param pumpId - зарядник
     * @param deltaTime - arrivalTime Время поступления запроса
     * @param processedVehiclesMap - обслуженные авто
     */
    private void checkCharging(Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap, Integer tierId, Integer pumpId, Double deltaTime,
                               Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap) {
        if (!chargingVehiclesMap.containsKey(tierId)) {
            chargingVehiclesMap.put(tierId, new HashMap<>());
        }
        var charging = chargingVehiclesMap.get(tierId);

        if (charging.containsKey(pumpId)) {
            var chargingVeh = charging.get(pumpId);
            var resComplT = chargingVeh.getActComplT() - deltaTime;
            chargingVeh.setResComplT(round(resComplT));
            if (resComplT <= 0) {
                var charged = processedVehiclesMap.get(tierId);
                charged.get(pumpId).add(chargingVeh);
                charging.remove(pumpId);
            }
        }
    }

    @Deprecated
    private boolean tryFastReserve(Vehicle vehicle,
                                   Map<Integer, Map<Integer, List<Vehicle>>> inProgress,
                                   Map<Integer, Map<Integer, Vehicle>> chargingVeh,
                                   int tierId) {

        for (Integer pumpId : inProgress.get(tierId).keySet()) {
            var listVehicles = inProgress.get(tierId).get(pumpId);
            if (listVehicles.isEmpty()) {
                var remChargeTime = chargingVeh.get(tierId).get(pumpId) != null ? chargingVeh.get(tierId).get(pumpId).getResComplT() : 0;
                boolean isReserved = reserve(vehicle, inProgress, tierId, remChargeTime, pumpId);
                if (isReserved) {
                    return true;
                }
            }
        }
        return false;
    }

    @Deprecated
    private boolean reserve(Vehicle veh, Map<Integer, Map<Integer, List<Vehicle>>> listVehicles, int tierId, double remChargeTime, Integer pumpId) {
        Double resStartChargeT;
        double deltaTime = veh.getArrT();
        if (veh.getResEarliestArrT() >= remChargeTime) {
            resStartChargeT = veh.getResEarliestArrT();
        } else {
            resStartChargeT = remChargeTime;
        }
        var chargeTime = veh.getChargT().get(tierId - 1);
        var resComplTime = resStartChargeT + chargeTime;
        if (resComplTime <= veh.getDeadlT()) {
            veh.setResStartChargeT(resStartChargeT);
            veh.setResComplT(round(resComplTime));
            veh.setActStartChargeT(round(deltaTime + resStartChargeT));
            veh.setActComplT(round(deltaTime + resComplTime));
            veh.setPump(pumpId);
            listVehicles.get(tierId).get(pumpId).add(veh);
            return true;
        }
        return false;
    }

    private void printResult(SimulationResult simulationResult, List<Vehicle> vehicles) {
        Map<Integer, Map<Integer, List<Vehicle>>> processedVehiclesMap = simulationResult.getProcessedVehiclesMap();
        Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap = simulationResult.getChargingVehiclesMap();
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = simulationResult.getInProgressVehiclesMap();
        List<Vehicle> rejectedVehicles = simulationResult.getRejectedVehicles();

        for (Integer tier : processedVehiclesMap.keySet()) {
            var map = processedVehiclesMap.get(tier);
            for (Integer i : map.keySet()) {
                System.out.println("tier: " + tier + " pump: " + i + "  size: " + map.get(i).size());
            }
        }
        try {
            System.out.println("Rejected: ");
            System.out.println(om.writeValueAsString(rejectedVehicles));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
//        try {
//            System.out.println("processed: ");
//            System.out.println(om.writeValueAsString(processedVehiclesMap));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        try {
            System.out.println("in progress: ");
            System.out.println(om.writeValueAsString(inProgressVehiclesMap));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("all: ");
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getPump().equals(23)) {
                    System.out.println(om.writeValueAsString(vehicle));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        var queueProcessSimulationService = new QueueProcessSimulationService(new SimpleReserveFinder());

        InitialData initialData = InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(720)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(600)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(300)
                                .build()
                        )
                )
                .vehMax(1000)
                .rw(0.23f)
                .rr(0.77f)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(.45f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.33f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.22f)
                                .tierIndex(3)
                                .build()
                        )
                )
//                .pumpMap(Map.of(1, 3, 2, 7, 3, 10))
//                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
                .sharablePumps(Map.of(1, 21, 2, 9, 3, 2))
//                .sharablePumps(Map.of(1, 1, 2, 2))
                .arrivalRate(14f)
                .build();

        var vehicles = new VehicleDataGenerationService().generate(initialData);
//        var vehicles = readCsv();
        var tierPumpsMap = new PumpDataGenerationService().generate(initialData);
        var outVehicles = queueProcessSimulationService.simulate(vehicles, tierPumpsMap);
        writeToCSV(outVehicles);
    }

}
