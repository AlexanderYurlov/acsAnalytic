package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.TierVehicle;
import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

import static com.acs.analytic.acsAnalytic.utils.Utils.round;
import static com.acs.analytic.acsAnalytic.utils.UtilsCsv.writeToCSV;
import static com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType.RR;
import static com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType.RW;

@Slf4j
@Service
public class VehicleDataGenerationService {

    private static final Integer HOUR = 60;
    private static final Integer MINIMUM_ARRIVAL_TIME = 5;
    private static final Integer FACTOR_ARRIVAL_TIME = 25;
    private static final Float MIN_BAT_CAPACITY = .7f;
    private static final Float MIN_TIER_WAIT_AV = .8f;
    private static final Random random = new Random();

    public List<Vehicle> generate(InitialData initialData) {

        List<Vehicle> vehicles = new ArrayList(initialData.getVehMax());

        var rw = 0;
        Map<Integer, Integer> mapR = initialData.getTiers().stream().collect(Collectors.toMap(Tier::getId, x -> 0));
        for (int i = 0; i < initialData.getVehMax(); i++) {
            var vehicle = new Vehicle();

            vehicle.setId(i + 1);

            var vehicleRequestType = getVehicleRequestType(rw, i, initialData.getRw());
            rw = vehicleRequestType == RW ? rw + 1 : rw;
            vehicle.setType(vehicleRequestType);

            var tier = getTier(mapR, i, initialData);
            mapR.put(tier.getId(), mapR.get(tier.getId()) + 1);
            vehicle.setTierId(tier.getId());

//             waiting time is within 20% margin from the average
//            deadl_t(i,1)=(0.4*rand+0.8)*Tier_Wait_Aver(tier(i,1));
            var deadlT = (MIN_TIER_WAIT_AV + .2f * random.nextDouble()) * tier.getMaxWaitingTime();
            vehicle.setDeadlT(round(deadlT));

            List<Double> chargT = getChargT(tier, initialData.getTiers());
            vehicle.setChargT(chargT);

            if (i == 0) {
                vehicle.setArrT(0d);
            } else {
                Double arrT = vehicles.get(i - 1).getArrT() + generateIa(initialData.getArrivalRate());
                vehicle.setArrT(round(arrT));
            }
            vehicle.setEarliestArrT(generateEArrT());

            vehicles.add(vehicle);
//            log.debug("" + vehicle);
        }

        return vehicles;
    }

    /**
     * Generate earliest arrival time, e.g., time required for vehicle to get to the charging station
     * e_arr_t=5+25*rand(Veh_max,1);
     */
    private Double generateEArrT() {
        return round(MINIMUM_ARRIVAL_TIME + FACTOR_ARRIVAL_TIME * random.nextDouble());
    }

    /**
     * Generate an exponential random variable - part of the loop (must be repeated). Change the description in the block to multiple values
     * IA = 60*exprnd(1/lambda,Veh_max-1,1); inter arrival interval (IA) follows exponential distribution
     */
    private double generateIa(Float arrivalRate) {
        double x = 1 - random.nextDouble();
        double ia = HOUR * Math.log(x) / (-arrivalRate);
        return ia;
    }

    private List<Double> getChargT(Tier tier, List<Tier> r) {

//            en_dem is 70-90% of tier's Bat_cap
//            en_dem(i,1)=(0.2*rand+0.7)*Bat_cap(tier(i,1));
        var enDem = (MIN_BAT_CAPACITY + .2f * random.nextDouble()) * tier.getBatteryCapacity();

        List<Double> chargT = new ArrayList<>();
        for (Tier value : r) {
//                charg_t(i,n)=60*en_dem(i,1)/min(Accep_rate(n),Accep_rate(tier(i,1)));
            Float energyAcceptanceRateMinimum = value.getEnergyAcceptanceRate() < tier.getEnergyAcceptanceRate() ?
                    value.getEnergyAcceptanceRate() : tier.getEnergyAcceptanceRate();
            Double time = HOUR * enDem / energyAcceptanceRateMinimum;
            chargT.add(round(time));
        }
        return chargT;
    }

    //TODO Make it completely randomly
    private Tier getTier(Map<Integer, Integer> mapR, int i, InitialData initialData) {
        List<TierVehicle> r = initialData.getR();
        for (TierVehicle vt : r) {
            float ratio = (float) mapR.get(vt.getTierIndex()) / (i + 1);
            if (ratio <= vt.getVehicleRatio()) {
                return initialData.getTierByIndex(vt.getTierIndex());
            }
        }
        return initialData.getTierByIndex(r.get(r.size() - 1).getTierIndex());
    }

    //TODO Make it completely randomly
    private VehicleRequestType getVehicleRequestType(Integer rw, int i, float initialDataRw) {
        float rwRatio = (float) rw / (i + 1);
        if (rwRatio <= initialDataRw) {
            return RW;
        } else {
            return RR;
        }
    }

    public static void main(String[] args) {
        InitialData initialData = InitialData.builder()
                .tiers(List.of(
                        Tier.builder()
                                .id(1)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(120)
                                .build(),
                        Tier.builder()
                                .id(2)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(300)
                                .build(),
                        Tier.builder()
                                .id(3)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(480)
                                .build()

                        )
                )
                .vehMax(100)
                .rw(0.23f)
                .rr(0.77f)
                .r(List.of(
                        TierVehicle.builder()
                                .vehicleRatio(.22f)
                                .tierIndex(1)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.33f)
                                .tierIndex(2)
                                .build(),
                        TierVehicle.builder()
                                .vehicleRatio(.45f)
                                .tierIndex(3)
                                .build()
                        )
                )
//                .n(100)
//                .pumpTotal(3)
//              .pumpMap()
//                .sharablePumps()
                .arrivalRate(12f)
//                .timeGeneration()
//                .n(11)
                .build();
        List<Vehicle> vehicles = new VehicleDataGenerationService().generate(initialData);
        writeToCSV(vehicles);
    }

}
