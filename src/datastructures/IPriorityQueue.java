package datastructures;

public interface IPriorityQueue<E extends Comparable<E>> {

    QueueEntry<E> insert(E data);

    IPriorityQueue<E> meld(IPriorityQueue<E> other);
    
    E deleteMin();

    E findMin();
    
    int size();
    
    void decreaseKey(QueueEntry<E> entry);

    void clear();
    
    default boolean isEmpty() {
        return this.size() == 0;
    }
    
}
