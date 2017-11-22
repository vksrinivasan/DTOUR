import java.util.*;

import java.util.*;
public class Graph {
    public final HashSet<HeapEdge> vertexes;
    public final ArrayList<Edge> edges;
    private HeapEdge startVertex = null;
    public HashMap<HeapEdge, Integer> treeIDLookup;
    
    public Graph() {
    	this.vertexes=new HashSet<HeapEdge>();
    	this.edges=new ArrayList<Edge>();
    	treeIDLookup = new HashMap<HeapEdge, Integer>();
    	
    	
    }

    public void setStart(HeapEdge v) { startVertex = v; }

    public HeapEdge getStart() { return startVertex; }
    
    public void add(HeapEdge source, HeapEdge destination,double weight, int treeID_src, int treeID_dest) {

    	vertexes.add(source);
    	vertexes.add(destination);
    	
    	Edge e1=new Edge(source, destination, weight);
        edges.add(e1);

        if(!treeIDLookup.containsKey(source)) { treeIDLookup.put(source, treeID_src); }

        if (!treeIDLookup.containsKey(destination)) { treeIDLookup.put(destination, treeID_dest); }

    }

    public HashSet<HeapEdge> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }



}
