import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataSource mySource = new StaticData_Implement();
        Heuristic myHeuristic = new NullHeuristic_Implement();
        PathGraph myPathGraph = new PathGraph();
        AStar myAlgo = new AStar(0,3, mySource, myHeuristic, myPathGraph);
        myAlgo.runAlgo();
        List<Integer> order = myAlgo.reconstructPath();

        for(Integer e : order) { System.out.println(e.toString()); };
    }
}
