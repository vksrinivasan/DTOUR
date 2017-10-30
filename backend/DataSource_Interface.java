interface DataSource {

    /* Initialize the DataSource to be ready for reading from */
    public void initSource();

    /* Get list of all edges/weights out from a given node */
    public EdgeData getNeighbors(Node source);

    /* Close DataSource link */
    public void closeSource();
}