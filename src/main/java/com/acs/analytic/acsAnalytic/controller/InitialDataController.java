package com.acs.analytic.acsAnalytic.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.controller.utils.MockUtils;
import com.acs.analytic.acsAnalytic.dao.InitialDataRepository;
import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.PumpDataGenerationService;
import com.acs.analytic.acsAnalytic.service.VehicleDataGenerationService;
import com.sun.istack.NotNull;

@RestController
@AllArgsConstructor
public class InitialDataController {

    public static final String BASE_PATH = "init_data";
    public static final String BY_ID = "/{id}";
    public static final String GET_BY_ID = BASE_PATH + BY_ID;
    public static final String GENERATE_DATA_TEST = BASE_PATH + "/test";

    private final PumpDataGenerationService pumpDataGenerationService;
    private final VehicleDataGenerationService vehicleDataGenerationService;
    private final InitializedDataRepository initializedDataRepository;
    private final InitialDataRepository initialDataRepository;

    @PostMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InitializedData> generateInitializedData(@RequestBody InitialData initialData) {
        initialDataRepository.save(initialData);
        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(initialData);
        List<Vehicle> vehicleList = vehicleDataGenerationService.generate(initialData);
        InitializedData initializedData = initializedDataRepository.save(new InitializedData(initialData, tierPumpConf, vehicleList));
        return ResponseEntity.ok(initializedData);
    }

    @GetMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<InitializedData>> getAllInitializedData() {
        List<InitializedData> list = initializedDataRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping(GET_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InitializedData> getInitializedData(@PathVariable @NotNull Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        return ResponseEntity.ok(initializedData);
    }

    @GetMapping(GENERATE_DATA_TEST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InitializedData> generateDataTest() {
        InitialData initialData = MockUtils.getInitialData();
        return generateInitializedData(initialData);
    }

}
