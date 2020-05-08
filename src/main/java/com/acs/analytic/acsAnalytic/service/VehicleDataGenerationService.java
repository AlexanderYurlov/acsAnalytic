package com.acs.analytic.acsAnalytic.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.acs.analytic.acsAnalytic.model.InitialData;
import com.acs.analytic.acsAnalytic.model.Tier;
import com.acs.analytic.acsAnalytic.model.Vehicle;
import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;

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
        var listR = new ArrayList<>(Collections.nCopies(initialData.getR().size(), 0));
        for (int i = 0; i < initialData.getVehMax(); i++) {
            var vehicle = new Vehicle();

            vehicle.setId(i);

            var vehicleRequestType = getVehicleRequestType(rw, i, initialData.getRw());
            rw = vehicleRequestType == RW ? rw + 1 : rw;
            vehicle.setType(vehicleRequestType);

            var tier = getTier(listR, i, initialData.getR());
            listR.set(tier.getIndex(), listR.get(tier.getIndex()) + 1);
            vehicle.setTierIndex(tier.getIndex());

//             waiting time is within 20% margin from the average
//            deadl_t(i,1)=(0.4*rand+0.8)*Tier_Wait_Aver(tier(i,1));
            var deadlT = (MIN_TIER_WAIT_AV + .2f * random.nextDouble()) * tier.getMaxWaitingTime();
            vehicle.setDeadlT(deadlT);

            List<Double> chargT = getChargT(tier, initialData.getR());
            vehicle.setChargT(chargT);

            if (i == 0) {
                vehicle.setArrT(0d);
            } else {
                Double arrT = vehicles.get(i - 1).getArrT() + generateIa(initialData.getArrivalRate());
                vehicle.setArrT(arrT);
            }
            vehicle.setEArrT(generateEArrT());

            vehicles.add(vehicle);
            log.debug("" + vehicle);
        }

        return vehicles;
    }

    /**
     * Generate earliest arrival time, e.g., time required for vehicle to get to the charging station
     * e_arr_t=5+25*rand(Veh_max,1);
     */
    private Double generateEArrT() {
        return MINIMUM_ARRIVAL_TIME + FACTOR_ARRIVAL_TIME * random.nextDouble();
    }

    /**
     * Generate an exponential random variable - part of the loop (must be repeated). Change the description in the block to multiple values
     * IA = 60*exprnd(1/lambda,Veh_max-1,1); inter arrival interval (IA) follows exponential distribution
     */
    private double generateIa(Float arrivalRate) {
        double x = 1 - random.nextDouble();
        System.out.println("x  = " + x);
        double ia = HOUR * Math.log(x) / (-arrivalRate);
        System.out.println("ia = " + ia);
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
            chargT.add(time);
        }
        return chargT;
    }

    //TODO Make it completely randomly
    private Tier getTier(ArrayList<Integer> listR, int i, List<Tier> r) {
        for (int j = 0; j < r.size(); j++) {
            float ratio = (float) listR.get(j) / (i + 1);
            if (ratio <= r.get(j).getVehicleRatio()) {
                return r.get(j);
            }
        }
        return r.get(r.size() - 1);
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
                .vehMax(1000)
                .rw(0.23f)
                .rr(0.77f)
                .r(List.of(
                        Tier.builder()
                                .index(0)
                                .batteryCapacity(81)
                                .energyAcceptanceRate(120f)
                                .maxWaitingTime(120)
                                .vehicleRatio(.22f)
                                .build(),
                        Tier.builder()
                                .index(1)
                                .batteryCapacity(20)
                                .energyAcceptanceRate(6.6f)
                                .maxWaitingTime(300)
                                .vehicleRatio(.33f)
                                .build(),
                        Tier.builder()
                                .index(2)
                                .batteryCapacity(14)
                                .energyAcceptanceRate(3.3f)
                                .maxWaitingTime(480)
                                .vehicleRatio(.45f)
                                .build()
                        )
                )
                .n(100)
                .pumpTotal(3)
//              .pumpMap()
//                .sharablePumps()
                .arrivalRate(12f)
//                .timeGeneration()
                .n(11)
                .build();
        new VehicleDataGenerationService().generate(initialData);
    }

}
