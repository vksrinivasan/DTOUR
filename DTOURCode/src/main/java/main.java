import java.util.HashMap;
import java.util.*;

public class main {
    public static void main(String[] args) {

        double latitude_source;
        double longitude_source;
        double latitude_dest;
        double longitude_dest;

        if(args.length != 4) {
            throw new IllegalArgumentException("Must supply 4 arguments: latitude_src longitude_src latitude_dest longitude_dest");
        }

        try {
            latitude_source = Double.parseDouble(args[0]);
        } catch (NumberFormatException nbfe) {
            System.out.println("double required (latitude_src). Try to run program again.");
            return;
        }

        try {
            longitude_source = Double.parseDouble(args[1]);
        } catch (NumberFormatException nbfe) {
            System.out.println("double required (longitude_src). Try to run program again.");
            return;
        }

        try {
            latitude_dest = Double.parseDouble(args[2]);
        } catch (NumberFormatException nbfe) {
            System.out.println("double required (latitude_dest). Try to run program again.");
            return;
        }

        try {
            longitude_dest = Double.parseDouble(args[3]);
        } catch (NumberFormatException nbfe) {
            System.out.println("double required (longitude_dest). Try to run program again.");
            return;
        }

        LongLat sourceLongLat = new LongLat(longitude_source, latitude_source);
        LongLat destLongLat = new LongLat(longitude_dest, latitude_dest);
        mainHarness.runCode(sourceLongLat, destLongLat);

    }
}