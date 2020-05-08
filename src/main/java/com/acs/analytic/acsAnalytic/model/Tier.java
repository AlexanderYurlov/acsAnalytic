package com.acs.analytic.acsAnalytic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tier {

    /**
     * Tier's index
     */
    Integer index;

    /**
     * Battery capacity of each tier in kWh
     */
    Integer batteryCapacity;

    /**
     * energy acceptance rate of tiers in kW
     */
    Float energyAcceptanceRate;

    /**
     * max waiting time for each tier: 480, 300, and 120 mins
     */
    Integer maxWaitingTime;

    /**
     * Vehicle ratios (R1, R2, ..., RN). SUMM(R1, R2, ..., RN) = 1
     */
    Float vehicleRatio;
}
