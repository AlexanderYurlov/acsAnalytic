package com.acs.analytic.acsAnalytic.model.resp;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;

@Builder
@Getter
@Setter
public class ReportDetailsDataDto {

    private Long id;
    private String name;
    private String inputData;
    private String startTime;
    private String endTime;
    private SimulationStatus status;
    private List<ScheduleData> scheduleData;
}
