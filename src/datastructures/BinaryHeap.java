package datastructures;


public class BinaryHeap<E extends Comparable<E>> implements IPriorityQueue<E> {

    private int size;
    private BinaryHeapItem[] data;

    public BinaryHeap() {
        this(8);
    }
    
    public BinaryHeap(int initSize) {
        this.data = new BinaryHeapItem[initSize];
    }

    @Override
    public QueueEntry<E> insert(E data) {
        this.ensureCapacity();
        
        BinaryHeapItem<E> item = new BinaryHeapItem<E>(data, this.size);
        this.data[this.size] = item;
        this.bubleUp(this.size++);
        
        return item;
    }

    @Override
    public E deleteMin() {
        if (this.isEmpty()) {
            return null;
        }
        
        BinaryHeapItem poped = this.data[0];
        BinaryHeapItem last = this.data[this.size - 1];
        this.data[--this.size] = null;
        poped.index = -1;
        E popedData = (E)poped.getData();
        
        if (this.size != 0) {
            last.index = 0;
            this.data[0] = last;
            this.bubleDown(0);
        }
        
        return popedData;
    }

    @Override
    public E findMin() {
        if (this.isEmpty()) {
            return null;
        } else {
            return (E)this.data[0].getData();
        }
    }

    @Override
    public void decreaseKey(QueueEntry<E> entry) {
        this.bubleUp(((BinaryHeapItem<E>)entry).index);
    }
    
    @Override
    public IPriorityQueue<E> meld(IPriorityQueue<E> other) {
        BinaryHeap<E> o = (BinaryHeap<E>)other;
        for (BinaryHeapItem item : o.data) {
            this.insert((E)item.getData());
        }
        o.clear();
        return this;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        this.data = new BinaryHeapItem[this.size];
        this.size = 0;
    }
    
    private void bubleUp(int itemIndex) {
        BinaryHeapItem item = this.data[itemIndex];
        
        while (itemIndex > 0) {
            int parentIndex = (itemIndex - 1) >>> 1;
            
            if (this.data[parentIndex].compareTo(item) <= 0) break;
            
            this.data[itemIndex] = this.data[parentIndex];
            this.data[itemIndex].index = itemIndex;
            
            itemIndex = parentIndex;
        }
        
        this.data[itemIndex] = item;
        item.index = itemIndex;
    }
    
    private void bubleDown(int itemIndex) {
        BinaryHeapItem item = this.data[itemIndex];
        int leafBorder = this.size >>> 1;
        
        while (itemIndex < leafBorder) {
            int childIndexI = (itemIndex << 1) + 1;
            int rightChildI = childIndexI + 1;
            
            if (rightChildI < this.size && this.data[rightChildI].compareTo(this.data[childIndexI]) < 0) {
                childIndexI = rightChildI;
            }
            
            if (item.compareTo(this.data[childIndexI]) <= 0) break;
            
            this.data[itemIndex] = this.data[childIndexI];
            this.data[itemIndex].index = itemIndex;
            
            itemIndex = childIndexI;
        }
        
        this.data[itemIndex] = item;
        item.index = itemIndex;
    }

    private void ensureCapacity() {
        if (this.size == this.data.length) {
            int newLength = this.data.length << 1;
            BinaryHeapItem[] newData = new BinaryHeapItem[newLength];
            System.arraycopy(this.data, 0, newData, 0, this.data.length);
            this.data = newData;
        }
    }

    private class BinaryHeapItem<E extends Comparable<E>> extends QueueEntry<E> {

        private int index;
        
        public BinaryHeapItem(E data, int index) {
            super(data);
            this.index = index;
        }
        
    }
    
}
