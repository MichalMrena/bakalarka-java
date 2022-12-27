package graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class GraphIterator<T extends Comparable<T>> implements Iterator<Vertex<T>> {

    private final ArrayList<Vertex<T>> vertices;
    private int pos;
    private boolean valid;
    
    public GraphIterator(ArrayList<Vertex<T>> vertices) {
        this.vertices = vertices;
        this.pos = -1;
        this.valid = false;
    }
    
    @Override
    public boolean hasNext() {
        if (this.valid) {
            return true;
        } else {
            return this.findNext();
        }
    }

    @Override
    public Vertex<T> next() {
        if (!this.valid) {
            if (!this.findNext()) {
                throw new NoSuchElementException();
            }
        }
        
        this.valid = false;
        return this.vertices.get(this.pos);
    }
    
    private boolean findNext() {
        int i = this.pos + 1;
        
        while (i < this.vertices.size()) {
            if (this.vertices.get(i) != null) {
                this.pos = i;
                this.valid = true;
                return true;
            }
            ++i;
        }
        
        this.pos = -1;
        this.valid = false;
        return false;
    }

}
