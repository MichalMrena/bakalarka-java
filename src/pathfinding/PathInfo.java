package pathfinding;

public class PathInfo {

    private final double lenght;
    private final long timeTaken;

    public PathInfo(double lenght, long timeTaken) {
        this.lenght = lenght;
        this.timeTaken = timeTaken;
    }

    public double getLenght() {
        return this.lenght;
    }

    public long getTimeTaken() {
        return this.timeTaken;
    }
    
}
