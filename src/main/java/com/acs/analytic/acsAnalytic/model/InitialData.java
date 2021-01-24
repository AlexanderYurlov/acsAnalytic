package com.acs.analytic.acsAnalytic.model;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
@Table(name = "initial_data")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitialData {

    @PrePersist
    void prePersist() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        this.pumpMapStr = om.writeValueAsString(this.pumpMap);
        this.sharablePumpsStr = om.writeValueAsString(this.sharablePumps);
        this.rStr = om.writeValueAsString(this.r);
    }

    @Id
    @SequenceGenerator(name = "hibernateSeq", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernateSeq")
    @Column(name = "id")
    Long id;

    @Column(name = "name")
    String name;

    /**
     * Common Data (Tiers Data) is actual for AutoTraffic
     */
    /**
     * Tiers for Pump and Vehicle
     */
    @OneToMany(mappedBy = "initialData", fetch = FetchType.EAGER)
    List<Tier> tiers;


    /**
     * ACS Data
     */

    /**
     * Numbers of pumps per tier: Pump1, …, PumpN;
     */
    @Transient
    Map<Integer, Integer> pumpMap;
    @Type(type = "jsonb")
    @Column(name = "pump_map", columnDefinition = "jsonb")
    String pumpMapStr;


    /**
     * Numbers of sharable pumps per tier: PS1, …, PSN;
     */
    @Transient
    Map<Integer, Integer> sharablePumps;
    @Type(type = "jsonb")
    @Column(name = "sharable_pumps", columnDefinition = "jsonb")
    String sharablePumpsStr;


    /**
     * AutoTraffic
     */

    /**
     * Charging requests’ arrival rate
     * константа зависит от города/мегаполиса/местности
     */
    @Column(name = "arrival_rate")
    Float arrivalRate;

    /**
     * типы зарядок автомобилей и их соотношение - R ( сумма = 1)
     */
    @Transient
    List<TierVehicle> r;

    @Type(type = "jsonb")
    @Column(name = "r", columnDefinition = "jsonb")
    String rStr;

    /**
     * Walk-in client ratio (RW + RR = 1)
     */
    @Column(name = "rw")
    Float rw;

    /**
     * Reservation requests ratio (RW + RR = 1)
     */
    @Column(name = "rr")
    Float rr;

    /**
     * Maximum number of simulated: Vehmax.
     * Или может быть расчитана из maximum timeGeneration
     */
    @Column(name = "veh_max")
    Integer vehMax;

    public Tier getTierByIndex(Integer tierIndex) {
        for (Tier tier : tiers) {
            if (tier.getId().equals(tierIndex)) {
                return tier;
            }
        }
        return null;
    }
}
