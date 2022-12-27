package datastructures.factory;

import datastructures.BrodalQueue;
import datastructures.IPriorityQueue;

public class BrodalQueueFactory implements IPrioQueueFactory {

    @Override
    public <E extends Comparable<E>> IPriorityQueue<E> makeQueue() {
        return new BrodalQueue<>();
    }

}
