import java.util.*;
public class DijkstraAlgorithm {

    private final List<HeapEdge> nodes;
    private final List<Edge> edges;
    private Set<HeapEdge> settledNodes;
    private Set<HeapEdge> unSettledNodes;
    private Map<HeapEdge, HeapEdge> predecessors;
    private Map<HeapEdge, Double> distance;
    public ArrayList<HeapEdge> order;
    public HashMap<HeapEdge, Integer> treeIDLookup;

    public DijkstraAlgorithm(Graph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<HeapEdge>(graph.getVertexes());
        this.edges = new ArrayList<Edge>(graph.getEdges());
        this.order = new ArrayList<HeapEdge>();
        this.treeIDLookup = graph.treeIDLookup;
    }

    public ArrayList<ArrayList<HeapEdge>> execute(HeapEdge source) {
        settledNodes = new HashSet<HeapEdge>(); // Set of all nodes we've seen
        unSettledNodes = new HashSet<HeapEdge>(); // Queue of nodes
        distance = new HashMap<HeapEdge, Double>(); // Current set of distances
        predecessors = new HashMap<HeapEdge, HeapEdge>(); // Keeps track of predecessors
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        predecessors.put(source, null);
        while (unSettledNodes.size() > 0) {
            HeapEdge node = getMinimum(unSettledNodes); // Get closest node
            settledNodes.add(node); // Add this node to the "found queue"
            order.add(node); // Add this node to the order list - we need this to construct paths
            unSettledNodes.remove(node); // Remove node from unseen list
            findMinimalDistances(node);
        }

        // Now build paths from results
        ArrayList<ArrayList<HeapEdge>> pg_paths = computeSTSequence();
        return pg_paths;
    }

    private void findMinimalDistances(HeapEdge node) {
        List<HeapEdge> adjacentNodes = getNeighbors(node);
        for (HeapEdge target : adjacentNodes) {
            if (getShortestDistance(target) > (getShortestDistance(node) + getDistance(node, target))) {
                distance.put(target, getShortestDistance(node) + getDistance(node, target));
                predecessors.put(target, node); // Predecessor of the target is the node we got the target from
                unSettledNodes.add(target); // Add the new target node to the unsettled nodes list
            }
        }
    }

    private ArrayList<ArrayList<HeapEdge>> computeSTSequence() {
        ArrayList<ArrayList<HeapEdge>> finalPGList = new ArrayList<ArrayList<HeapEdge>>();

        // For each vertex in the shortest path tree, we have to construct a sequence
        for(int i = 0; i < order.size(); i++) {
            ArrayList<HeapEdge> tempSubList = new ArrayList<HeapEdge>();
            HeapEdge currNode = order.get(i);
            int currTree = 0;
            while(currNode != null) {
                tempSubList.add(currNode);
                currTree = treeIDLookup.get(currNode);

                HeapEdge predNode = predecessors.get(currNode); // our immediate predecessor
                if(predNode == null) {
                    currNode = predNode;
                    finalPGList.add(tempSubList);
                    continue;
                }
                int predTree = treeIDLookup.get(predNode); // Get the tree of your predecessor

                // If our predecessor tree is the same as the curr tree, then keep searching back until it's different
                while(predTree == currTree) {
                    predNode = predecessors.get(predNode);

                    if(predNode == null) break;

                    predTree = treeIDLookup.get(predNode);
                }

                // curr node is now this predecessor node
                currNode = predNode;

                // Add out list to the final list
                finalPGList.add(tempSubList);
            }
        }

        return finalPGList;
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