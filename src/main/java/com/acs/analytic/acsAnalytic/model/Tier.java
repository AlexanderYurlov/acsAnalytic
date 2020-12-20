package com.acs.analytic.acsAnalytic.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(name = "tier")
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
public class Tier {

    @Id
    @SequenceGenerator(name = "hibernateSeq", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "hibernateSeq")
    @Column(name = "id")
    Long systemId;

    /**
     * Tier's id
     */
    @Column(name = "tier_id")
    Integer id;

    /**
     * Battery capacity of each tier in kWh
     */
    @Column(name = "battery_capacity")
    Integer batteryCapacity;

    /**
     * energy acceptance rate of tiers in kW
     */
    @Column(name = "energy_acceptance_rate")
    Float energyAcceptanceRate;

    /**
     * max waiting time for each tier: 480, 300, and 120 mins
     */
    @Column(name = "max_waiting_time")
    Integer maxWaitingTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    InitialData initialData;

}
