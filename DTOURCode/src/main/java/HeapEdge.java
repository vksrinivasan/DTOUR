public class HeapEdge {
    int source;
    int dest;
    double priority;

    HeapEdge(int src, int dst, double prity) {
        this.source = src;
        this.dest = dst;
        this.priority = prity;
    }

    HeapEdge(HeapEdge suggestion) {
        this.source = suggestion.source;
        this.dest = suggestion.dest;
        this.priority = suggestion.priority;
    }
}
