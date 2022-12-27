package pathfinding;

import datastructures.IPriorityQueue;
import datastructures.QueueEntry;
import datastructures.factory.IPrioQueueFactory;
import graph.Edge;
import graph.Graph;
import graph.Vertex;
import utils.Stopwatch;

public class Dijkstra {

    private final Graph<DijkstraData> graph;

    public Dijkstra(Graph<DijkstraData> graph) {
        this.graph = graph;
    }
    
    public PathInfo findPath(int idSrc, IPrioQueueFactory factory) {
        Vertex<DijkstraData> src = this.graph.getVertex(idSrc);

        IPriorityQueue<Vertex<DijkstraData>> queue = factory.makeQueue();
        
        this.init();
        src.getData().setMarkT(this.graph.getZeroDist());
        src.getData().setEntry(queue.insert(src));
        
        Stopwatch watch = new Stopwatch();
        while (!queue.isEmpty()) {
            Vertex<DijkstraData> poped = queue.deleteMin();
            
            for (Edge<DijkstraData> edge : poped.getForwardStar()) {
                
                int newCost = edge.getCost() + poped.getData().getMarkT();
                Vertex<DijkstraData> target = edge.getTarget(poped);
                
                if (newCost < target.getData().getMarkT()) {
                    target.getData().setMarkT(newCost);
                    QueueEntry<Vertex<DijkstraData>> entry = target.getData().getEntry();
                    if (entry != null) {
                        queue.decreaseKey(entry);
                    } else {
                        target.getData().setEntry(queue.insert(target));
                    }
                }
            }
        }
        
        return new PathInfo(0, watch.getTime());
    }
    
    private void init() {
        for (Vertex<DijkstraData> vertex : this.graph) {
            vertex.getData().setEntry(null);
            vertex.getData().setMarkT(this.graph.getMaxDist());
        }
    }

}
