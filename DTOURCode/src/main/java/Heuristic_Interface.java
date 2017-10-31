interface Heuristic {

    /* Should just have some method of taking src and dest
     * and returning some heuristic for distance/probability
     */
    public double computeHeuristic(int src, int destination);
}