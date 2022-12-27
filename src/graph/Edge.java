package graph;

public class Edge<T extends Comparable<T>> {

    private int cost;
    private final Vertex<T> v1;
    private final Vertex<T> v2;

    public Edge(int cost, Vertex<T> v1, Vertex<T> v2) {
        this.cost = cost;
        this.v1 = v1;
        this.v2 = v2;
    }

    public int getCost() {
        return this.cost;
    }
    
    public Vertex<T> getTarget(Vertex<T> src) {
        return src == this.v1 ? this.v2 : this.v1;
    }
    
}
