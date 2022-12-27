package graph;

import java.util.ArrayList;
import java.util.Iterator;

public class Graph<T extends Comparable<T>> implements Iterable<Vertex<T>> {
    
    private final int zeroDist;
    private final int maxDist;
    private final ArrayList<Vertex<T>> vertices;
    private int vertexCount;

    public Graph(int zeroDist, int maxDist) {
        this.zeroDist = zeroDist;
        this.maxDist = maxDist;
        this.vertices = new ArrayList<>();
        this.vertexCount = 0;
    }

    public int getZeroDist() {
        return this.zeroDist;
    }

    public int getMaxDist() {
        return this.maxDist;
    }
    
    public Vertex<T> getVertex(int vid) {
        if (vid >= this.vertices.size() || this.vertices.get(vid) == null) {
            throw new NoSuchVertexException();
        }
        
        return this.vertices.get(vid);
    }
    
    public void addVertex(int vid, T data) {
        while (this.vertices.size() <= vid) {
            this.vertices.add(null);
        }
        
        this.vertices.set(vid, new Vertex<T>(vid, data));
        ++this.vertexCount;
    }
    
    public void addEdge(int vid1, int vid2, int cost) {
        Vertex<T> v1 = this.getVertex(vid1);
        Vertex<T> v2 = this.getVertex(vid2);
        Edge<T> edge = new Edge<>(cost, v1, v2);
        
        v1.addEgde(edge);
        v2.addEgde(edge);
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    @Override
    public Iterator<Vertex<T>> iterator() {
        return new GraphIterator<>(this.vertices);
    }
    
}
