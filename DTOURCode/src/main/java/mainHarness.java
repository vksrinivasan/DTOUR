import java.util.*;

public class mainHarness {
    public static void runCode(LongLat startLL, LongLat endLL) {

        // First translate the latitude/longtidues to node numbers using Shirata's GraphDiscretizer
        GraphDiscretizer discretizer = new GraphDiscretizer();
        List<GraphDiscretizer.Node> startNodeList = discretizer.queryKDTree(startLL.latitude, startLL.longitude, 1);
        List<GraphDiscretizer.Node> endNodeList = discretizer.queryKDTree(endLL.latitude, endLL.longitude, 1);
        int start_id = startNodeList.get(0).id;
        int end_id = endNodeList.get(0).id;

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

        HashMap<Integer,MinHeap> HtCollection=Ht.createHtCollection(order, myPathGraph.Hin_Collection, g);

        int dest=order.get(order.size()-1);
        System.out.println(HtCollection.get(dest));
        HeapEdge target=HtCollection.get(dest).heap[1];

        // run dijkstra on dest to find k paths
        ArrayList<ArrayList<Integer>> final_alt_paths = null;
        if(g.vertexes.size() > 0 && g.edges.size() > 0) {
            HeapEdge start = g.getStart();
            if (start != null) {
                DijkstraAlgorithm dij = new DijkstraAlgorithm(g);
                ArrayList<ArrayList<HeapEdge>> pg_paths = dij.execute(start);
                HashMap<Integer, Integer> astar_sp_tree = myAlgo.getCameFrom();
                final_alt_paths = PathUtils.pathExpansion(pg_paths, astar_sp_tree, start_id, end_id);
            }
        }
        final_alt_paths.add(0, new ArrayList<Integer>(order));

        /* Finally re-rank our calculated paths based on not just probability, but also any other measured
         * factor
         */
        ArrayList<Path> rankPaths = new ArrayList<Path>();
        ArrayList<Comparator<Path>> comparisonBasis = new ArrayList<Comparator<Path>>();
        comparisonBasis.add(new distanceComparator());
        double[] rankWeight = {.40, .60};
        int numRanks = 1; // Number of additional factors to rank on other than probability
        for(int i = 0; i < final_alt_paths.size(); i++) {
            Path tempPath = new Path(final_alt_paths.get(i), (double)i, numRanks, NodeLongLat);
            rankPaths.add(tempPath);
        }
        for(int i = 0; i < comparisonBasis.size(); i++) {
            Collections.sort(rankPaths, comparisonBasis.get(i));
            for(int j = 0; j < rankPaths.size(); j++) {
                rankPaths.get(j).setAdditionalRank((double)j);
            }
        }
        for(int i = 0; i < rankPaths.size(); i++) {
            rankPaths.get(i).computeFinalRank(rankWeight);
        }
        Collections.sort(rankPaths, new finalRankComparator());

        /* Return/Print at most 3 paths */
        System.out.println("Top Paths:");
        for(int i = 0; i < rankPaths.size() && i < 3; i++) {
            rankPaths.get(i).printPaths();
        }
    }
}
