package com.acs.analytic.acsAnalytic.service.simulation.fullreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import com.acs.analytic.acsAnalytic.dao.InitializedDataRepository;
import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.InitializedData;
import com.acs.analytic.acsAnalytic.model.ReportData;
import com.acs.analytic.acsAnalytic.model.TierPumpConf;
import com.acs.analytic.acsAnalytic.model.dto.SharableConfDto;
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
    private final SimulationService simulationService;
    private final InitializedDataRepository initializedDataRepository;

    @Async
    @Transactional
    public void simulate(InitialData initialData, List<Vehicle> vehicleList) {
        Date startTime = new Date();

        TierPumpConf tierPumpConf = pumpDataGenerationService.generate(initialData);
        InitializedData initializedData = simulationService.simulateBaseReportPart(initialData, tierPumpConf, vehicleList, SimulationStatus.IN_PROGRESS);

        List<SharableConfDto> sharableConfs = new ArrayList<>();
        List<TierPumpConf> tierPumpConfs = pumpDataGenerationService.generateList(initialData);
        for (TierPumpConf tpConf : tierPumpConfs) {
            List<Vehicle> vehicles = queueProcessSimulationService.simulatePartOfMultiReport(initializedData, tpConf);
            SharableConfDto sharableConf = new SharableConfDto(tpConf, vehicles);
            sharableConfs.add(sharableConf);
        }
        ReportData rp = ReportData.builder()
                .sharableConf(sharableConfs)
                .initializedData(initializedData)
                .build();
        Date endTime = new Date();
        initializedData.setStatus(SimulationStatus.COMPLETED);
        initializedData.setStartTime(startTime);
        initializedData.setEndTime(endTime);
        initializedData.setReportData(rp);
        initializedDataRepository.save(initializedData);
        System.out.println("Total full report execution time: " + (endTime.getTime() - startTime.getTime()) + "ms");
    }
}
