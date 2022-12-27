package datastructures;

public class FibonacciHeap<E extends Comparable<E>> implements IPriorityQueue<E> {

    private int size;
    private FibHeapNode maxPrioItem;

    public FibonacciHeap() {
        this.size = 0;
        this.maxPrioItem = null;
    }
    
    @Override
    public QueueEntry<E> insert(E data) {
        FibHeapNode newItem = new FibHeapNode(data);
        
        this.addOneItem(newItem);
        this.size++;
        
        return newItem;
    }

    @Override
    public E deleteMin() {
        if (this.size == 0) {
            return null;
        }
        
        E retData = (E)this.maxPrioItem.getData();
        this.size--;
                
        this.removeMinItem();
        
        if (this.size != 0) {
            this.consolidateRoots();
        }
                 
        return retData;
    }

    @Override
    public E findMin() {
        if (this.size == 0) {
            return null;
        }
        
        return (E)this.maxPrioItem.getData();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void decreaseKey(QueueEntry<E> heapEntry) {
        FibHeapNode item = (FibHeapNode)heapEntry;
        
        if (item.isViolating()) {
            this.cutChild(item);
        } else if (item.compareTo(this.maxPrioItem) < 0) {
            this.maxPrioItem = item;
        }
    }
    
    @Override
    public IPriorityQueue<E> meld(IPriorityQueue<E> other) {
        if (this.isEmpty()) {
            return other;
        }
        
        FibonacciHeap<E> otherFib = (FibonacciHeap<E>)other;
        
        this.addMoreItems(otherFib.maxPrioItem);
        otherFib.size = 0;
        otherFib.maxPrioItem = null;
        
        return this;
    }
    
    @Override
    public void clear() {
        this.maxPrioItem = null;
        this.size = 0;
    }

    private void addOneItem(FibHeapNode newItem) {
        if (this.maxPrioItem == null) {
            this.maxPrioItem = newItem;
            newItem.prev = newItem;
            newItem.next = newItem;
        } else {
            newItem.prev = this.maxPrioItem;
            newItem.next = this.maxPrioItem.next;
            this.maxPrioItem.next.prev = newItem;
            this.maxPrioItem.next = newItem;
        }
        
        if (newItem.compareTo(this.maxPrioItem) < 0) {
            this.maxPrioItem = newItem;
        }
    }
    
    private void addMoreItems(FibHeapNode item) {
        FibHeapNode leftLeft = this.maxPrioItem;
        FibHeapNode rightRight = this.maxPrioItem.next;
        
        FibHeapNode leftRight = item;
        FibHeapNode rightLeft = item.prev;
        
        leftLeft.next = leftRight;
        leftRight.prev = leftLeft;

        rightLeft.next = rightRight;
        rightRight.prev = rightLeft;
    }

    private void removeMinItem() {
        FibHeapNode children = this.maxPrioItem.disconectChildren();
        
        if (this.maxPrioItem == this.maxPrioItem.next) {
            this.maxPrioItem = children;
        } else {
            this.maxPrioItem.prev.next = this.maxPrioItem.next;
            this.maxPrioItem.next.prev = this.maxPrioItem.prev;
            this.maxPrioItem = this.maxPrioItem.next;               
            
            if (children != null) {
                this.addMoreItems(children);
            }
        }
    }
    
    private void consolidateRoots() {
        int bitsNeeded = (int)Math.ceil(Math.log10(this.size + 1) / Math.log10(1.61)) + 1; 
        FibHeapNode[] auxItems = new FibHeapNode[bitsNeeded];                              

        FibHeapNode item = this.maxPrioItem;
        FibHeapNode last = this.maxPrioItem.prev;
        boolean endIt = false;
        
        for (;;) {
            FibHeapNode nextIt = item.next;
            
            if (item == last) {
                endIt = true;
            }
            
            for (;;) {
                if (auxItems[item.rank] == null) {
                    auxItems[item.rank] = item;
                    break;                                           
                } else {                                            
                    item = auxItems[item.rank].merge(item);       
                    auxItems[item.rank - 1] = null;
                }
            }
            
            if (endIt) {
                break;
            }
            
            item = nextIt;
        }
        
        this.maxPrioItem = null;
        for (FibHeapNode auxitem : auxItems) {
            if (auxitem == null) {
                continue;
            }
            
            if (this.maxPrioItem == null) {
                this.maxPrioItem = auxitem;
            } else if (auxitem.compareTo(this.maxPrioItem) < 0) {
                this.maxPrioItem = auxitem;
            }
        }
    }
    
    private void cutChild(FibHeapNode chld) {
        FibHeapNode parent = chld.parent;
        boolean goUp = parent.marked;
        
        parent.removeChild(chld);
        chld.marked = false;
        this.addOneItem(chld);
        
        if (goUp) {
            this.cutChild(parent);
        }
    }
    
    private class FibHeapNode<E extends Comparable<E>> extends QueueEntry<E> {

        private int rank;
        private boolean marked;
        private FibHeapNode next;
        private FibHeapNode prev;
        private FibHeapNode parent;
        private FibHeapNode child;
        
        public FibHeapNode(E data) {
            super(data);
            this.rank = 0;
            this.marked = false;
            this.next = null;
            this.prev = null;
            this.parent = null;
            this.child = null;
        }
        
        public FibHeapNode disconectChildren() {
            if (this.child != null) {
                FibHeapNode endIt = this.child;
                FibHeapNode iterated = this.child;
                do {
                    iterated.parent = null;
                    iterated.marked = false;
                    iterated = iterated.next;
                } while (iterated != endIt);
            }
            
            return this.child;
        }
        
        public FibHeapNode merge(FibHeapNode other) {
            if (this.compareTo(other) < 0) {
                other.removeFromList();
                this.addChild(other);
                return this;
            } else {
                this.removeFromList();
                other.addChild(this);
                return other;
            }
        }
        
        public boolean isRoot() {
            return this.parent == null;
        }
        
        public boolean isViolating() {
            return !this.isRoot() && this.compareTo(this.parent) < 0;
        }
        
        public void removeFromList() {
            this.prev.next = this.next;
            this.next.prev = this.prev;
        }
        
        public void removeChild(FibHeapNode chld) {
            if (chld == chld.next) {
                this.child = null;
            } else {
                if (chld == this.child) {
                    this.child = this.child.next;
                }
                
                chld.prev.next = chld.next;
                chld.next.prev = chld.prev;
            }
            
            chld.parent = null;
            this.rank--;
            
            if (!this.isRoot()) {
                this.marked = true;
            }
        }
        
        private void addChild(FibHeapNode newChild) {
            if (this.child == null) {
                this.child = newChild;
                newChild.next = newChild;
                newChild.prev = newChild;
            } else {
                newChild.next = this.child.next;
                newChild.prev = this.child;
                this.child.next.prev = newChild;
                this.child.next = newChild;
            }
            
            newChild.parent = this;
            this.rank++;
        }
        
    }
    
}

