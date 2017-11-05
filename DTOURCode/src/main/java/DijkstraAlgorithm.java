import java.util.*;
public class DijkstraAlgorithm {

    private final List<HeapEdge> nodes;
    private final List<Edge> edges;
    private Set<HeapEdge> settledNodes;
    private Set<HeapEdge> unSettledNodes;
    private Map<HeapEdge, ArrayList<HeapEdge>> predecessors;
    private Map<HeapEdge, Double> distance;

    public DijkstraAlgorithm(Graph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<HeapEdge>(graph.getVertexes());
        this.edges = new ArrayList<Edge>(graph.getEdges());
    }

    public void execute(HeapEdge source) {
        settledNodes = new HashSet<HeapEdge>();
        unSettledNodes = new HashSet<HeapEdge>();
        distance = new HashMap<HeapEdge, Double>();
        predecessors = new HashMap<HeapEdge, ArrayList<HeapEdge>>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            HeapEdge node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(HeapEdge node) {
        List<HeapEdge> adjacentNodes = getNeighbors(node);
        for (HeapEdge target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                if(!predecessors.containsKey(node)) {
                    ArrayList<HeapEdge> newList = new ArrayList<HeapEdge>();
                    newList.add(target);
                    predecessors.put(node, newList);
                } else {
                    ArrayList<HeapEdge> tempList = predecessors.get(node);
                    tempList.add(target);
                }
                unSettledNodes.add(target);
            }
        }

    }

    private double getDistance(HeapEdge node, HeapEdge target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<HeapEdge> getNeighbors(HeapEdge node) {
        List<HeapEdge> neighbors = new ArrayList<HeapEdge>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private HeapEdge getMinimum(Set<HeapEdge> vertexes) {
        HeapEdge minimum = null;
        for (HeapEdge vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(HeapEdge vertex) {
        return settledNodes.contains(vertex);
    }

    private double getShortestDistance(HeapEdge destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

}