package com.acs.analytic.acsAnalytic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.acs.analytic.acsAnalytic.model.enums.VehicleRequestType;
import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;

public class UtilsCsv {


    public static List<Vehicle> readCsv() {
        final String CSV_SEPARATOR = "\t";
        List<Vehicle> vehicles = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("test1.csv"));

            // skip first string
            reader.readLine();

            while (reader.ready()) {
                String vehStr = reader.readLine();
                String[] str = vehStr.split(CSV_SEPARATOR);
                vehicles.add(Vehicle.builder()
                        .id(Integer.valueOf(str[0]))
                        .type(getType(str[1]))
                        .tierId(Integer.valueOf(str[2]))
                        .arrT(Double.valueOf(str[3].replaceAll(",", ".")))
                        .earliestArrT(Double.valueOf(str[4].replaceAll(",", ".")))
                        .deadlT(Double.valueOf(str[5].replaceAll(",", ".")))
                        .chargT(List.of(
                                Double.valueOf(str[6].replaceAll(",", ".")),
                                Double.valueOf(str[7].replaceAll(",", ".")),
                                Double.valueOf(str[8].replaceAll(",", "."))))
                        .build());
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public static void writeToCSV(List<Vehicle> Vehicle) {
        final String CSV_SEPARATOR = ",";
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("vehicle.csv"), StandardCharsets.UTF_8));
            bw.write("Id" + CSV_SEPARATOR +
                    "Type" + CSV_SEPARATOR +
                    "TierId" + CSV_SEPARATOR +
                    "ArrT" + CSV_SEPARATOR +
                    "ChargT" + CSV_SEPARATOR +
                    "EArrT" + CSV_SEPARATOR +
                    "resEarliestArrT" + CSV_SEPARATOR +
                    "DeadlT" + CSV_SEPARATOR +
                    "resDeadlT" + CSV_SEPARATOR +
                    "Pump" + CSV_SEPARATOR +
                    "actStartChargeT" + CSV_SEPARATOR +
                    "resStartChargeT" + CSV_SEPARATOR +
                    "actComplT" + CSV_SEPARATOR +
                    "resComplT"
            );
            bw.newLine();
            for (Vehicle vehicle : Vehicle) {
                String oneLine = (vehicle.getId() == null ? "" : vehicle.getId()) +
                        CSV_SEPARATOR +
                        (vehicle.getType() == null ? "" : vehicle.getType()) +
                        CSV_SEPARATOR +
                        (vehicle.getTierId() == null ? "" : vehicle.getTierId()) +
                        CSV_SEPARATOR +
                        (vehicle.getArrT() == null ? "" : vehicle.getArrT()) +
                        CSV_SEPARATOR +
                        (vehicle.getChargT() == null ? "" : vehicle.getChargT().stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"))) +
                        CSV_SEPARATOR +
                        (vehicle.getEarliestArrT() == null ? "" : vehicle.getEarliestArrT()) +
                        CSV_SEPARATOR +
                        (vehicle.getResEarliestArrT() == null ? "" : vehicle.getResEarliestArrT()) +
                        CSV_SEPARATOR +
                        (vehicle.getDeadlT() == null ? "" : vehicle.getDeadlT()) +
                        CSV_SEPARATOR +
                        (vehicle.getResDeadlT() == null ? "" : vehicle.getResDeadlT()) +
                        CSV_SEPARATOR +
                        (vehicle.getPump() == null ? "" : vehicle.getPump()) +
                        CSV_SEPARATOR +
                        (vehicle.getActStartChargeT() == null ? "" : vehicle.getActStartChargeT()) +
                        CSV_SEPARATOR +
                        (vehicle.getResStartChargeT() == null ? "" : vehicle.getResStartChargeT()) +
                        CSV_SEPARATOR +
                        (vehicle.getActComplT() == null ? "" : vehicle.getActComplT()) +
                        CSV_SEPARATOR +
                        (vehicle.getResComplT() == null ? "" : vehicle.getResComplT());
                System.out.println(oneLine);
                bw.write(oneLine);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException ignored) {
        }
    }

    private static VehicleRequestType getType(String s) {
        if (s.equals("1")) {
            return VehicleRequestType.RR;
        } else {
            return VehicleRequestType.RW;
        }

    }
}
