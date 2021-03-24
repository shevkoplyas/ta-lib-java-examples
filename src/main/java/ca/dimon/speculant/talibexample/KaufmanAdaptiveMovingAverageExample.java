package ca.dimon.speculant.talibexample;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.nio.charset.StandardCharsets;  // for readFile( encoding)
import java.nio.file.Files; // for readFile()
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;

// how to parse CSV files in JAVA:
// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
// using OpenCSV:
import com.opencsv.CSVReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import java.util.*; // for ArrayList
import java.util.stream.Collectors;

public class KaufmanAdaptiveMovingAverageExample {

    public Core c;
    public ArrayList<Long> sliding_window_epoch_s = new ArrayList<Long>();
    public ArrayList<Double> sliding_window_price = new ArrayList<Double>();
    public ArrayList<Long> sliding_window_volume = new ArrayList<Long>();
    // The number of periods to average together.

    public int periods_average_fast = 3;
    public int periods_average_medium = 10;
    public int periods_average_slow = 30;

    public MInteger begin_idx_fast = new MInteger();
    public MInteger length_fast = new MInteger();

    public MInteger begin_idx_medium = new MInteger();
    public MInteger length_medium = new MInteger();

    public MInteger begin_idx_slow = new MInteger();
    public MInteger length_slow = new MInteger();

    public double[] out_fast = new double[2000]; // max we need is around 800-900, let's make it twice as much, just in case (input data never "too clean")
    public double[] out_medium = new double[2000]; // max we need is around 800-900, let's make it twice as much, just in case (input data never "too clean")
    public double[] out_slow = new double[2000]; // max we need is around 800-900, let's make it twice as much, just in case (input data never "too clean")

    public Integer day_count = 0;

    PrintWriter pw_result_file;

    private String output_csv_filename = "/tmp/sliding_slit_result.csv";

    public KaufmanAdaptiveMovingAverageExample() {

        // https://stackoverflow.com/questions/8210616/printwriter-append-method-not-appending
        try {
            File file = new File(output_csv_filename);

            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }

            pw_result_file = new PrintWriter(new FileOutputStream(
                    new File(output_csv_filename),
                    true /* append = true */));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        KaufmanAdaptiveMovingAverageExample example = new KaufmanAdaptiveMovingAverageExample();
        example.main2();
    }

