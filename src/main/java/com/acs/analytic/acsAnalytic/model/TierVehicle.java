package com.acs.analytic.acsAnalytic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierVehicle {

    /**
     * Vehicle ratios (R1, R2, ..., RN). SUMM(R1, R2, ..., RN) = 1
     */
    Float vehicleRatio;

    /**
     * Tier's index
     */
    Integer tierIndex;

}
