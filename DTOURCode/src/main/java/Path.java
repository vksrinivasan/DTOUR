import java.util.ArrayList;

public class Path {
    ArrayList<Integer> myPathOrder;
    ArrayList<LongLat> myPathCoordinates;
    double   pathDistance;
    double   probabilityRank;
    double[] additionalRanks;
    int      additionalRankIndex;
    double   overallRank;

    Path(ArrayList<Integer> thePath, double probRank, int numRanks, double[] geoList) {
        this.myPathOrder = thePath;
        this.probabilityRank = probRank;
        this.additionalRanks = new double[numRanks];
        this.additionalRankIndex = 0;
        this.overallRank = 0.0;

        // Then just set the path coordinates so we have them
        this.pathDistance = 0.0;
        this.myPathCoordinates = new ArrayList<LongLat>();
        for(int i = 0; i < this.myPathOrder.size(); i++) {
            int vertexNum = this.myPathOrder.get(i);
            double longitude = geoList[2*vertexNum + 0];
            double latitude = geoList[2*vertexNum + 1];
            LongLat tempLL = new LongLat(longitude, latitude);
            this.myPathCoordinates.add(tempLL);

            if(i > 0) {
                this.pathDistance += DistanceHeuristic.computeDistance(this.myPathCoordinates.get(i).longitude,
                                                                       this.myPathCoordinates.get(i-1).longitude,
                                                                       this.myPathCoordinates.get(i).latitude,
                                                                       this.myPathCoordinates.get(i-1).latitude);
            }
        }
    }

    /* Sets additional rank value for path object */
    void setAdditionalRank(double rank) {
        this.additionalRanks[this.additionalRankIndex] = rank;
        this.additionalRankIndex += 1;
    }

    /* Assumes first weight is for probability, and 2nd through nth weights
     * are in order of additional ranks
     */
    void computeFinalRank(double[] weights) {
        this.overallRank += this.probabilityRank * weights[0];
        for(int i = 1; i < weights.length; i++) {
            this.overallRank += this.additionalRanks[i-1]*weights[i];
        }
    }

    void printPaths() {
        for(int i = 0; i < myPathCoordinates.size(); i++) {
            System.out.print("(" + Double.toString(myPathCoordinates.get(i).latitude) + ", " + Double.toString(myPathCoordinates.get(i).longitude) + ")\t");
        }
        System.out.println();
    }
}
