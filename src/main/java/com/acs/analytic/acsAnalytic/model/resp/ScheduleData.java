package com.acs.analytic.acsAnalytic.model.resp;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ScheduleData {

    Integer pumpId;
    Integer tierId;
    List<Consumer> consumers;

}
