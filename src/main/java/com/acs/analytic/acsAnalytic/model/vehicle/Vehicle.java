package com.acs.analytic.acsAnalytic.model.vehicle;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
@Table(name = "vehicle")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long systemId;

    @Column(name = "vehicle_id")
    Integer id;

    /**
     * с колёс/ через запрос
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    VehicleRequestType type;

    /**
     * Зарядка используемая по умолчанию
     */
    @Column(name = "tier_id", nullable = false)
    Integer tierId;

    /**
     * Зарядка используемая по умолчанию
     */
    @Column(name = "charged_tier_id")
    Integer chargedTierId;

    /**
     * Время запроса на зарядку
     */
    @Column(name = "arr_t")
    Double arrT;

    /**
     * Время зарядки зарядкой по умолчанию, для различных зарядных устройств.
     */
    @Type(type = "jsonb")
    @Column(name = "charg_t", columnDefinition = "jsonb")
    List<Double> chargT;

    /**
     * Нижняя граница предпологаемого прибытия на станцию
     */
    @Column(name = "earliest_arr_T")
    Double earliestArrT;

    /**
     * Обновляемое время прибытия на станцию (нижняя граница)
     * 'res' means 'reset' or 'recalculated
     */
    @Column(name = "res_earliest_arr_t")
    Double resEarliestArrT;

    /**
     * Верхняя граница времени окончания зарядки
     */
    @Column(name = "deadl_t")
    Double deadlT;

    /**
     * Обновлённая верхняя граница времени окончания зарядки
     */
    @Column(name = "res_deadl_t")
    Double resDeadlT;

    /**
     * Используемаый зарядник(pump)
     */
    @Column(name = "pump_id")
    Integer pumpId;

    /**
     * Фактическое время начала зарядки
     */
    @Column(name = "act_start_charge_t")
    Double actStartChargeT;

    /**
     * Фактическое время начала зарядки (черновое)
     */
    @JsonIgnore
    @Transient
    Double draftStartChargeT;

    /**
     * Фактическое время окончания зарядки
     */
    @Column(name = "act_compl_t")
    Double actComplT;

    /**
     * Фактическое время окончания зарядки (черновое)
     */
    @JsonIgnore
    @Transient
    Double draftComplT;

    /**
     * Обновлённое время начала зарядки
     */
    @Column(name = "res_start_charge_t")
    Double resStartChargeT;

    /**
     * Обновлённое время окончания зарядки. compl_t. Если автомобиль зарядился = 0
     */
    @Column(name = "res_compl_t")
    Double resComplT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initialized_data_id")
    @JsonIgnore
    InitializedData initializedData;

//    /**
//     * Используется не родной уровень зарядки(true) или родной (false)
//     */
//    @Column(name = "sharable_state")
//    boolean sharableState;
}
