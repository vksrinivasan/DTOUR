import java.util.List;

interface DataSource {

    /* Initialize the DataSource to be ready for reading from */
    public void initSource();

    /* Get list of all edges/weights out from a given node */
    public List<EdgeData> getNeighbors(int source);

    /* Get array of all longitudes/latitudes for every node. geoList[2*nodenumber+0] is long,
     * geolist[2*nodenumber+1] is lat
     */
    public double[] getGeoList();

    /* Close DataSource link */
    public void closeSource();
}