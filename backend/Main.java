import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataSource mySource = new StaticData_Implement();
        Heuristic myHeuristic = new NullHeuristic_Implement();
        PathGraph myPathGraph = new PathGraph();
        AStar myAlgo = new AStar(0,3, mySource, myHeuristic, myPathGraph);

        // Currently the A* code stops running once it finds a path to the destination - I have this while loop here
        // to keep running it until it completes (finds shortest/most probabilistic path to destination), but we can
        // remove this loop and just run it once. Or we could try to implement the feedback scheduling from the paper.
        while(!myAlgo.searchComplete)
            myAlgo.runAlgo();
        List<Integer> order = myAlgo.reconstructPath();

        for(Integer e : order) { System.out.println(e.toString()); };
    }
}
