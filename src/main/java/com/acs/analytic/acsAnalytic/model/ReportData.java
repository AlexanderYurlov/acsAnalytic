package com.acs.analytic.acsAnalytic.model;

import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.deserializer.SharableConfDeserializer;
import com.acs.analytic.acsAnalytic.model.dto.SharableConfDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;

@TypeDef(
        name = "jsonb",
        typeClass = JsonNodeBinaryType.class
)
@Table(name = "report_data")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "initialized_data_id")
    private InitializedData initializedData;

    @Type(type = "jsonb")
    @Column(name = "sharable_conf", columnDefinition = "jsonb")
    @JsonDeserialize(using = SharableConfDeserializer.class)
    private List<SharableConfDto> sharableConf;

}
