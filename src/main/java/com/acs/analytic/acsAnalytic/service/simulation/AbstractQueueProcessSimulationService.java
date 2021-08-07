package com.acs.analytic.acsAnalytic.service.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.ReservationResult;
import com.acs.analytic.acsAnalytic.model.SimulationResult;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.PumpDataGenerationService;
import com.acs.analytic.acsAnalytic.service.VehicleDataGenerationService;
import com.acs.analytic.acsAnalytic.service.reservation.ReserveFinder;
import com.acs.analytic.acsAnalytic.service.reservation.SimpleReserveFinder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;
import static com.acs.analytic.acsAnalytic.utils.UtilsCsv.writeToCSV;

public abstract class AbstractQueueProcessSimulationService {

    public final ObjectMapper om = new ObjectMapper();

    protected abstract ReserveFinder getReserveFinder();

    protected List<Vehicle> simulateVehicles(List<Vehicle> vehicles, TierPumpConf tierPumpConf) {

        SimulationResult simulationResult = new SimulationResult(tierPumpConf);

        // авто стоящие на зарядке в данный момент
        Map<Integer, Map<Integer, Vehicle>> chargingVehiclesMap = simulationResult.getChargingVehiclesMap();

        // авто поставленные в очередь
        Map<Integer, Map<Integer, List<Vehicle>>> inProgressVehiclesMap = simulationResult.getInProgressVehiclesMap();

        for (Vehicle vehicle : vehicles) {

            arrange(vehicle, inProgressVehiclesMap, chargingVehiclesMap);

            boolean result = tryReserve(vehicle, simulationResult, tierPumpConf);
            if (!result) {
                vehicle.setPumpId(0);
                vehicle.setChargedTierId(0);
//                vehicle.setSharableState(false);
            }
        }
        return vehicles;
    }

    /**
     * Попытка поставить авто в резерв
     *
     * @param veh - авто, которое пытаемся поставить в резерв
     * @param simulationResult - список авто
     * @param tierPumpConf
     *
     * @return результат попытки
     */
    private boolean tryReserve(Vehicle veh, SimulationResult simulationResult, TierPumpConf tierPumpConf) {

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
                ReservationResult result = getReserveFinder().tryToReserve(veh, vehicles, remCharge, tierId, pumpId);
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
        var resDeadlT = veh.getArrT() + veh.getDeadlT() - deltaTime;
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

    public static void main(String[] args) throws JsonProcessingException {
        var queueProcessSimulationService = new QueueProcessSimulationService(new SimpleReserveFinder());

        InitialData initialData = InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(650)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(550)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(220)
                                .build()
                        )
                )
                .vehMax(10000)
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
                .pumpMap(Map.of(1, 21, 2, 9, 3, 2))
//                .sharablePumps(Map.of(1, 21, 2, 9, 3, 2))
//                .sharablePumps(Map.of(1, 1, 2, 2))
                .arrivalRate(14f)
                .build();

        var vehicles = new VehicleDataGenerationService().generate(initialData);
//        var vehicles = readCsv();
        var tierPumpsMap = new PumpDataGenerationService().generate(initialData);
        //todo
        var outVehicles = queueProcessSimulationService.simulateVehicles(vehicles, tierPumpsMap);
        writeToCSV(outVehicles);
    }

}
