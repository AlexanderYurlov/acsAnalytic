package com.acs.analytic.acsAnalytic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.acs.analytic.acsAnalytic.model.PumpDataInitialization;
import com.acs.analytic.acsAnalytic.service.AcsService;

@RestController
public class AcsController {

    public static final String BASE_PATH = "acs";

    AcsService acsService;

    @GetMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public PumpDataInitialization checkget() {
        System.out.println("checked");
        PumpDataInitialization pumpDataInitialization = PumpDataInitialization.builder().n(100).build();
        System.out.println(pumpDataInitialization);
        return pumpDataInitialization;
    }

    @PostMapping(BASE_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void check(@RequestBody String body) {
        acsService.updateCalculationData(body);
    }

}
