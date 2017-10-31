import java.util.*;

/* Used this tutorial (https://www.redblobgames.com/pathfinding/a-star/implementation.html#python-astar) as a starting
 * point for coding up the algorithm
 */

public class AStar {
    int                          startNode; // starting node
    int                          endNode; // destination node
    DataSource                   mySource; // where do I get information about my graph/neighbors from
    Heuristic                    myHeur; // what heuristic do I use for A*
    PathGraph                    myPathGraph; // where do I store information about side track edges
    PriorityQueue<VertexNode>    unExplored; // enforce k* heuristic for next node selection
    HashMap<Integer, Integer>    cameFrom; // helps trace back path when I reach destination
    HashMap<Integer, Double>     costSoFar; // cost so far up until node
    HashMap<Integer, VertexNode> VNReference; // Need this to update the prority of nodes
    HashSet<Integer>             seen; // tells me if I've already included node in my "seen" category

    class VertexNode {
        int id;
        double pVal;

        VertexNode(int src, double priority) {
            this.id = src;
            this.pVal = priority;
        }
    }

    AStar(int start, int end, DataSource myData, Heuristic myHeur, PathGraph myPathGraph) {
        // Set the values that we know
        this.startNode = start;
        this.endNode = end;
        this.mySource = myData;
        this.myHeur = myHeur;
        this.myPathGraph = myPathGraph;

        // initialize the data structures that we will need
        this.cameFrom = new HashMap<Integer, Integer>();
        this.costSoFar = new HashMap<Integer, Double>();
        this.VNReference = new HashMap<Integer, VertexNode>();
        this.seen = new HashSet<Integer>();

        this.unExplored = new PriorityQueue<VertexNode>(100, new Comparator<VertexNode>() {
            public int compare(VertexNode a, VertexNode b) {
                if(a.pVal < b.pVal) return -1;
                if(a.pVal == b.pVal) return 0;
                if(a.pVal > b.pVal) return 1;
                return 0;
            }
        });

        // seed structures with the start point
        VertexNode sourceVN = new VertexNode(this.startNode, 0);
        this.unExplored.add(sourceVN);
        this.cameFrom.put(this.startNode, startNode);
        this.costSoFar.put(this.startNode, 0.0);
        this.VNReference.put(this.startNode, sourceVN);
    }

    public void runAlgo() {
        while(!this.unExplored.isEmpty()) {
            VertexNode curr = this.unExplored.poll();
            this.seen.add(curr.id);
            if(curr.id == this.endNode) break;

            List<EdgeData> outEdges = this.mySource.getNeighbors(curr.id);
            for(EdgeData tempEdge : outEdges) {

                // If the destination has already been seen/processed, then make a note of the sidetrack edge and move
                // on
                if(this.seen.contains(tempEdge.dest)) {
                    double delta = this.costSoFar.get(tempEdge.source) + tempEdge.weight - this.costSoFar.get(tempEdge.dest);
                    myPathGraph.addHinEdge(tempEdge.source, tempEdge.dest, delta);
                    continue;
                }

                // Calculate the new cost for this node
                double newCostVal = this.costSoFar.get(curr.id) + tempEdge.weight;

                // If the costs are more favorable than what we have on file, or if we have nothing on file,
                // then let's add this node to the priority queue
                if(this.costSoFar.get(tempEdge.dest) == null) {
                    this.costSoFar.put(tempEdge.dest, newCostVal);
                    double priority = newCostVal + this.myHeur.computeHeuristic(tempEdge.dest, this.endNode);
                    VertexNode neighborToAdd = new VertexNode(tempEdge.dest, priority);
                    this.VNReference.put(tempEdge.dest, neighborToAdd);
                    this.unExplored.add(neighborToAdd);
                    this.cameFrom.put(tempEdge.dest, tempEdge.source);
                }
                // Otherwise, update the priority - Note, the old edge can be premptively added to an Hin heap. If
                // by the time we reach the target the target node has been "seen", this is great. Otherwise, it is
                // fine too since we don't consider Hins that were not on the "shortest tree path" from source to
                // dest
                else if(newCostVal < this.costSoFar.get(tempEdge.dest)) {
                    // Need to write some code here to keep track of potential side-track edges that get found before the
                    // node is actually 'seen'

                    // Then deal with updating to the new shortest known path
                    this.costSoFar.put(tempEdge.dest, newCostVal);
                    double priority = newCostVal + this.myHeur.computeHeuristic(tempEdge.dest, this.endNode);
                    VertexNode oldNeighborNode = this.VNReference.get(tempEdge.dest);
                    this.unExplored.remove(oldNeighborNode);
                    oldNeighborNode.pVal = priority;
                    this.unExplored.add(oldNeighborNode);
                    this.cameFrom.put(tempEdge.dest, tempEdge.source);
                }
            }

        }
    }

    public List<Integer> reconstructPath() {
        List<Integer> thePath = new ArrayList<Integer>();
        int curr = this.endNode;
        while(curr!= this.startNode) {
            thePath.add(curr);
            curr = this.cameFrom.get(curr);
        }
        thePath.add(this.startNode);
        Collections.reverse(thePath);
        return thePath;
    }
}