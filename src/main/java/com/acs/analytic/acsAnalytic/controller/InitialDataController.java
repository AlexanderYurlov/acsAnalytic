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

import com.acs.analytic.acsAnalytic.controller.utils.MockUtils;
import com.acs.analytic.acsAnalytic.dto.InitialDataDto;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.CsvReadService;
import com.acs.analytic.acsAnalytic.service.PumpDataGenerationService;
import com.acs.analytic.acsAnalytic.service.simulation.SimulationService;
import com.acs.analytic.acsAnalytic.service.VehicleDataGenerationService;

@RestController
@AllArgsConstructor
public class InitialDataController {

    public static final String BASE_PATH = "acs/init_data";
    public static final String BY_ID = "/{id}";
    public static final String GET_BY_ID = BASE_PATH + BY_ID;
    public static final String GENERATE_DATA_TEST = BASE_PATH + "/test";
    public static final String GENERATE_DATA_CSV = BASE_PATH + "/csv";

    private final PumpDataGenerationService pumpDataGenerationService;
    private final VehicleDataGenerationService vehicleDataGenerationService;
    private final SimulationService simulationService;
    private final CsvReadService csvReadService;

    @PostMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> generateInitializedData(@RequestBody InitialDataDto dto) {
        InitialData initialData = new InitialData(dto);
        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(initialData);
        List<Vehicle> vehicleList = vehicleDataGenerationService.generate(initialData);
        simulationService.simulate(initialData, tierPumpConf, vehicleList);
        return ResponseEntity.ok().build();
    }

//    @GetMapping(BASE_PATH)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<List<InitializedData>> getAllInitializedData() {
//        List<InitializedData> list = initializedDataRepository.findAll();
//        return ResponseEntity.ok(list);
//    }
//
//    @GetMapping(GET_BY_ID)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<InitializedData> getInitializedData(@PathVariable @NotNull Long id) {
//        InitializedData initializedData = initializedDataRepository.getOne(id);
//        return ResponseEntity.ok(initializedData);
//    }

    @GetMapping(GENERATE_DATA_TEST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> generateDataTest() {
        InitialDataDto initialData = MockUtils.getInitialDataDto();
        return generateInitializedData(initialData);
    }

    @GetMapping(GENERATE_DATA_CSV)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportDetailsDataDto> check() {
        InitialDataDto dto = csvReadService.getInitialDataDto();
        InitialData initialData = new InitialData(dto);
        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(initialData);
        List<Vehicle> vehicleList = csvReadService.read();
        simulationService.simulate(initialData, tierPumpConf, vehicleList);
        return ResponseEntity.ok().build();
    }

}
