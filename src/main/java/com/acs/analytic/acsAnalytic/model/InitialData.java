package com.acs.analytic.acsAnalytic.model;

import java.util.HashMap;
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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.acs.analytic.acsAnalytic.dto.InitialDataDto;
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

    public InitialData(InitialDataDto dto) {
        this.name = dto.getName();
        this.tiers = dto.getTiers();
        this.arrivalRate = dto.getArrivalRate();
        this.r = dto.getR();
        this.rStr = dto.getRStr();
        this.rw = dto.getRw();
        this.rr = dto.getRr();
        this.vehMax = dto.getVehMax();
        if (dto.getPumpMap().size() != dto.getSharablePumps().size()) {
            throw new RuntimeException("Different quantity of tiers for all pumpMap and sharablePumpMap");
        }
        Map<Integer, Integer> pumpMap = new HashMap<>();
        for (Integer tierId : dto.getPumpMap().keySet()) {
            int val = dto.getPumpMap().get(tierId) - dto.getSharablePumps().get(tierId);
            if (val < 0) {
                throw new RuntimeException("Quantity of Sharable pump more than total pump quantity for tier " + tierId);
            }
            pumpMap.put(tierId, val);
        }
        this.pumpMap = pumpMap;
        this.sharablePumps = dto.getSharablePumps();
        this.rejectedReportDelta = dto.getRejectedReportDelta() != null ? dto.getRejectedReportDelta() : .1f;
        this.totalPumpMap = dto.getPumpMap();
    }

    @PrePersist
    void prePersist() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        this.pumpMapStr = om.writeValueAsString(this.pumpMap);
        this.sharablePumpsStr = om.writeValueAsString(this.sharablePumps);
        this.totalPumpMapStr = om.writeValueAsString(this.totalPumpMap);
        this.rStr = om.writeValueAsString(this.r);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    /**
     * Common Data (Tiers Data) is actual for AutoTraffic
     */
    /**
     * Tiers for Pump and Vehicle
     */
    @OneToMany(mappedBy = "initialData", fetch = FetchType.EAGER)
    private List<Tier> tiers;


    /**
     * ACS Data
     */

    /**
     * Numbers of pumps per tier: Pump1, …, PumpN;
     */
    @Transient
    private Map<Integer, Integer> pumpMap;
    @Type(type = "jsonb")
    @Column(name = "pump_map", columnDefinition = "jsonb")
    private String pumpMapStr;

    @Transient
    private Map<Integer, Integer> totalPumpMap;
    @Type(type = "jsonb")
    @Column(name = "total_pump_map", columnDefinition = "jsonb")
    private String totalPumpMapStr;

    /**
     * Numbers of sharable pumps per tier: PS1, …, PSN;
     */
    @Transient
    private Map<Integer, Integer> sharablePumps;
    @Type(type = "jsonb")
    @Column(name = "sharable_pumps", columnDefinition = "jsonb")
    private String sharablePumpsStr;


    /**
     * AutoTraffic
     */

    /**
     * Charging requests’ arrival rate
     * константа зависит от города/мегаполиса/местности
     */
    @Column(name = "arrival_rate")
    private Float arrivalRate;

    /**
     * типы зарядок автомобилей и их соотношение - R ( сумма = 1)
     */
    @Transient
    private List<TierVehicle> r;

    /**
     * Дельта отчётов
     */
    @Transient
    private Float rejectedReportDelta;

    @Type(type = "jsonb")
    @Column(name = "r", columnDefinition = "jsonb")
    private String rStr;

    /**
     * Walk-in client ratio (RW + RR = 1)
     */
    @Column(name = "rw")
    private Float rw;

    /**
     * Reservation requests ratio (RW + RR = 1)
     */
    @Column(name = "rr")
    private Float rr;

    /**
     * Maximum number of simulated: Vehmax.
     * Или может быть расчитана из maximum timeGeneration
     */
    @Column(name = "veh_max")
    private Integer vehMax;

    public Tier getTierByIndex(Integer tierIndex) {
        for (Tier tier : tiers) {
            if (tier.getId().equals(tierIndex)) {
                return tier;
            }
        }
        return null;
    }
}
