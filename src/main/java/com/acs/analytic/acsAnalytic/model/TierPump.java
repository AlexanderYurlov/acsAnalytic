package com.acs.analytic.acsAnalytic.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TierPump {

    int id;

    /**
     * Tier's index
     */
    Tier Tier;

}
