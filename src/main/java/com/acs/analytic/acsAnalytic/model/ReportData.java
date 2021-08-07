package com.acs.analytic.acsAnalytic.model;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Table(name = "full_report_data")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportData {

    @PrePersist
    void prePersist() throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        this.pumpMapStr = om.writeValueAsString(this.pumpMap);
        this.sharablePumpsStr = om.writeValueAsString(this.sharablePumps);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initialized_data_id")
    @JsonIgnore
    private InitializedData initializedData;

    @Transient
    private Map<Integer, Integer> pumpMap;
    @Type(type = "jsonb")
    @Column(name = "pump_map", columnDefinition = "jsonb")
    private String pumpMapStr;

    @Transient
    private Map<Integer, Integer> sharablePumps;
    @Type(type = "jsonb")
    @Column(name = "sharable_pumps", columnDefinition = "jsonb")
    private String sharablePumpsStr;

    @Column(name = "rejected")
    private Long rejected;

}
