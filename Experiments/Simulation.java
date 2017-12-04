import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Simulation {

    public static void writePaths(List<Path> values, String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int counter = 0;
        for(Path thePath : values) {
            pw.print(Integer.toString(counter) + '\t');
            for(LongLat longlatpair : thePath.myPathCoordinates) {
                pw.print("(" + Double.toString(longlatpair.latitude) + "," + Double.toString(longlatpair.longitude) +')' + '\t');
            }
            pw.print('\n');
            counter += 1;
        }
        pw.close();
    }

    public static void writeData(List<Double> values, String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(Double value : values) {
            pw.println(value);
        }
        pw.close();
    }

    public static double ourProbScore(Path ourPath, DataSource mySource) {
        double retVal = 0.0;

        for(int i = 0; i < ourPath.myPathOrder.size()-1;i++) {
            List<EdgeData> outEdges = mySource.getNeighbors(ourPath.myPathOrder.get(i));
            for(EdgeData outEdge : outEdges) {
                if(outEdge.dest == ourPath.myPathOrder.get(i+1)) {
                    retVal += outEdge.weight;
                    break;
                }
            }
        }

        return retVal;
    }

    public static double ourDistance(Path ourPath) {
        double retVal = 0.0;
        for(int i = 0; i < ourPath.myPathCoordinates.size()-1; i++) {
            LongLat curr = ourPath.myPathCoordinates.get(i);
            LongLat next = ourPath.myPathCoordinates.get(i+1);

            double distance = DistanceHeuristic.computeDistance(curr.longitude, next.longitude, curr.latitude, next.latitude);
            retVal += distance;
        }

        return retVal;
    }

    public static double googleDistances(List<GraphDiscretizer.Node> googleNodeOrder) {
        double retVal = 0.0;

        for(int i = 0; i < googleNodeOrder.size() - 1; i++) {
            double curr_lat = googleNodeOrder.get(i).latitude;
            double curr_long = googleNodeOrder.get(i).longitude;

            double next_lat = googleNodeOrder.get(i+1).latitude;
            double next_long = googleNodeOrder.get(i+1).longitude;

            double distance = DistanceHeuristic.computeDistance(curr_long, next_long, curr_lat, next_lat);
            retVal += distance;
        }

        return retVal;
    }

    public static double googleProbScore(List<GraphDiscretizer.Node> googleNodeOrder, DataSource mySource) {
        double retVal = 0.0;

        int index = 0;
        while(index < googleNodeOrder.size()) {
            GraphDiscretizer.Node currNode = googleNodeOrder.get(index);

            // Get the highest probability edge from this node to any other node in the remaining path
            List<EdgeData> outEdges = mySource.getNeighbors(currNode.id);
            HashMap<Integer, EdgeData> edgeLookup = new HashMap<Integer, EdgeData>();
            for(EdgeData outEdge : outEdges) {
                edgeLookup.put(outEdge.dest, outEdge);
            }

            double maxProb = Double.MAX_VALUE;
            int maxProbIndex = -1;

            for(int j = index + 1; j < googleNodeOrder.size(); j++) {
                if(edgeLookup.containsKey(googleNodeOrder.get(j).id)) {
                    EdgeData tempEdgeData = edgeLookup.get(googleNodeOrder.get(j).id);
                    if(tempEdgeData.weight < maxProb) {
                        maxProb = tempEdgeData.weight;
                        maxProbIndex = j;
                    }
                }
            }

            if(maxProbIndex > -1) {
                index = maxProbIndex;
                retVal += (1.0/Math.exp(maxProb));
            } else {
                index += 1;
                retVal += 0.0001;
            }
        }

        return retVal/(double)googleNodeOrder.size();
    }

    public static void main(String[] args) {
        String filename = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\googleMapsPathsExpanded_500.txt";
        String ourFilename = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\pathOut.txt";
        String probOutOurs = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\probOut_Final_Ours.txt";
        String probOutGoogle = "C:\\Users\\Vyas\\OneDrive\\CSE 6242\\DTOUR\\Experiments\\Data\\probOut_Final_Google.txt";


        GraphDiscretizer discretizer = new GraphDiscretizer();

        List<Double> distributionProbabilities = new ArrayList<Double>();
        List<Double> distributionProbabilitiesOurs = new ArrayList<Double>();

        DataSource mySource = new MySqlDataSource();
        mySource.initSource();

        //DataSource mySource = new MySqlDataSource();
        //mySource.initSource();
        //double[] NodeLongLat = mySource.getGeoList();
        int counter = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            try (BufferedReader ourbr = new BufferedReader(new FileReader(ourFilename))) {
                String lineGM;
                String lineOurs;

                while(((lineGM = br.readLine()) != null) && ((lineOurs = ourbr.readLine()) != null)) {
                    System.out.println(counter);
                    counter += 1;

                    // Get google maps stuff into an arraylist
                    String[] coordStrings  = lineGM.split("\t");
                    List<double[]> googleMapsList = new ArrayList<double[]>();
                    for(int i = 0; i < coordStrings.length; i++) {
                        double[] temp = new double[2];
                        String[] tempCoords = coordStrings[i].split(",");
                        temp[0] = Double.parseDouble(tempCoords[0]);
                        temp[1] = Double.parseDouble(tempCoords[1]);
                        googleMapsList.add(temp);
                    }

                    List<GraphDiscretizer.Node> googleNodeOrder;
                    try {
                        googleNodeOrder = discretizer.pathToNodes(googleMapsList);
                    }
                    catch (RuntimeException ex) {
                        continue;
                    }

                    // Then get our data into an arraylist
                    String[] ourCoordStrings = lineOurs.split("\t");
                    List<double[]> ourCoordList = new ArrayList<double[]>();
                    for(int i = 1; i < ourCoordStrings.length; i++) {
                        double[] temp = new double[2];
                        String[] tempCoordsOurs = ourCoordStrings[i].replace("(","").replace(")","").split(",");
                        temp[0] = Double.parseDouble(tempCoordsOurs[0]);
                        temp[1] = Double.parseDouble(tempCoordsOurs[1]);
                        ourCoordList.add(temp);
                    }

                    List<GraphDiscretizer.Node> ourNodeOrder = new ArrayList<GraphDiscretizer.Node>();
                    for(int i = 0; i < ourCoordList.size(); i++) {
                        List<GraphDiscretizer.Node> tempNodeValue = discretizer.queryKDTree(ourCoordList.get(i)[0], ourCoordList.get(i)[1], 1);
                        ourNodeOrder.add(tempNodeValue.get(0));
                    }

                    assert(ourCoordList.get(0)[0] == googleMapsList.get(0)[0] && ourCoordList.get(0)[1] == googleMapsList.get(0)[1]);

                    double google_probabilityScore = googleProbScore(googleNodeOrder, mySource);
                    double our_probabilityScore = googleProbScore(ourNodeOrder, mySource);

                    distributionProbabilities.add(google_probabilityScore);
                    distributionProbabilitiesOurs.add(our_probabilityScore);

                }
            }
            catch (FileNotFoundException ex){
                System.out.println(ex);
            }
            catch (IOException ex){
                System.out.println(ex);
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        catch (IOException ex) {
            System.out.println(ex);
        }

        writeData(distributionProbabilitiesOurs, probOutOurs);
        writeData(distributionProbabilities, probOutGoogle);
    }
}
