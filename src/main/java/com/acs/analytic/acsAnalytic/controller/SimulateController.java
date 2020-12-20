package com.acs.analytic.acsAnalytic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.service.QueueProcessSimulationService;
import com.sun.istack.NotNull;

@RestController
@AllArgsConstructor
public class SimulateController {

    public static final String BASE_PATH = "simulate";
    public static final String TEST = BASE_PATH + "/test";

    public static final String BY_ID = "/{id}";
    public static final String GET_BY_ID = BASE_PATH + BY_ID;

    private final InitializedDataRepository initializedDataRepository;
    private final QueueProcessSimulationService queueProcessSimulationService;

    @PostMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> simulate(@RequestBody InitializedData initData) {
        InitializedData initializedData = initializedDataRepository.getOne(initData.getId());
        return ResponseEntity.ok(queueProcessSimulationService.simulate(initializedData.getVehicles(), new TierPumpConf(initializedData.getInitialData(), initializedData.getTierPumps())));
    }

    //todo сделать получени
    @GetMapping(GET_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> getSimulation(@PathVariable @NotNull Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        return ResponseEntity.ok(queueProcessSimulationService.simulate(initializedData.getVehicles(), new TierPumpConf(initializedData.getInitialData(), initializedData.getTierPumps())));
    }

    @GetMapping(TEST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> check() {
        return ResponseEntity.ok(queueProcessSimulationService.simulateTest());
    }

}
