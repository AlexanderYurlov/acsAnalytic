package com.acs.analytic.acsAnalytic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Table(name = "initialized_data")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitializedData implements Serializable {

    public InitializedData(InitialData initialData, TierPumpConf tierPumpConf, List<Vehicle> vehicleList) {
        this.initialData = initialData;
        this.vehicles = vehicleList;
        List<TierPump> tierPumps = getTierPumpList(tierPumpConf.tierPumpsMap);
        tierPumps.addAll(getTierPumpList(tierPumpConf.sharableTierPumpsMap));
        this.tierPumps = tierPumps;

        for (Tier t : this.initialData.getTiers()) {
            t.setInitialData(this.initialData);
        }
        for (Vehicle v : this.vehicles) {
            v.setInitializedData(this);
        }
        for (TierPump pump : this.tierPumps) {
            pump.setInitializedData(this);
        }

        this.status = SimulationStatus.INITIALIZED;

        this.name = initialData.getName();
    }

    private List<TierPump> getTierPumpList(Map<Integer, List<TierPump>> tierPumpsMap) {
        return tierPumpsMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @OneToOne(cascade = CascadeType.ALL)
    InitialData initialData;

    @OneToMany(mappedBy = "initializedData", cascade = CascadeType.ALL)
    @Builder.Default
    List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "initializedData", cascade = CascadeType.ALL)
    @Builder.Default
    List<TierPump> tierPumps = new ArrayList<>();

    @Column(name = "name")
    String name;

    @Column(name = "start_time")
    Date startTime;

    @Column(name = "end_time")
    Date endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    SimulationStatus status;

}
