package com.acs.analytic.acsAnalytic.model.resp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Getter
@Setter
public class ReportDetailsDataDto {

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
        //todo
        inputData = initializedData.getInitialData().toString();
        startTime = initializedData.getStartTime();
        endTime = initializedData.getEndTime();
        status = initializedData.getStatus();
    }

    public ReportDetailsDataDto(InitializedData initializedData, List<Vehicle> vehicles) {
        this(initializedData);
        scheduleData = fillScheduleData(vehicles);
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
            for (Integer pumpId : processedVehiclesMap.get(tierId).keySet()) {
                scheduleDataList.add(ScheduleData.builder()
                        .tierId(tierId)
                        .pumpId(pumpId)
                        .consumers(processedVehiclesMap.get(tierId).get(pumpId))
                        .build());
            }
        }
        return scheduleDataList;
    }

}
