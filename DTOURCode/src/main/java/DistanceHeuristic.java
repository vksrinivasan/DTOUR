public class DistanceHeuristic implements Heuristic {

    // Empirically Based
    double minMile = 0.04;
    double maxMile = 13.0;

    // Heuristics
    double minCost = 0.05;
    double maxCost = 0.10;

    /* Let's try to map distance of curr node (src) to destination node (actual target node) to probability space */
    public double computeHeuristic(double src_long, double dest_long, double src_lat, double dest_lat) {

        /* Used this source to get equation for distance based on Long/Lat:
         * http://www.meridianworlddata.com/distance-calculation/
         */
        double x = 69.1*(dest_lat - src_lat);
        double y = 53.0*(dest_long - src_long);
        double mile_distance = Math.sqrt(((x*x) + (y*y))); // approximate

        // Now map mile distance to cost range
        double cost = ((maxCost - minCost)*(mile_distance - minMile)/(maxMile - minMile)) + minCost;
        return cost;
    }

    public static double computeDistance(double src_long, double dest_long, double src_lat, double dest_lat) {

        /* Used this source to get equation for distance based on Long/Lat:
         * http://www.meridianworlddata.com/distance-calculation/
         */
        double x = 69.1*(dest_lat - src_lat);
        double y = 53.0*(dest_long - src_long);
        double mile_distance = Math.sqrt(((x*x) + (y*y))); // approximate

        return mile_distance;
    }

//    public double calcCost(double distance) {
//        return ((maxCost - minCost)*(distance - minMile)/(maxMile - minMile)) + minCost;
//    }
//
//    public static void main(String[] args) {
//        DistanceHeuristic myDH = new DistanceHeuristic();
//        double[] test = {0.04, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0};
//        for(double temp : test) {
//            double cost = myDH.calcCost(temp);
//            System.out.println(cost);
//        }
//    }
}