    // irb(main):010:0> Date.parse(Time.at(1528833540).to_s).mjd - Date.parse(Time.at(1074868200).to_s).mjd
    // => 5254
    // but in the output last I see is 3000 (even rounded to 1000 should show 5k last, not 3k!
    public void main2() {

        // spy.1_min.conid.756733.trades.csv has following structure:
        // epoch_s,open,high,low,close,volume,barCount,WAP
        // 1074868200,115.0,115.06,115.0,115.04,2722,147,115.037
        String csvFile = "csv/spy.1_min.conid.756733.trades.csv";
        System.out.println("reading file: " + csvFile);
        CSVReader reader = null;
        Integer line_count = 0;

//        sliding_window_epoch_s = new ArrayList<Long>();
//        sliding_window_price = new ArrayList<Double>();
//        sliding_window_volume = new ArrayList<Long>();
        long previous_epoch_s = 0;
        long max_allowed_seconds_gap_inside_one_day = 5 * 60 * 60; // 5 hours in seconds
        long max_delta_epoch_s = 0;
        long max_sliding_window_epoch_s_size = 0;
        long epoch_s_during_max = 0;

        c = new Core();

        try {

            reader = new CSVReader(new FileReader(csvFile));
            String[] line;

            // read CSV file line-by-line
            while ((line = reader.readNext()) != null) {

                // keep track of line numbers (1st line would have number 1)
                line_count++;

                // skip 1st line (headers)
                if (line_count == 1) {
                    continue;
                }

                // get values from CSV line
                long epoch_s = Long.parseLong(line[0]);
                double wap = Double.parseDouble(line[7]);
                long volume = Long.parseLong(line[5]);

                // check if still in the same day, if a new day, then clear all buckets/lists
                long delta_epoch_s = epoch_s - previous_epoch_s;
                if (delta_epoch_s > max_allowed_seconds_gap_inside_one_day) {
                    day_count++; // note: 1st day has day_count==1

                    // keep track of max_delta_epoch_s
                    if (max_delta_epoch_s < delta_epoch_s && delta_epoch_s < 10123456) {
                        max_delta_epoch_s = delta_epoch_s;
                    }

                    // new day started
                    System.out.println("new day started: epoch_s = " + epoch_s + ", previous_epoch_s = " + previous_epoch_s);

                    // process full bucket (if any)
                    if (sliding_window_epoch_s.size() > 0) {
                        // yes we have "previous day" bucket
                        // process "full day"
                        //                        System.out_fast.println("process FULL DAY: sliding_window_epoch_s.size() = " + sliding_window_epoch_s.size());
                        //                        System.out_fast.println("line_count=" + line_count);
                        if (max_sliding_window_epoch_s_size < sliding_window_epoch_s.size()) {
                            max_sliding_window_epoch_s_size = sliding_window_epoch_s.size();
                            epoch_s_during_max = epoch_s;
                        }

                        // ================== Process day (begin) =============
                        process_day();
                        // ================== Process day (end) =============

                        // reset buckets AFTER processing the full day
                        sliding_window_epoch_s.clear();
                        sliding_window_price.clear();
                        sliding_window_volume.clear();
                    }
                }

                // add values into backets
                sliding_window_epoch_s.add(epoch_s);
                sliding_window_price.add(wap);
                sliding_window_volume.add(volume);
                previous_epoch_s = epoch_s;

                if (day_count % 10000 == 0) {
                    System.out.println("day_count : " + day_count);
                }
            } // while (line reader...

            // process last day after loop is over?
            if (sliding_window_epoch_s.size() > 0) {
                // ================== Process day (begin) =============
                pw_result_file.close();
                // ================== Process day (end) =============
            }

//            System.out_fast.println("max_delta_epoch_s: " + max_delta_epoch_s);
//            System.out_fast.println("day_count: " + day_count);
//            System.out_fast.println("max_sliding_window_epoch_s_size: " + max_sliding_window_epoch_s_size);
//            System.out_fast.println("epoch_s_during_max: " + epoch_s_during_max);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Result will be stored to file: " + output_csv_filename);
    }

    private void process_day() {

        // Convert List of doubles to array of float
        // https://stackoverflow.com/a/1565516/7022062
        float[] priceArray = new float[sliding_window_price.size()];
        int index = 0;
        for (double price : sliding_window_price) {
            priceArray[index++] = (float) price;
        }

        RetCode retCode_fast = c.movingAverage(0, priceArray.length - 1, priceArray, periods_average_fast, MAType.Sma, begin_idx_fast, length_fast, out_fast);
        RetCode retCode_medium = c.movingAverage(0, priceArray.length - 1, priceArray, periods_average_medium, MAType.Sma, begin_idx_medium, length_medium, out_medium);
        RetCode retCode_slow = c.movingAverage(0, priceArray.length - 1, priceArray, periods_average_slow, MAType.Sma, begin_idx_slow, length_slow, out_slow);

        if (retCode_fast == RetCode.Success && retCode_medium == RetCode.Success && retCode_slow == RetCode.Success) {
            DecimalFormat df = new DecimalFormat("#.##");  // https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
            df.setRoundingMode(RoundingMode.HALF_UP);

            // df3 same as df, but with 3 decimal points
            DecimalFormat df3 = new DecimalFormat("#.###");  // https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
            df3.setRoundingMode(RoundingMode.HALF_UP);

            StringBuffer csv_result_line = new StringBuffer(350 * 1024); // we'll need rough estimate 200-300k

            int last_idx_fast = begin_idx_fast.value + length_fast.value - 1;
            int last_idx_medium = begin_idx_medium.value + length_medium.value - 1;
            int last_idx_slow = begin_idx_slow.value + length_slow.value - 1;

            // add headers
            if (day_count == 2) {
                csv_result_line.append("epoch_s,price,volume,ma_fast,ma_medium,ma_slow,delta_ma_fast,delta_ma_medium,delta_ma_slow");
                csv_result_line.append(",forecast_fast,forecast_medium,forecast_slow");
                csv_result_line.append("\n");
            }

            // note we start from "+1" value to be able to calculate derivative dMA/dT and normalaze it against current price (dMA/dT)/curr_price
            for (int i = begin_idx_slow.value + 1; i <= (last_idx_slow - periods_average_slow); i++) {
                csv_result_line.append(
                        /* CURRENT TIME / Price / Volume        */
                        sliding_window_epoch_s.get(i)
                        + "," + sliding_window_price.get(i)
                        + "," + sliding_window_volume.get(i)
                        + /* SOME HISTORY                                          */ ","
                        + df.format(out_fast[i - begin_idx_fast.value])
                        + "," + df.format(out_medium[i - begin_idx_medium.value])
                        + "," + df.format(out_slow[i - begin_idx_slow.value])
                        + "," + df3.format((out_fast[i - begin_idx_fast.value] - out_fast[i - begin_idx_fast.value - 1]) / sliding_window_price.get(i) * 100)
                        + "," + df3.format((out_medium[i - begin_idx_medium.value] - out_medium[i - begin_idx_medium.value - 1]) / sliding_window_price.get(i) * 100)
                        + "," + df3.format((out_slow[i - begin_idx_slow.value] - out_slow[i - begin_idx_slow.value - 1]) / sliding_window_price.get(i) * 100)
                        /*  some future */
                        + "," + df3.format(sliding_window_price.get(i + periods_average_fast) - sliding_window_price.get(i))
                        + "," + df3.format(sliding_window_price.get(i + periods_average_medium) - sliding_window_price.get(i))
                        + "," + df3.format(sliding_window_price.get(i + periods_average_slow) - sliding_window_price.get(i))
                        + "\n");
            }

            //System.out.println(csv_result_line.toString());
            pw_result_file.append(csv_result_line.toString());

            System.out.println("debug: result csv str-buf length: " + csv_result_line.toString().length());
//            System.exit(0);
        } else {
            System.out.println("Error during TA-Lib call. Exiting");
            System.exit(0);
        }
    }

    // http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String readFile(String path)
            throws IOException {

        return readFile(path, StandardCharsets.UTF_8);
    }
}
