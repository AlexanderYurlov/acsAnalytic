package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.acs.analytic.acsAnalytic.dao.VehicleRepository;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.SimulationResult;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.resp.Consumer;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.resp.ScheduleData;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;
import static com.acs.analytic.acsAnalytic.utils.UtilsCsv.readCsv;

@Service
public class QueueProcessSimulationService {

    public final ObjectMapper om = new ObjectMapper();

    private final ReserveFinder reserveFinder;
    private final VehicleRepository vehicleRepository;

    QueueProcessSimulationService(@Qualifier("simpleReserveFinder") ReserveFinder reserveFinder, VehicleRepository vehicleRepository) {
        this.reserveFinder = reserveFinder;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * @param vehicles - все сгенерированные авто
     * @param tierPumpConf - конфигурация уровне зарядки и пампов на зарядной станции в том числе разделяемые зарядки
     *
     * @return
     */
    public ReportDetailsDataDto simulate(List<Vehicle> vehicles, TierPumpConf tierPumpConf) {

        long startTime = System.currentTimeMillis();

        SimulationResult simulationResult = new SimulationResult(tierPumpConf);

        // авто стоящие на зарядке в данный момент
        Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap = simulationResult.getChargingVehiclesMap();

        // авто поставленные в очередь
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = simulationResult.getInProgressVehiclesMap();

        for (int num = 0; num < vehicles.size(); num++) {

            var vehicle = vehicles.get(num);
            arrange(vehicle, inProgressVehiclesMap, chargingVehiclesMap);

            boolean result = tryReserve(vehicle, simulationResult, tierPumpConf);
            if (!result) {
                vehicle.setPumpId(0);
                vehicle.setChargedTierId(0);
            }
        }
        vehicles = vehicleRepository.saveAll(vehicles);
        List<ScheduleData> scheduleDataList = fillScheduleData(vehicles);

        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");

//        printResult(simulationResult, vehicles);
//        return vehicles;
        return ReportDetailsDataDto.builder()
                .id(1L)
                .name("Test 1")
                .inputData("test")
                .startTime(String.valueOf(new Date(startTime)))
                .endTime(String.valueOf(new Date(endTime)))
                .status(SimulationStatus.COMPLETED)
                .scheduleData(scheduleDataList)
                .build();
    }

    private List<ScheduleData> fillScheduleData(List<Vehicle> vehicles) {
        Map<Integer, Map<Integer, List<Consumer>>> processedVehiclesMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            var tierId = vehicle.getTierId();
            var pumpId = vehicle.getPumpId();
            processedVehiclesMap.computeIfAbsent(tierId, k -> new HashMap<>());
            processedVehiclesMap.get(tierId).computeIfAbsent(pumpId, k -> new ArrayList<>());
            processedVehiclesMap.get(tierId).get(pumpId).add(new Consumer(vehicle));
        }
        List<ScheduleData> scheduleDataList = new ArrayList<>();
        for (Integer tierId : processedVehiclesMap.keySet()) {
            for (Integer pumpId : processedVehiclesMap.keySet()) {
                scheduleDataList.add(ScheduleData.builder()
                        .tierId(tierId)
                        .pumpId(pumpId)
                        .consumers(processedVehiclesMap.get(tierId).get(pumpId))
                        .build());
            }
        }
        return scheduleDataList;
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
                         Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap) {
        vehicle.setResEarliestArrT(vehicle.getEarliestArrT());
        vehicle.setResDeadlT(vehicle.getDeadlT());
        var deltaTime = vehicle.getArrT();
        for (Integer tierId : inProgressVehiclesMap.keySet()) {
            Map<Integer, List<Vehicle>> inProgress = inProgressVehiclesMap.get(tierId);
            for (Integer pumpId : inProgress.keySet()) {
                List<Vehicle> vehicles = inProgress.get(pumpId);
                Iterator<Vehicle> i = vehicles.iterator();
                checkCharging(chargingVehiclesMap, tierId, pumpId, deltaTime);
                while (i.hasNext()) {
                    var veh = i.next();
                    resetTime(veh, deltaTime);
                    if (veh.getResStartChargeT() <= 0) {
                        if (veh.getResComplT() <= 0) {
                            veh.setPumpId(pumpId);
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
     */
    private void checkCharging(Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap, Integer tierId, Integer pumpId, Double deltaTime) {
        if (!chargingVehiclesMap.containsKey(tierId)) {
            chargingVehiclesMap.put(tierId, new HashMap<>());
        }
        var charging = chargingVehiclesMap.get(tierId);

        if (charging.containsKey(pumpId)) {
            var chargingVeh = charging.get(pumpId);
            var resComplT = chargingVeh.getActComplT() - deltaTime;
            chargingVeh.setResComplT(round(resComplT));
            if (resComplT <= 0) {
                charging.remove(pumpId);
            }
        }
    }

    private void printResult(SimulationResult simulationResult, List<Vehicle> vehicles) {
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = simulationResult.getInProgressVehiclesMap();
        List<Vehicle> rejectedVehicles = simulationResult.getRejectedVehicles();

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
                if (vehicle.getPumpId().equals(23)) {
                    System.out.println(om.writeValueAsString(vehicle));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws JsonProcessingException {
//        var queueProcessSimulationService = new QueueProcessSimulationService(new SimpleReserveFinder());
//
//        InitialData initialData = InitialData.builder()
//                .tiers(List.of(
//                        Tier.builder()
//                                .id(1)
//                                .batteryCapacity(14)
//                                .energyAcceptanceRate(3.3f)
//                                .maxWaitingTime(720)
//                                .build(),
//                        Tier.builder()
//                                .id(2)
//                                .batteryCapacity(20)
//                                .energyAcceptanceRate(6.6f)
//                                .maxWaitingTime(600)
//                                .build(),
//                        Tier.builder()
//                                .id(3)
//                                .batteryCapacity(81)
//                                .energyAcceptanceRate(120f)
//                                .maxWaitingTime(300)
//                                .build()
//                        )
//                )
//                .vehMax(1000)
//                .rw(0.23f)
//                .rr(0.77f)
//                .r(List.of(
//                        TierVehicle.builder()
//                                .vehicleRatio(.45f)
//                                .tierIndex(1)
//                                .build(),
//                        TierVehicle.builder()
//                                .vehicleRatio(.33f)
//                                .tierIndex(2)
//                                .build(),
//                        TierVehicle.builder()
//                                .vehicleRatio(.22f)
//                                .tierIndex(3)
//                                .build()
//                        )
//                )
////                .pumpMap(Map.of(1, 3, 2, 7, 3, 10))
//                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
////                .sharablePumps(Map.of(1, 21, 2, 9, 3, 2))
////                .sharablePumps(Map.of(1, 1, 2, 2))
//                .arrivalRate(14f)
//                .build();
//
//        var vehicles = new VehicleDataGenerationService().generate(initialData);
////        var vehicles = readCsv();
//        var tierPumpsMap = new PumpDataGenerationService().generate(initialData);
//        var outVehicles = queueProcessSimulationService.simulate(vehicles, tierPumpsMap);
////        writeToCSV(outVehicles);
//    }

    public ReportDetailsDataDto simulateTest() {
        var vehicles = readCsv();
        var tierPumpsMap = new PumpDataGenerationService().generate(
                InitialData.builder()
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
                        .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
                        .build());
        return simulate(vehicles, tierPumpsMap);
    }
}
