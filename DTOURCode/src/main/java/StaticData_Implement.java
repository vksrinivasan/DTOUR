import java.util.*;

public class StaticData_Implement implements DataSource {

    List<List<Integer>> neighborList;
    List<List<Double>> weightList;

    StaticData_Implement() {
        neighborList = new ArrayList<List<Integer>>();
        weightList = new ArrayList<List<Double>>();

        /* S0 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(1,5)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.90, .85)));

        /* S1 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(2,4)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.60, .50)));

        /* S2 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(4,3)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.45, .30)));

        /* S3 */
        neighborList.add(new ArrayList<Integer>());
        weightList.add(new ArrayList<Double>());

        /* S4 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(5,6,3)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.80,.65,.40)));

        /* S5 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(1)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.95)));

        /* S6 */
        neighborList.add(new ArrayList<Integer>(Arrays.asList(5,3)));
        weightList.add(new ArrayList<Double>(Arrays.asList(.99,.25)));
    }

    /* For the static data, there is no source, I'm just giving it for testing */
    public void initSource() {
        return;
    }

    /* just iterate through our lists... */
    public List<EdgeData> getNeighbors(int source) {
        List<Integer> nodeNeighbors = neighborList.get(source);
        List<Double> nodeNeighborWeight = weightList.get(source);

        Iterator<Integer> it1 = nodeNeighbors.iterator();
        Iterator<Double> it2 = nodeNeighborWeight.iterator();

        List<EdgeData> toReturn = new LinkedList<EdgeData>();

        while(it1.hasNext() && it2.hasNext()) {
            EdgeData newDatum = new EdgeData(source, it1.next(), inputAdjustment.probabilityAdjustment(it2.next()));
            toReturn.add(newDatum);
        }

        return toReturn;
    }

    public double[] getGeoList() {
        return new double[14];
    }

    /* Don't have to close anything, static data */
    public void closeSource() {
        return;
    }

//    public static void main(String[] args) {
//        StaticData_Implement mytest = new StaticData_Implement();
//        int data_size = mytest.neighborList.size();
//        for(int i = 0; i < data_size; i++) {
//            System.out.print(Integer.toString(i) + ": ");
//            for(int j = 0; j < mytest.neighborList.get(i).size(); j++) {
//                System.out.print("(" + mytest.neighborList.get(i).get(j) + ", " + mytest.weightList.get(i).get(j) +"), ");
//            }
//            System.out.println();
//        }
//    }
}