import java.util.List;

public class Main {
    public static void main(String[] args) {

        DataSource mySource = new StaticData_Implement();
        mySource.initSource();
        double []NodeLongLat = mySource.getGeoList(); // We will keep longitudes/latitudes in memory (only ~1000 nodes)
        Heuristic myHeuristic = new DistanceHeuristic();
        PathGraph myPathGraph = new PathGraph();
        AStar myAlgo = new AStar(0,6, mySource, myHeuristic, myPathGraph, NodeLongLat);

        // Time it and keep track of A* iteration
        final long startTime = System.currentTimeMillis();
        int iteration = 0;

        // Currently the A* code stops running once it finds a path to the destination - I have this while loop here
        // to keep running it until it completes (finds shortest/most probabilistic path to destination), but we can
        // remove this loop and just run it once. Or we could try to implement the feedback scheduling from the paper.
        while(!myAlgo.searchComplete) {
            System.out.println("Curr Iteration: " + iteration);
            myAlgo.runAlgo();
            iteration++;
        }
        List<Integer> order = myAlgo.reconstructPath();

        // Finish timing
        final long endTime = System.currentTimeMillis();

        for(Integer e : order) { System.out.println(e.toString()); };
        System.out.println("");
        System.out.println(endTime - startTime);
        mySource.closeSource();
    }
}
