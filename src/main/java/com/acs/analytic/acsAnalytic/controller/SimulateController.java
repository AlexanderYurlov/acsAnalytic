package com.acs.analytic.acsAnalytic.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.service.simulation.SimulationService;
import com.sun.istack.NotNull;

@RestController
@AllArgsConstructor
public class SimulateController {

    public static final String BASE_PATH = "acs/simulate";
    public static final String TEST = BASE_PATH + "/test";

    public static final String BY_ID = "/{id}";
    public static final String BASE_PATH_ID = BASE_PATH + BY_ID;

    public static final String BASE_PATH_UNOPTIMIZED_ID = BASE_PATH + "/unoptimized" + BY_ID;

    private final SimulationService simulationService;
    private final InitializedDataRepository initializedDataRepository;

    @GetMapping(BASE_PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> getSimulation(@PathVariable @NotNull Long id) {
        ReportDetailsDataDto reportDetailsDataDto = simulationService.getSimulation(id);
        return ResponseEntity.ok(reportDetailsDataDto);
    }

    @GetMapping(BASE_PATH_UNOPTIMIZED_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> getUnoptimizedSimulation(@PathVariable @NotNull Long id) {
        ReportDetailsDataDto reportDetailsDataDto = simulationService.getUnoptimizedSimulation(id);
        return ResponseEntity.ok(reportDetailsDataDto);
    }

    @PostMapping(BASE_PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> removeSimulation(@PathVariable @NotNull Long id) {
        simulationService.removeSimulation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ReportDetailsDataDto>> getAllSimulation() {
        List<InitializedData> initializedData = initializedDataRepository.findAll();
        List<ReportDetailsDataDto> reportDetailsDataDtoList = initializedData.stream()
                .map(ReportDetailsDataDto::new)
                .sorted(Comparator.comparing(ReportDetailsDataDto::getId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reportDetailsDataDtoList);
    }

}
