package com.acs.analytic.acsAnalytic.model.resp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPump;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;

@Getter
@Setter
public class ReportDetailsDataDto {

    private static final String ARRIVAL_RATE = "{ArrivalRate}";
    private static final String MAX_NUMBER_OF_VEHICLES = "{MaxNumberOfVehicles}";
    private static final String WALKIN_RATIO = "{WalkinRatio}";
    private static final String TIER_RATIO = "{TierRatio}";
    private static final String AVERAGE_BATTERY_CAPACITY = "{AverageBatteryCapacity}";
    private static final String AVERAGE_ENERGY_ACCEPTANCE_RATE = "{AverageEnergyAcceptanceRate}";
    private static final String AVERAGE_WAITING_TIME = "{AverageWaitingTime}";
    private static final String PUMPS_PER_TIER = "{PumpsPerTier}";
    private static final String PUMPS_SHARED_PER_TIER = "{PumpsSharedPerTier}";

    private static final ObjectMapper om = new ObjectMapper();

    private static final String INPUT_DATA_TEMPLATE =
            ARRIVAL_RATE + ";" +
                    MAX_NUMBER_OF_VEHICLES + ";" +
                    WALKIN_RATIO + ";" +
                    TIER_RATIO + ";" +
                    AVERAGE_BATTERY_CAPACITY + ";" +
                    AVERAGE_ENERGY_ACCEPTANCE_RATE + ";" +
                    AVERAGE_WAITING_TIME + "___" + PUMPS_PER_TIER + ";" +
                    PUMPS_SHARED_PER_TIER;

    private Long id;
    private String name;
    private String inputData;
    private Date startTime;
    private Date endTime;
    private SimulationStatus status;
    private List<ScheduleData> scheduleData;

    public ReportDetailsDataDto(InitializedData initializedData) {
        id = initializedData.getId();
        name = initializedData.getName();
        InitialData initialData = initializedData.getInitialData();
        inputData = INPUT_DATA_TEMPLATE.replace(ARRIVAL_RATE, initialData.getArrivalRate().toString())
                .replace(MAX_NUMBER_OF_VEHICLES, initialData.getVehMax().toString())
                .replace(WALKIN_RATIO, initialData.getRw().toString())
                .replace(TIER_RATIO, printTierRatio(initialData))
                .replace(AVERAGE_BATTERY_CAPACITY, initialData.getTiers()
                        .stream()
                        .map(x -> String.valueOf(x.getBatteryCapacity()))
                        .collect(Collectors.joining("/")))
                .replace(AVERAGE_ENERGY_ACCEPTANCE_RATE, initialData.getTiers()
                        .stream()
                        .map(x -> String.valueOf(x.getEnergyAcceptanceRate()))
                        .collect(Collectors.joining("/")))
                .replace(AVERAGE_WAITING_TIME, initialData.getTiers()
                        .stream()
                        .map(x -> String.valueOf(x.getMaxWaitingTime()))
                        .collect(Collectors.joining("/")))
                .replace(PUMPS_PER_TIER, printPumpsPerTier(initialData.getPumpMapStr()))
                .replace(PUMPS_SHARED_PER_TIER, printPumpsPerTier(initialData.getSharablePumpsStr()));
        startTime = initializedData.getStartTime();
        endTime = initializedData.getEndTime();
        status = initializedData.getStatus();
    }

    private String printPumpsPerTier(String pumpMapStr) {
        Map<String, Integer> pumpMap = null;
        try {
            pumpMap = om.readValue(pumpMapStr, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return pumpMap.values().stream().map(String::valueOf).collect(Collectors.joining("/"));
    }

    private String printTierRatio(InitialData initialData) {
        Map<String, Integer> pumpMap = null;
        try {
            pumpMap = om.readValue(initialData.getPumpMapStr(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assert pumpMap != null;
        Integer totalPumps = pumpMap.values().stream().reduce(0, Integer::sum);

        return pumpMap.values().stream().map(x -> String.valueOf(round(x / (double) totalPumps, 2))).collect(Collectors.joining("/"));

    }

    public ReportDetailsDataDto(InitializedData initializedData, List<Vehicle> vehicles) {
        this(initializedData);
        scheduleData = fillScheduleData(initializedData.getTierPumps(), vehicles);
    }

    private List<ScheduleData> fillScheduleData(List<TierPump> tierPumps, List<Vehicle> vehicles) {
        Map<Integer, Map<Integer, TierPump>> mapTierPump = new HashMap<>();
        for (TierPump tierPump : tierPumps) {
            var tierId = tierPump.getTier().getId();
            mapTierPump.computeIfAbsent(tierId, k -> new HashMap<>());
            mapTierPump.get(tierId).put(tierPump.getId(), tierPump);
        }
        Map<Integer, Map<Integer, List<Consumer>>> processedVehiclesMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            var tierId = vehicle.getChargedTierId();
            var pumpId = vehicle.getPumpId();
            processedVehiclesMap.computeIfAbsent(tierId, k -> new HashMap<>());
            processedVehiclesMap.get(tierId).computeIfAbsent(pumpId, k -> new ArrayList<>());
            processedVehiclesMap.get(tierId).get(pumpId).add(new Consumer(vehicle));
        }
        List<ScheduleData> scheduleDataList = new ArrayList<>();
        for (Integer tierId : processedVehiclesMap.keySet()) {
            for (Integer pumpId : processedVehiclesMap.get(tierId).keySet()) {
                boolean isShareable = tierId != 0 && mapTierPump.get(tierId).get(pumpId).getIsShareable();
                Integer batteryCapacity = tierId != 0 ? mapTierPump.get(tierId).get(pumpId).getTier().getBatteryCapacity() : null;
                Float energyAcceptanceRate = tierId != 0 ? mapTierPump.get(tierId).get(pumpId).getTier().getEnergyAcceptanceRate() : null;
                scheduleDataList.add(ScheduleData.builder()
                        .tierId(tierId)
                        .pumpId(pumpId)
                        .isShareable(isShareable)
                        .batteryCapacity(batteryCapacity)
                        .energyAcceptanceRate(energyAcceptanceRate)
                        .consumers(processedVehiclesMap.get(tierId).get(pumpId))
                        .build());
            }
        }
        return scheduleDataList;
    }

}
