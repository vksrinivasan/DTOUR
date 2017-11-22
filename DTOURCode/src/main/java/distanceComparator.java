import java.util.Comparator;

public class distanceComparator implements Comparator<Path> {

    @Override
    public int compare(Path p1, Path p2) {
        if(p1.pathDistance > p2.pathDistance) {
            return 1;
        }
        else if (p1.pathDistance == p2.pathDistance) {
            return 0;
        }
        else {
            return -1;
        }
    }

}
