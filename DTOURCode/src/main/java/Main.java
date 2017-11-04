import java.util.HashMap;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        DataSource mySource = new StaticData_Implement();
        //Heuristic myHeuristic = new NullHeuristic_Implement();
        double []NodeLongLat = mySource.getGeoList(); // We will keep longitudes/latitudes in memory (only ~1000 nodes)
        Heuristic myHeuristic = new DistanceHeuristic();
        PathGraph myPathGraph = new PathGraph();
       // AStar myAlgo = new AStar(0,3, mySource, myHeuristic, myPathGraph);
        AStar myAlgo = new AStar(0,3, mySource, myHeuristic, myPathGraph, NodeLongLat);

        // Currently the A* code stops running once it finds a path to the destination - I have this while loop here
        // to keep running it until it completes (finds shortest/most probabilistic path to destination), but we can
        // remove this loop and just run it once. Or we could try to implement the feedback scheduling from the paper.
        while(!myAlgo.searchComplete)
            myAlgo.runAlgo();
        List<Integer> order = myAlgo.reconstructPath();
        TreeHeap Ht=new TreeHeap();
        Graph g=new Graph();
        
        HashMap <Integer,MinHeap> HtCollection=Ht.createHtCollection(order, myPathGraph.Hin_Collection, g);
        
        //myPathGraph.treeHeapCollection = new HashMap<Integer,Heap_In>(myPathGraph.Hin_Collection);
       //myPathGraph.treeHeapCollection=new HashMap(myPathGraph.Hin_Collection);
        
        int dest=order.get(order.size()-1);
        System.out.println(HtCollection.get(dest));
        PathGraph.HeapEdge target=HtCollection.get(dest).heap[1];
        Vertex start=new Vertex(target);
        
        
        // run dijkstra on dest to find k paths
        
        DijkstraAlgorithm dij=new DijkstraAlgorithm(g);
        dij.execute(start);
        LinkedList <Vertex> path=dij.getPath(start);
        
        
        
        

        for(int i=1;i<path.size();i++) {
        	
        	//PathGraph.Heap_In s=myPathGraph.treeHeapCollection.get(order.get(i));
        	
        	//s.root=myPathGraph.treeHeapCollection.get(order.get(i-1)).root;
        	 
        	
        	//System.out.println(e.toString());
        	
        System.out.println(path.get(i).getId().source);
        
        
        };
        
        //myPathGraph.treeHeapCollection=myPathGraph.Hin_Collection;
        
        
        System.out.println(myPathGraph.Hin_Collection.get(1).root.dest);
    }
}










/*import java.util.List;

public class Main {
    public static void main(String[] args) {

        DataSource mySource = new MySqlDataSource();
        mySource.initSource();
        double []NodeLongLat = mySource.getGeoList(); // We will keep longitudes/latitudes in memory (only ~1000 nodes)
        Heuristic myHeuristic = new DistanceHeuristic();
        PathGraph myPathGraph = new PathGraph();
        AStar myAlgo = new AStar(1053,1020, mySource, myHeuristic, myPathGraph, NodeLongLat);

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

*/
