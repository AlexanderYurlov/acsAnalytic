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
import com.acs.analytic.acsAnalytic.model.Tier;
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

        Map<String, Integer> pumpMap = null;
        Map<String, Integer> sharablePumpMap = null;
        try {
            pumpMap = om.readValue(initialData.getPumpMapStr(), Map.class);
            sharablePumpMap = om.readValue(initialData.getSharablePumpsStr(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
                .replace(PUMPS_PER_TIER, printTotalPumpsPerTier(pumpMap, sharablePumpMap))
                .replace(PUMPS_SHARED_PER_TIER, printPumpsPerTier(sharablePumpMap));
        startTime = initializedData.getStartTime();
        endTime = initializedData.getEndTime();
        status = initializedData.getStatus();
    }

    private String printTotalPumpsPerTier(Map<String, Integer> pumpMap, Map<String, Integer> sharablePumpMap) {
        Map<String, Integer> totalPump = new HashMap<>();
        for (String tierId : pumpMap.keySet()) {
//            todo
            int quantity = sharablePumpMap.get(tierId) == null ? 0 : sharablePumpMap.get(tierId);
            totalPump.put(tierId, pumpMap.get(tierId) + quantity);
        }
        return printPumpsPerTier(totalPump);
    }

    private String printPumpsPerTier(Map<String, Integer> pumpMap) {
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
        Map<Integer, Tier> tierMap = getTierMap(tierPumps);
        Map<Integer, Map<Integer, TierPump>> mapTierPump = new HashMap<>();
        Map<Integer, Map<Integer, List<Consumer>>> processedVehiclesMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            var tierId = vehicle.getChargedTierId();
            var pumpId = vehicle.getPumpId();
            processedVehiclesMap.computeIfAbsent(tierId, k -> new HashMap<>());
            processedVehiclesMap.get(tierId).computeIfAbsent(pumpId, k -> new ArrayList<>());
            processedVehiclesMap.get(tierId).get(pumpId).add(new Consumer(vehicle, tierMap.get(vehicle.getTierId())));
        }
        List<ScheduleData> scheduleDataList = new ArrayList<>();
        for (TierPump tierPump : tierPumps) {
            var tierId = tierPump.getTier().getId();
            mapTierPump.computeIfAbsent(tierId, k -> new HashMap<>());
            mapTierPump.get(tierId).put(tierPump.getId(), tierPump);
            scheduleDataList.add(ScheduleData.builder()
                    .tierId(tierId)
                    .pumpId(tierPump.getId())
                    .isShareable(tierId != 0 && tierPump.getIsShareable())
                    .consumers(processedVehiclesMap.get(tierId) != null ? processedVehiclesMap.get(tierId).get(tierPump.getId()) != null ?
                            processedVehiclesMap.get(tierId).get(tierPump.getId()) : new ArrayList<>()
                            : new ArrayList<>())
                    .build());
        }
        return scheduleDataList;
    }

    private Map<Integer, Tier> getTierMap(List<TierPump> tierPumps) {
        Map<Integer, Tier> tierMap = new HashMap<>();
        for (TierPump tierPump : tierPumps) {
            var tierId = tierPump.getTier().getId();
            tierMap.computeIfAbsent(tierId, v -> tierPump.getTier());
        }
        return tierMap;
    }

}
