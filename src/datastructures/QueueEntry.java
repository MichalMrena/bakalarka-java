package datastructures;

public class QueueEntry<E extends Comparable<E>> implements Comparable<QueueEntry<E>> {

    private final E data;

    public QueueEntry(E data) {
        this.data = data;
    }

    public E getData() {
        return this.data;
    }

    @Override
    public int compareTo(QueueEntry<E> o) {
        return this.data.compareTo(o.data);
    }
    
}
