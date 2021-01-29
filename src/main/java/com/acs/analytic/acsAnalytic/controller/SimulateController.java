package com.acs.analytic.acsAnalytic.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.service.QueueProcessSimulationService;
import com.acs.analytic.acsAnalytic.service.SimulationService;
import com.sun.istack.NotNull;

@RestController
@AllArgsConstructor
public class SimulateController {

    public static final String BASE_PATH = "acs/simulate";
    public static final String TEST = BASE_PATH + "/test";

    public static final String BY_ID = "/{id}";
    public static final String GET_BY_ID = BASE_PATH + BY_ID;

    private final SimulationService simulationService;
    private final InitializedDataRepository initializedDataRepository;
    private final QueueProcessSimulationService queueProcessSimulationService;

//    @PostMapping(BASE_PATH)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<ReportDetailsDataDto> simulate(@RequestBody InitializedData initData) {
//        return ResponseEntity.ok(simulationService.simulate(initData));
//    }

    @GetMapping(GET_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> getSimulation(@PathVariable @NotNull Long id) {
        ReportDetailsDataDto reportDetailsDataDto = simulationService.getSimulation(id);
        return ResponseEntity.ok(reportDetailsDataDto);
    }

    @GetMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ReportDetailsDataDto>> getAllSimulation() {
        List<InitializedData> initializedData = initializedDataRepository.findAll();
        List<ReportDetailsDataDto> reportDetailsDataDtoList = initializedData.stream()
                .map(ReportDetailsDataDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDetailsDataDtoList);
    }

    @GetMapping(TEST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> check() {
        return ResponseEntity.ok(queueProcessSimulationService.simulateTest());
    }

}
