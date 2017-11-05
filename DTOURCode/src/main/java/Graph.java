import java.util.*;

import java.util.*;
public class Graph {
    private final HashSet<HeapEdge> vertexes;
    private final ArrayList<Edge> edges;
    private HeapEdge startVertex = null;
    
    public Graph() {
    	this.vertexes=new HashSet<HeapEdge>();
    	this.edges=new ArrayList<Edge>();
    	
    	
    }

    public void setStart(HeapEdge v) { startVertex = v; }

    public HeapEdge getStart() { return startVertex; }
    
    public void add(HeapEdge source, HeapEdge destination,double weight) {

    	vertexes.add(source);
    	vertexes.add(destination);
    	
    	Edge e1=new Edge(source, destination, weight);
        edges.add(e1);
    }

    public HashSet<HeapEdge> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }



}
