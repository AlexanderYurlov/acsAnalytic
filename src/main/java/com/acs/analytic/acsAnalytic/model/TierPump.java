package com.acs.analytic.acsAnalytic.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Table(name = "tier_pump")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TierPump {

    public TierPump(int id, Tier tier, Boolean isShareable) {
        this.id = id;
        this.tier = tier;
        this.isShareable = isShareable;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long systemId;

    /**
     * ID пампа
     */
    @Column(name = "pump_id")
    int id;

    /**
     * Разделяемая зарядка?
     */
    @Column(name = "is_shareable")
    Boolean isShareable;

    /**
     * Tier
     */
    @OneToOne(cascade = CascadeType.ALL)
    Tier tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    InitializedData initializedData;

}
