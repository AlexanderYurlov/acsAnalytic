package com.acs.analytic.acsAnalytic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Table(name = "initialized_data")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
    }

    private List<TierPump> getTierPumpList(Map<Integer, List<TierPump>> tierPumpsMap) {
        return tierPumpsMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Id
    @SequenceGenerator(name = "hibernateSeq", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernateSeq")
    @Column(name = "id")
    Long id;

    @OneToOne
    InitialData initialData;

    @OneToMany(mappedBy = "initializedData", cascade = CascadeType.ALL)
    List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "initializedData", cascade = CascadeType.ALL)
    List<TierPump> tierPumps = new ArrayList<>();

}
