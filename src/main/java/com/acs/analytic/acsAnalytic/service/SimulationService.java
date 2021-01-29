package com.acs.analytic.acsAnalytic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.dao.VehicleRepository;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Service
@AllArgsConstructor
public class SimulationService {

    private final QueueProcessSimulationService queueProcessSimulationService;
    private final InitializedDataRepository initializedDataRepository;
    private final VehicleRepository vehicleRepository;

//    public ReportDetailsDataDto simulate(InitializedData initData) {
    @Transactional
    public ReportDetailsDataDto simulate(InitialData initialData, TierPumpConf tierPumpConf, List<Vehicle> vehicleList) {
//        InitializedData initializedData = initializedDataRepository.save(new InitializedData(initialData, tierPumpConf, vehicleList));
//        InitializedData initializedData = initializedDataRepository.getOne(initData.getId());
//        return queueProcessSimulationService.simulate(initializedData);
        return queueProcessSimulationService.simulate(new InitializedData(initialData, tierPumpConf, vehicleList));
    }

    @Transactional
    public ReportDetailsDataDto getSimulation(Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        List<Vehicle> vehicles = vehicleRepository.findByInitializedDataId(id);
        return new ReportDetailsDataDto(initializedData, vehicles);
    }

}
