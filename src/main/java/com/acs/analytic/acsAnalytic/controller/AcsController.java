package com.acs.analytic.acsAnalytic.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.PumpDataGenerationService;
import com.acs.analytic.acsAnalytic.service.QueueProcessSimulationService;
import com.acs.analytic.acsAnalytic.service.VehicleDataGenerationService;

@RestController
@AllArgsConstructor
public class AcsController {

    public static final String BASE_PATH = "acs";
    public static final String SIMULATE = BASE_PATH + "/simulate";
    public static final String TEST = BASE_PATH + "/test";

    private final QueueProcessSimulationService queueProcessSimulationService;
    private final PumpDataGenerationService pumpDataGenerationService;
    private final VehicleDataGenerationService vehicleDataGenerationService;

    @PostMapping(SIMULATE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> simulate(@RequestBody InitialData body) {
        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(body);
        List<Vehicle> vehicleList = vehicleDataGenerationService.generate(body);
        return ResponseEntity.ok(queueProcessSimulationService.simulate(vehicleList, tierPumpConf));
    }

    @GetMapping(TEST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> check() {
        return ResponseEntity.ok(queueProcessSimulationService.simulateTest());
    }

}
