import java.util.*;

public class NullHeuristic_Implement implements Heuristic {

    /* Should result in A* being identical to Djikstra's - In fact, we should
     * probably write this so that we can just re-use each other's code
     */
    public double computeHeuristic(double src_long, double dest_long, double src_lat, double dest_lat) {
        return 0;
    }
}
