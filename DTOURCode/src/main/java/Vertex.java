
	
	public class Vertex {
	    final private PathGraph.HeapEdge id;
	    


	    public Vertex(PathGraph.HeapEdge id) {
	        this.id = id;
	       
	    }
	    public PathGraph.HeapEdge getId() {
	        return id;
	    }

	   

	  
	    
	    

	    @Override
	    public String toString() {
	        return ""+id;
	    }

	}


