public class Edge  {
    
    private final HeapEdge source;
    private final HeapEdge destination;
    private final double weight;

    public Edge( HeapEdge source, HeapEdge destination, double weight) {
        
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

   
    public HeapEdge getDestination() {
        return destination;
    }

    public HeapEdge getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }


}