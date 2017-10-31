import java.util.List;

interface DataSource {

    /* Initialize the DataSource to be ready for reading from */
    public void initSource();

    /* Get list of all edges/weights out from a given node */
    public List<EdgeData> getNeighbors(int source);

    /* Close DataSource link */
    public void closeSource();
}