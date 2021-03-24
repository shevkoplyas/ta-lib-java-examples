package ca.dimon.speculant.talibexample;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SimpleMovingAverageExample {

    /**
     * The total number of periods to generate data for.
     */
    public static final int TOTAL_PERIODS = 30;

    /**
     * The number of periods to average together.
     */
    public static final int PERIODS_AVERAGE = 10;

    public static void main(String[] args) {
        double[] out_sma = new double[TOTAL_PERIODS];
        double[] out_ema = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

//        // create array of prices        
//        double[] closePrice = new double[TOTAL_PERIODS];
//        /* class MInteger is simply a 'Boxing' of an int.
//        * see: https://github.com/BYVoid/TA-Lib/blob/master/src/main/java/com/tictactec/ta/lib/MInteger.java
//        * Used instead of auto-boxing because it provides a "mutable" Integer 
//        * object.
//         */
//        // fill in closePrice array
//        for (int i = 0; i < closePrice.length; i++) {
//            closePrice[i] = (double) i;
//            if (i == 15) closePrice[i] = (double) 30;
//        }
// based on this article:
// https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages
// I used following 30 closePrice values to test Ta-lib EMA (vs SMA) and it shows a good matching values to expected ones
// see also my google xls: "debugging Ta-lib (java) for speculant"
// https://docs.google.com/spreadsheets/d/1JDLc1X1zPSWqKnut10nltimgoGzz9KMG5q6XpdRz6hw/edit#gid=75680168
        double[] closePrice = {
            22.27,
            22.19,
            22.08,
            22.17,
            22.18,
            22.13,
            22.23,
            22.43,
            22.24,
            22.29,
            22.15,
            22.39,
            22.38,
            22.61,
            23.36,
            24.05,
            23.75,
            23.83,
            23.95,
            23.63,
            23.82, // + 100,  // we can indroduce anomaly "price spike" and see how EMA vs SMA react to it in time
            23.87,
            23.65,
            23.19,
            23.10,
            23.33,
            22.68,
            23.10,
            22.40,
            22.17
        };

        Core c = new Core();
        int startIdx = 0;
        int endIdx = closePrice.length - 1;

        // Uncomment next line to try to get only 1 (last) EMA and SMA values (they'll be incorrectly the same):
        //startIdx = endIdx;
        RetCode retCodeSma = c.sma(startIdx, endIdx, closePrice, PERIODS_AVERAGE, begin, length, out_sma);
        RetCode retCodeEma = c.ema(startIdx, endIdx, closePrice, PERIODS_AVERAGE, begin, length, out_ema);

        if (retCodeSma == RetCode.Success && retCodeEma == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output length: " + length.value);
            System.out.println("Output End Period: " + (begin.value + length.value - 1));

            for (int i = 0; i < TOTAL_PERIODS; i++) {
                StringBuilder line = new StringBuilder();
                line.append("Period #");
                line.append(i);
                line.append(" close=");
                line.append(closePrice[i]);
                if (i >= begin.value) {
                    line.append(" sma=");
                    line.append(round(out_sma[i - begin.value], 2));
                    line.append(" ema=");
                    line.append(round(out_ema[i - begin.value], 2));
                }
                System.out.println(line.toString());
            }
        } else {
            System.out.println("Error: retCodeSma: " + retCodeSma + ", retCodeEma: " + retCodeEma);
        }
    }

    // round() came from https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    // in particular from accepted answer: https://stackoverflow.com/a/2808648/7022062
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
