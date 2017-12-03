import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Simulation {

    static class LLTEST {
        double Longitude;
        double Latitude;

        LLTEST(String coordinate) {
            String adjusted = coordinate.replace("(", "");
            adjusted = adjusted.replace(")", "");

            String[] adjCoord = adjusted.split(",");

            Longitude = Double.parseDouble(adjCoord[1]);
            Latitude = Double.parseDouble(adjCoord[0]);
        }
    }

    static void printTimings(String filename, ArrayList<Long> timings) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            for(Long timing : timings) {
                writer.println(timing);
            }
            writer.close();
        }
        catch (Exception e) {
            System.out.println("WHAT!?");
        }
    }

    public static void main(String[] args) {
        ArrayList<Long> timingsList = new ArrayList<Long>();
        String filename = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\pathOut.txt";
        String outname = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\runTimes.txt";

        // Just use one datasource
        DataSource mySource = new MySqlDataSource();
        mySource.initSource();

        int counter = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArr = line.split("\t");
                String startLL = lineArr[1];
                String endLL = lineArr[lineArr.length-1];

                LLTEST start = new LLTEST(startLL);
                LLTEST end = new LLTEST(endLL);

                LongLat startExpected = new LongLat(start.Longitude, start.Latitude);
                LongLat endExpected = new LongLat(end.Longitude, end.Latitude);

                // Time it
                final long startTime = System.currentTimeMillis();
                try {
                    mainHarness.runCode(startExpected, endExpected, mySource);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                final long endTime = (System.currentTimeMillis() - startTime)/(1000L);
                System.out.println(endTime);

                timingsList.add(endTime);
                counter += 1;
                System.out.println("Counter is: " + Integer.toString(counter));
                if(counter >= 500) {
                    break;
                }

            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        printTimings(outname, timingsList);
        mySource.closeSource();

    }
}
