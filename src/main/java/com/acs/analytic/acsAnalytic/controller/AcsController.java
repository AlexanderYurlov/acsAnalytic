package com.acs.analytic.acsAnalytic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.service.AcsService;

@RestController
public class AcsController {

    public static final String BASE_PATH = "acs";

    private AcsService acsService;

    @GetMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public InitialData checkget() {
        System.out.println("checked");
        return InitialData.builder()
//              .pumpMap()
//                .sharablePumps()
                .arrivalRate(0.77f)
//                .timeGeneration()
//                .r(22)
                .rr(0.77f)
                .rw(0.23f)
                .build();
    }

    @PostMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void check(@RequestBody InitialData body) {

        System.out.println(body);
//        acsService.updateCalculationData(body);
    }

}
