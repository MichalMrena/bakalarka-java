package test;

import datastructures.IPriorityQueue;
import datastructures.QueueEntry;
import datastructures.factory.IPrioQueueFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TesterPrioQueue {

    private final int testCaseCount;
    private final int seed;

    public TesterPrioQueue() {
        this(1_000_000, 144);
    }
    
    public TesterPrioQueue(int testCaseCount, int seed) {
        this.testCaseCount = testCaseCount;
        this.seed = seed;
    }
    
    public void test(IPrioQueueFactory factory) {
        
        Random rand = new Random(this.seed);
            
        IPriorityQueue<Dummy> queue = factory.makeQueue();
        
        // insert
        for (int i = 0; i < this.testCaseCount; i++) {
            Integer data = rand.nextInt(Integer.MAX_VALUE);
            queue.insert(new Dummy(data));
        }

        // delete min
        Integer prevPop = queue.deleteMin().data;
        for (int i = 1; i < this.testCaseCount; i++) {
            Integer poped = queue.deleteMin().data;
            
            if (poped < prevPop) {
                throw new TestFailedException("Nesprávne usporiadanie.");
            }
            
            prevPop = poped;
        }

        if (!queue.isEmpty()) {
            throw new TestFailedException("Niečo tam zostalo.");
        }

        // decrease insert
        List<QueueEntry<Dummy>> entries = new ArrayList<>(this.testCaseCount);
        for (int i = 0; i < this.testCaseCount; i++) {
            Integer prio = rand.nextInt(Integer.MAX_VALUE - 11000) + 10000;
            entries.add(queue.insert(new Dummy(prio)));
        }

        // decrease
        for (int i = 0; i < this.testCaseCount; i++) {
            QueueEntry<Dummy> entry = entries.get(i);
            Integer newPrio = rand.nextInt(entry.getData().data);
            entry.getData().data = newPrio;
            queue.decreaseKey(entry);
        }

        // decrease deleteMin
        prevPop = queue.deleteMin().data;
        for (int i = 1; i < this.testCaseCount; i++) {
            int poped = queue.deleteMin().data;
            
            if (poped < prevPop) {
                throw new TestFailedException("Nesprávne usporiadanie po decrease key.");
            }
            
            prevPop = poped;
        }

        if (!queue.isEmpty()) {
            throw new TestFailedException("Niečo tam zostalo po decrease key.");
        }
        
    }
    
    private class Dummy implements Comparable<Dummy> {
        
        private Integer data;

        public Dummy(Integer data) {
            this.data = data;
        }

        @Override
        public int compareTo(Dummy o) {
            return this.data.compareTo(o.data);
        }
        
    }
    
}
