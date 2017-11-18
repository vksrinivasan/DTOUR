interface Heuristic {

    /* Should just have some method of taking src and dest
     * and returning some heuristic for distance/probability
     */
    public double computeHeuristic(double src_long, double dest_long, double src_lat, double dest_lat);
}