package datastructures.factory;

import datastructures.IPriorityQueue;

public interface IPrioQueueFactory {

    <E extends Comparable<E>> IPriorityQueue<E> makeQueue();
    
}
