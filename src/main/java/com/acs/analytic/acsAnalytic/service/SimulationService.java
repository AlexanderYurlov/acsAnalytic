package com.acs.analytic.acsAnalytic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.dao.VehicleRepository;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Service
@AllArgsConstructor
public class SimulationService {

    private final QueueProcessSimulationService queueProcessSimulationService;
    private final InitializedDataRepository initializedDataRepository;
    private final VehicleRepository vehicleRepository;

    public ReportDetailsDataDto simulate(InitializedData initData) {
        InitializedData initializedData = initializedDataRepository.getOne(initData.getId());
        return queueProcessSimulationService.simulate(initializedData);
    }

    public ReportDetailsDataDto getSimulation(Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        List<Vehicle> vehicles = vehicleRepository.findByInitializedDataId(id);
        return new ReportDetailsDataDto(initializedData, vehicles);
    }

}
