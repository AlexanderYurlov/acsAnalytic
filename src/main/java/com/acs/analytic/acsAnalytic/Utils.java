package com.acs.analytic.acsAnalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {

    public static double round(double value) {
        return round(value, 2);
    }

    public static double round(double value, Integer places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
