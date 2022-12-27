package pathfinding;

import datastructures.QueueEntry;
import graph.Vertex;

public class DijkstraData implements Comparable<DijkstraData> {

    private int markT;
    private QueueEntry<Vertex<DijkstraData>> entry;

    @Override
    public int compareTo(DijkstraData o) {
        return Integer.compare(this.markT, o.markT);
    }

    public int getMarkT() {
        return this.markT;
    }

    public QueueEntry<Vertex<DijkstraData>> getEntry() {
        return this.entry;
    }

    public void setMarkT(int markT) {
        this.markT = markT;
    }

    public void setEntry(QueueEntry<Vertex<DijkstraData>> entry) {
        this.entry = entry;
    }
    
}
