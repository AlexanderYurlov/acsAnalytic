package com.acs.analytic.acsAnalytic.model.resp;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;

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

    public ReportDetailsDataDto(InitializedData initializedData, List<ScheduleData> scheduleDataList) {
        this(initializedData);
        scheduleData = scheduleDataList;
    }
}
