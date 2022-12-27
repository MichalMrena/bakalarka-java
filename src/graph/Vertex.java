package graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex<T extends Comparable<T>> implements Comparable<Vertex<T>> {

    private final int id;
    private final T data;
    private final List<Edge<T>> forwardStar;

    public Vertex(int id, T data) {
        this.id = id;
        this.data = data;
        this.forwardStar = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public T getData() {
        return this.data;
    }

    public Iterable<Edge<T>> getForwardStar() {
        return this.forwardStar;
    }
    
    public void addEgde(Edge<T> e) {
        this.forwardStar.add(e);
    }

    @Override
    public int compareTo(Vertex<T> o) {
        return this.data.compareTo(o.data);
    }
    
    private T makeData(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("newInstance() magic failed.");
        }
    }
    
}
