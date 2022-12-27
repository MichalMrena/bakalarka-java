package datastructures.factory;

import datastructures.IPriorityQueue;
import datastructures.StrictFibonacciHeap;

public class StrictFibonacciHeapFactory implements IPrioQueueFactory {

    @Override
    public <E extends Comparable<E>> IPriorityQueue<E> makeQueue() {
        return new StrictFibonacciHeap<>();
    }

}
