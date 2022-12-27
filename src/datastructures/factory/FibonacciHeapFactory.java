package datastructures.factory;

import datastructures.FibonacciHeap;
import datastructures.IPriorityQueue;

public class FibonacciHeapFactory implements IPrioQueueFactory {

    @Override
    public <E extends Comparable<E>> IPriorityQueue<E> makeQueue() {
        return new FibonacciHeap<>();
    }

}
