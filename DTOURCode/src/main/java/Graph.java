import java.util.*;

import java.util.*;
public class Graph {
    private final ArrayList<Vertex> vertexes;
    private final ArrayList<Edge> edges;
    
    public Graph() {
    	this.vertexes=new ArrayList<Vertex>();
    	this.edges=new ArrayList<Edge>();
    	
    	
    }

   
    
    public void add(PathGraph.HeapEdge source, PathGraph.HeapEdge destination,double weight) {
    	
    	Vertex v=new Vertex(source);
    	Vertex v2=new Vertex(destination);
    	
    	vertexes.add(v);
    	
    	vertexes.add(v2);
    	
    	Edge e1=new Edge(v, v2, weight);
    	
    	
    	
    	
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }



}
