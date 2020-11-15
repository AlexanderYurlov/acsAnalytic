package com.acs.analytic.acsAnalytic.model.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SimulationStatus {

    COMPLETED("COMPLETED"),
    IN_PROGRESS("IN_PROGRESS");

    public static final Map<String, SimulationStatus> BY_CODE;

    static {

        var byCode = new HashMap<String, SimulationStatus>();
        for (var value : values()) {
            byCode.put(value.code, value);
        }

        BY_CODE = Collections.unmodifiableMap(byCode);
    }

    private final String code;

    SimulationStatus(String code) {
        this.code = code;
    }

    @JsonCreator
    public static SimulationStatus fromValue(String code) {
        var value = BY_CODE.get(code);
        if (value == null) {
            throw new IllegalArgumentException("Unknown code: " + code);
        }
        return value;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
