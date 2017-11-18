import java.util.HashMap;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        int start_id = 659;
        int end_id = 660;

        DataSource mySource = new MySqlDataSource();
        mySource.initSource();
        double []NodeLongLat = mySource.getGeoList(); // We will keep longitudes/latitudes in memory (only ~1000 nodes)
        Heuristic myHeuristic = new DistanceHeuristic();
        PathGraph myPathGraph = new PathGraph();
        AStar myAlgo = new AStar(start_id,end_id, mySource, myHeuristic, myPathGraph, NodeLongLat);

        // Currently the A* code stops running once it finds a path to the destination - I have this while loop here
        // to keep running it until it completes (finds shortest/most probabilistic path to destination), but we can
        // remove this loop and just run it once. Or we could try to implement the feedback scheduling from the paper.
        while(!myAlgo.searchComplete)
            myAlgo.runAlgo();
        List<Integer> order = myAlgo.reconstructPath();
        TreeHeap Ht=new TreeHeap();
        Graph g=new Graph();
        
        HashMap <Integer,MinHeap> HtCollection=Ht.createHtCollection(order, myPathGraph.Hin_Collection, g);
        
        int dest=order.get(order.size()-1);
        System.out.println(HtCollection.get(dest));
        HeapEdge target=HtCollection.get(dest).heap[1];

        // run dijkstra on dest to find k paths
        HeapEdge start = g.getStart();
        LinkedList<HeapEdge> path = null;
        ArrayList<ArrayList<Integer>> final_alt_paths = null;
        if(start != null) {
            DijkstraAlgorithm dij = new DijkstraAlgorithm(g);
            ArrayList<ArrayList<HeapEdge>> pg_paths = dij.execute(start);
            HashMap<Integer, Integer> astar_sp_tree = myAlgo.getCameFrom();
            final_alt_paths = PathUtils.pathExpansion(pg_paths, astar_sp_tree, start_id, end_id);
        }
        final_alt_paths.add(0, new ArrayList<Integer>(order));
        int a = 5;

    }
}