import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PathUtils {

    public static ArrayList<ArrayList<Integer>> pathExpansion(ArrayList<ArrayList<HeapEdge>> pg_paths, HashMap<Integer,
            Integer> astar_sp_tree, int start, int end) {
        ArrayList<ArrayList<Integer>> finalPaths = new ArrayList<ArrayList<Integer>>();

        for(int i = 0; i < pg_paths.size(); i++) {

            ArrayList<HeapEdge> currPathEdges = pg_paths.get(i);
            ArrayList<Integer> finalPathNodes = new ArrayList<Integer>();
            int currVertex = end;
            int targetVertex = -1;
            for(int j = currPathEdges.size()-1; j >= -1; j--) {

                // On the first iteration, backtrack from the target node to the destination of the last heap edge node
                if(j == currPathEdges.size()-1) {
                    HeapEdge eToInclude = currPathEdges.get(j);
                    targetVertex = eToInclude.dest;
                    finalPathNodes.addAll(backTrack(currVertex, targetVertex, astar_sp_tree));
                    currVertex = eToInclude.source;
                }

                // on the last iteration, backtrack from the first heap edge's source to the source of the whole thing
                else if(j < 0) {
                    targetVertex = start;
                    finalPathNodes.addAll(backTrack(currVertex, targetVertex, astar_sp_tree));
                }

                // if you are just between two heap edge nodes, then you need to backtrack from the source of the
                // later one to the destination of the previous one
                else {
                    HeapEdge ePrevious = currPathEdges.get(j);
                    targetVertex = ePrevious.dest;
                    finalPathNodes.addAll(backTrack(currVertex, targetVertex, astar_sp_tree));
                    currVertex = ePrevious.source;
                }
            }
            Collections.reverse(finalPathNodes);
            finalPaths.add(finalPathNodes);
        }

        return finalPaths;
    }

    public static ArrayList<Integer> backTrack(int currVertex, int targetVertex, HashMap<Integer, Integer> astar_sp_tree) {
        ArrayList<Integer> newList = new ArrayList<Integer>();
        if(currVertex == targetVertex) {
            newList.add(currVertex);
            return newList;
        } else {

            while(currVertex != targetVertex) {
                newList.add(currVertex);
                currVertex = astar_sp_tree.get(currVertex);
            }
            newList.add(targetVertex);
            return newList;
        }
    }
}
