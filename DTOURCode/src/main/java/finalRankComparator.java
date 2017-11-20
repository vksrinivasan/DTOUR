import java.util.Comparator;

public class finalRankComparator implements Comparator<Path> {

    @Override
    public int compare(Path p1, Path p2) {
        if(p1.overallRank > p2.overallRank) {
            return 1;
        }
        else if (p1.overallRank == p2.overallRank) {
            return 0;
        }
        else {
            return -1;
        }
    }

}
