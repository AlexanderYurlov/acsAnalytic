package com.acs.analytic.acsAnalytic.service.simulation.fullreport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.ReportData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.enums.SimulationStatus;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;
import com.acs.analytic.acsAnalytic.service.PumpDataGenerationService;
import com.acs.analytic.acsAnalytic.service.simulation.QueueProcessSimulationService;
import com.acs.analytic.acsAnalytic.service.simulation.SimulationService;

@Service
@AllArgsConstructor
public class FullReportSimulationService {

    private final QueueProcessSimulationService queueProcessSimulationService;
    private final PumpDataGenerationService pumpDataGenerationService;
    private final TierPumpConfGenerator tierPumpConfGenerator;
    private final SimulationService simulationService;

    @Async
    public void simulate(InitialData initialData, List<Vehicle> vehicleList) {
        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(initialData);
        InitializedData InitializedData = simulationService.simulateBaseReportPart(initialData, tierPumpConf, vehicleList, SimulationStatus.IN_PROGRESS);
        List<Vehicle> originVehicles = InitializedData.getVehicles();
        ReportData originReportData = calculateReport(originVehicles);

        List<ReportData> reportDataList = new ArrayList<>();
        reportDataList.add(originReportData);
        List<TierPumpConf> tierPumpConfs = tierPumpConfGenerator.generate(tierPumpConf);
        for (TierPumpConf tpConf : tierPumpConfs) {
            List<Vehicle> vehicles = queueProcessSimulationService.simulatePartOfMultiReport(InitializedData, tpConf);
            ReportData rp = calculateReport(vehicles);
            reportDataList.add(rp);
        }

    }

    private ReportData calculateReport(List<Vehicle> vehicles) {
        return null;
    }
}
