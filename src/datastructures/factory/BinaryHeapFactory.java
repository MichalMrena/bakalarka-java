package datastructures.factory;

import datastructures.BinaryHeap;
import datastructures.IPriorityQueue;

public class BinaryHeapFactory implements IPrioQueueFactory {

    @Override
    public <E extends Comparable<E>> IPriorityQueue<E> makeQueue() {
        return new BinaryHeap<>();
    }

}
