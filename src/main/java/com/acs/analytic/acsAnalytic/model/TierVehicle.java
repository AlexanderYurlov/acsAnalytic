package com.acs.analytic.acsAnalytic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
