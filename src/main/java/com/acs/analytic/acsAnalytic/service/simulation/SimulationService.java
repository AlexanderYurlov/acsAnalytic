package com.acs.analytic.acsAnalytic.service.simulation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.dao.VehicleRepository;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.resp.ReportDetailsDataDto;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

@Service
@AllArgsConstructor
public class SimulationService {

    private final QueueProcessSimulationService queueProcessSimulationService;
    private final QueueProcessUnoptimizedSimulationService queueProcessUnoptimizedSimulationService;
    private final InitializedDataRepository initializedDataRepository;
    private final VehicleRepository vehicleRepository;

    @Async
    @Transactional
    public void simulate(InitialData initialData, TierPumpConf tierPumpConf, List<Vehicle> vehicleList) {
        simulateBaseReportPart(initialData, tierPumpConf, vehicleList, SimulationStatus.COMPLETED);
    }

    @Transactional
    public InitializedData simulateBaseReportPart(InitialData initialData, TierPumpConf tierPumpConf, List<Vehicle> vehicleList, SimulationStatus finalPartState) {
        InitializedData initializedData = initializedDataRepository.save(new InitializedData(initialData, tierPumpConf, vehicleList));
        queueProcessSimulationService.simulateSingleReport(initializedData, finalPartState);
        return initializedData;
    }

    @Transactional
    public ReportDetailsDataDto getSimulation(Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        List<Vehicle> vehicles = vehicleRepository.findByInitializedDataId(id);
        return new ReportDetailsDataDto(initializedData, vehicles);
    }

    public void removeSimulation(Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        initializedDataRepository.delete(initializedData);
    }

    @Transactional
    public ReportDetailsDataDto getUnoptimizedSimulation(Long id) {
        InitializedData initializedData = initializedDataRepository.getOne(id);
        return queueProcessUnoptimizedSimulationService.simulate(prepareData(initializedData));
    }

    private InitializedData prepareData(InitializedData initializedData) {
        InitializedData initData = new InitializedData();
        List<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle vehicle : initializedData.getVehicles()) {
            Vehicle veh = new Vehicle();
            veh.setId(vehicle.getId());
            veh.setTierId(vehicle.getTierId());
            veh.setType(vehicle.getType());
            veh.setArrT(vehicle.getArrT());
            veh.setDeadlT(vehicle.getDeadlT());
            veh.setChargT(vehicle.getChargT());
            veh.setEarliestArrT(vehicle.getEarliestArrT());
            vehicles.add(veh);
        }
        initData.setVehicles(vehicles);
        initData.setTierPumps(initializedData.getTierPumps());
        initData.setInitialData(initializedData.getInitialData());
        return initData;
    }
}
