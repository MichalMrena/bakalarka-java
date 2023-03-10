package datastructures;

import java.util.ArrayList;
import java.util.List;
import utils.ArrayUtils;

public class BrodalQueue<E extends Comparable<E>> implements IPriorityQueue<E> {

    private RootWrapT1 t1;
    private RootWrapT2 t2;

    private int size;

    public BrodalQueue() {
        this(0, null, null);
    }
    
    private BrodalQueue(int initSize, RootWrapT1 t1, RootWrapT2 t2) {
        this.size = initSize;
        this.t1 = t1;
        this.t2 = t2;
        this.setRootReferences();
    }
    
    @Override
    public QueueEntry<E> insert(E data) {
        BrodalNode newItem = new BrodalNode();
        BrodalEntry newEntry = new BrodalEntry(data );
        newItem.setEntry(newEntry);
        newEntry.setItem(newItem);
        
        this.size++;
        
        if (this.size == 1) {

            this.t1 = new RootWrapT1(newItem);

        } else {
            
            BrodalNode t1Root = this.t1.getRootNode();
            if (newItem.compareTo(t1Root) < 0) {
                t1Root.swapEntries(newItem);
            }            
            
            if (this.size == 2) {
                this.t2 = new RootWrapT2(newItem);
            } else if (this.size == 3) {
                BrodalNode oldt2 = this.t2.getRootNode();
                this.t2 = null;
                this.t1.setOtherWrap(null);
                this.t1.increaseRank(oldt2, newItem);
            } else {
                this.t1.addChild(newItem, false);
            }
            
        }
        
        return newEntry;
    }

    @Override
    public E deleteMin() {
        if (this.isEmpty()) {
            return null;
        } 
        
        E ret = this.findMin();
        this.size--;
        
        if (this.size == 0) {
            this.t1 = null;
        } else if (this.size == 1) {
            this.t1 = new RootWrapT1(this.t2.getRootNode());
            this.t2 = null;
        } else if (this.size == 2) {
            BrodalNode child1 = this.t1.getRootNode().getChild();
            BrodalNode child2 = child1.getNext();
            
            if (child1.compareTo(child2) < 0) {
                this.t1 = new RootWrapT1(child1);
                this.t2 = new RootWrapT2(child2);
            } else {
                this.t1 = new RootWrapT1(child2);
                this.t2 = new RootWrapT2(child1);
            }
            
            this.t1.setOtherWrap(this.t2);
            this.t2.setOtherWrap(this.t1);
        } else {
            this.moveT2UnderT1();
            
            BrodalNode newMin = this.findNewMin();        
            if (!newMin.isSonOfRoot()) {
                this.makeSonOfRoot(newMin); 
            }

            this.t1.removeChild(newMin);  
            BrodalNode newChildren = newMin.disconectChildren();
            this.t1.addLinkedItems(newChildren, false); 
            this.t1.mergeViolationSets(newMin);
            this.t1.totallyReduceWViolations();
            this.t1.getRootNode().swapEntries(newMin);
        }
        
        return ret;
    }

    @Override
    public E findMin() {
        this.increaseRankOfT1();
        
        if (this.isEmpty()) {
            return null;
        } else {
            return (E)this.t1.getRootNode().getEntry().getData();
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void decreaseKey(QueueEntry<E> heapEntry) {
        this.increaseRankOfT1();
        
        BrodalEntry entry = (BrodalEntry)heapEntry;
        BrodalNode item = entry.getItem();
        
        BrodalNode rootItem = this.t1.getRootNode();
        if (item.compareTo(rootItem) < 0) {
            rootItem.swapEntries(item);
        }
        
        if (item.isViolating()) {
            this.t1.addPossibleViolation(item, false);            
        } 
    }
    
    @Override
    public IPriorityQueue<E> meld(IPriorityQueue<E> otherQueue) {
        if (this.isEmpty() && otherQueue.isEmpty()) {
            return new BrodalQueue<>();
        }
        
        BrodalQueue<E> other = (BrodalQueue<E>)otherQueue;
        BrodalQueue<E> newQueue;
        int newSize = this.size + other.size;
        
        RootWrapT1<E> newT1 = this.findNewT1(other);
        RootWrap<E> maxRankRoot = this.findMaxRank(other);
        
        if (maxRankRoot.getRootNode().rank == 0) {
            newQueue = new BrodalQueue<>();
            newQueue.dumbMeld(this, other);
        } else {
            if (maxRankRoot.getRootNode().rank == newT1.getRootNode().rank) {
                maxRankRoot = newT1;
            }

            if (maxRankRoot == newT1) {
                newQueue = new BrodalQueue<>(newSize, newT1, null);
                newQueue.meldUnder(newQueue.t1, this, other);
            } else {
                RootWrapT2 newT2 = new RootWrapT2(maxRankRoot);
                newQueue = new BrodalQueue<>(newSize, newT1, newT2);
                newQueue.meldUnder(newQueue.t2, this, other);
            }
        }
        
        this.clear();
        other.clear();
        
        return newQueue;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.t1 = null;
        this.t2 = null;
    }
    
    // meldUtils
    
    private RootWrapT1<E> findNewT1(BrodalQueue<E> other) {
        if (this.t1 == null) {
            return other.t1;
        } else if (other.t1 == null) {
            return this.t1;
        } else {
            return this.t1.compareTo(other.t1) < 0 ? this.t1 : other.t1;
        }
    }
    
    private RootWrap<E> findMaxRank(BrodalQueue<E> other) {
        RootWrap<E> max1 = this.t2  == null ? this.t1  : this.t2;
        RootWrap<E> max2 = other.t2 == null ? other.t1 : other.t2;
        
        if (max1 == null) {
            return max2;
        } else if (max2 == null) {
            return max1;
        } else {
            return max1.getRootNode().rank > max2.getRootNode().rank ? max1 : max2;
        }
    }
    
    private void meldUnder(RootWrap<E> underRoot, BrodalQueue<E> q1, BrodalQueue<E> q2) {
        this.addUnderRoot(underRoot, q1.t1);
        this.addUnderRoot(underRoot, q1.t2);
        this.addUnderRoot(underRoot, q2.t1);
        this.addUnderRoot(underRoot, q2.t2);
    }
    
    private void addUnderRoot(RootWrap<E> root, RootWrap<E> toAdd) {
        if (toAdd == this.t1 || toAdd == this.t2 || toAdd == null || toAdd.getRootNode() == null) {
            return;
        }

        BrodalNode nodeToAdd = toAdd.getRootNode();
        
        if (root.getRootNode().rank > nodeToAdd.rank) {
            root.addChild(nodeToAdd, false);
        } else {
            BrodalNode largeChildren = nodeToAdd.removeLargestSons();
            root.addLinkedItems(largeChildren, false);            
            root.addChild(nodeToAdd, false);
        }
    }
    
    private void setRootReferences() {
        if (this.t1 != null) {
            this.t1.setOtherWrap(this.t2);
        }
        
        if (this.t2 != null) {
            this.t2.setOtherWrap(this.t1);
        }
    }
    
    private void dumbMeld(BrodalQueue<E> q1, BrodalQueue<E> q2) {
        while (!q1.isEmpty()) {
            this.insert(q1.deleteMin());
        }
        
        while (!q2.isEmpty()) {
            this.insert(q2.deleteMin());
        }
    }
    
    // deleteMinUtils
    
    public void moveT2UnderT1() {
        if (this.t2 == null) {
            return;
        }
        
        BrodalNode t2root = this.t2.getRootNode();
        BrodalNode t1root = this.t1.getRootNode();
        if (t2root.getRank() > 0) {
        
            BrodalNode zeroRank = this.t2.getFirstZeroRankChild();
            t2root.disconectChildren();
            BrodalNode item = zeroRank.getPrev();
            
            if (t1root.getRank() == 0) {
                BrodalNode newZeroRank = zeroRank.getNext().getNext();
                this.t1.increaseRank(zeroRank, zeroRank.getNext());
                zeroRank = newZeroRank;
            }

            while (zeroRank != null) {
                BrodalNode nextZeroRank = zeroRank.getNext();
                this.t1.addChild(zeroRank, false);
                zeroRank = nextZeroRank;
            }
            
            while (item != null) {
                if (item.getRank() == t1root.getRank()) {
                    BrodalNode newItem = item.getPrev().getPrev();
                    this.t1.increaseRank(item, item.getPrev());
                    item = newItem;
                }
                
                if (item == null) {
                    break;
                }
                
                if (item.getRank() == t1root.getRank()) {
                    continue;
                }
                
                BrodalNode nextItem = item.getPrev();
                this.t1.addChild(item, false);
                item = nextItem;
            }
            
        }
        
        this.t1.addChild(t2root, false);
        this.t1.setOtherWrap(null);
        this.t2 = null;
    }

    /**
     * N??jde nov?? kore?? t1 v potomkoch aktu??lneho t1 a vo violation setoch.
     * @return 
     */
    private BrodalNode findNewMin() {
        BrodalNode min = this.t1.getRootNode().getChild();
        
        BrodalNode item = min.getNext();
        while (item != null) {
            if (item.compareTo(min) < 0) {
                min = item;
            }
            item = item.getNext();
        }
        
        item = this.t1.getRootNode().getSetW();
        while (item != null) {
            if (item.compareTo(min) < 0) {
                min = item;
            }
            item = item.getNextInSet();
        }
        
        item = this.t1.getRootNode().getSetV();
        while (item != null) {
            if (item.compareTo(min) < 0) {
                min = item;
            }
            item = item.getNextInSet();
        }
        
        this.t1.removeViolation(min);
        
        return min;
    }

    private void makeSonOfRoot(BrodalNode newMin) {
        BrodalNode swaped = this.t1.getItemOfRank(newMin.getRank());
        
        swaped.swapWith(newMin);
        this.t1.removeViolation(newMin);
        this.t1.addPossibleViolation(swaped, false);
    }
    
    // utils
    
    private void increaseRankOfT1() {
        if (this.t2 == null || this.size < 4) {
            return;
        }
        
        int t1Rank = this.t1.getRootNode().getRank();
        int t2Rank = this.t2.getRootNode().getRank();
        
        if (t2Rank <= t1Rank + 2) {
            BrodalNode t2root = this.t2.getRootNode();
            this.t2 = null;
            this.t1.setOtherWrap(null);
            
            BrodalNode delinked1 = t2root.removeLargestSons();
            if (t2root.getRank() > t1Rank) {
                BrodalNode delinked2 = t2root.removeLargestSons();
                this.t1.incRankAndAddRest(delinked2);
            }
            this.t1.incRankAndAddRest(delinked1);
            this.t1.addChild(t2root, false);
            
        } else {
            BrodalNode item = this.t2.removeChild(t1Rank + 1, false);
            BrodalNode delinked = item.removeLargestSons();
            this.t1.incRankAndAddRest(delinked);
            this.t1.addChild(item, false);
        }
    }
       
    // test
    
    private void test() {
        int expSize = 0;

        if (this.t1 != null) {
            expSize += this.t1.getRootNode().subtreeSize();
        }

        if (this.t2 != null) {
            expSize += this.t2.getRootNode().subtreeSize();
        }
        
        if (expSize != this.size) {
            System.out.println("bug");
        }
    }
    
    // Nested Classes
    
    private class BrodalEntry<E extends Comparable<E>> extends QueueEntry<E> { 

        private BrodalNode node;

        public BrodalEntry(E data) {
            super(data);
        }

        BrodalNode getItem() {
            return this.node;
        }

        void setItem(BrodalNode item) {
            this.node = item;
        }

    }
    
    private class BrodalNode<E extends Comparable<E>> implements Comparable<BrodalNode> {
        
        private int rank;
        private BrodalEntry entry;

        private BrodalNode parent;
        private BrodalNode prev;
        private BrodalNode next;
        private BrodalNode child;

        private BrodalNode setV;
        private BrodalNode setW;
        private BrodalNode[] auxW;
        private boolean inVSet;

        private BrodalNode prevInSet;
        private BrodalNode nextInSet;
        private BrodalNode inThisOnesSet;

        public BrodalNode() {
            this.rank = 0;
            this.auxW = new BrodalNode[4];
            this.setV = null;
            this.setW = null;
            this.prevInSet = null;
            this.nextInSet = null;
            this.inVSet = false;
        }

        public int subtreeSize() {
            int size = 1;
            
            BrodalNode chld = this.child;
            while (chld != null) {
                size += chld.subtreeSize();
                chld = chld.getNext();
            }
            
            return size;
        }
        
        @Override
        public int compareTo(BrodalNode o) {
            return this.entry.compareTo(o.entry);
        }
        
        public void swapEntries(BrodalNode other) {
            BrodalEntry tmpEntry = other.entry;
            other.entry = this.entry;
            this.entry = tmpEntry;

            other.entry.setItem(other);
            this.entry.setItem(this);
        }

        public boolean isViolating() {
            return this.parent != null && this.compareTo(this.parent) < 0;
        }

        /**
         * Odstr??ni node z violation setu, v ktorom je. Korektne nastav??
 aj node, do ktor??ho violation setu patr??.
         */
        public void removeFromViolationSet() {
            this.removeFromViolationSet(this.rank);
        }

        /**
         * Mohlo sa sta?? ??e sa rank itemu zmen??il ale vo violation sete je
         * e??te evidovan?? pod t??m star??m.
         * @param trueRank -> rank itemu, s ktor??m je evidovan?? vo violation sete
         */
        public void removeFromViolationSet(int trueRank) {
            if (this.inThisOnesSet != null) {
                this.inThisOnesSet.removeFromSet(this, trueRank);
            }

            if (this.nextInSet != null) {
                this.nextInSet.prevInSet = this.prevInSet;
            } 

            if (this.prevInSet != null) {
                this.prevInSet.nextInSet = this.nextInSet;
            }

            this.prevInSet = null;
            this.nextInSet = null;
            this.inThisOnesSet = null;
            this.inVSet = false;
        }

        public void addToVSet(BrodalNode item) {
            if (this.setV == null) {
                this.setV = item;
            } else {
                this.setV.prevInSet = item;
                item.nextInSet = this.setV;
                this.setV = item;
            }

            item.inThisOnesSet = this;
            item.inVSet = true;
        }

        public void addToWSet(BrodalNode item) {
            if (this.auxW[item.rank] == null) {
                this.addToWSetInFront(item);
                this.auxW[item.rank] = item;
            } else {
                this.addToWSetAfter(item, this.auxW[item.rank]);
            }

            item.inThisOnesSet = this;
        }

        public int getRank() {
            return this.rank;
        }

        public BrodalNode getParent() {
            return this.parent;
        }

        public BrodalNode getPrev() {
            return this.prev;
        }

        public BrodalNode getNext() {
            return this.next;
        }

        public BrodalNode getChild() {
            return this.child;
        }

        public BrodalNode getPrevInSet() {
            return this.prevInSet;
        }

        public BrodalNode getNextInSet() {
            return this.nextInSet;
        }

        public BrodalNode getSetW() {
            return this.setW;
        }

        public BrodalNode getSetV() {
            return this.setV;
        }

        public BrodalNode getInThisOnesSet() {
            return this.inThisOnesSet;
        }

        public BrodalNode disconnectSetW() {
            BrodalNode ret = this.setW;
            this.setW = null;
            this.auxW = new BrodalNode[this.rank];
            return ret;
        }

        public BrodalNode disconnectSetV() {
            BrodalNode ret = this.setV;
            this.setV = null;
            return ret;
        }

        public BrodalNode getWViolation(int rank) {
            return this.auxW[rank];
        }

        public boolean isInVSet() {
            return this.inVSet;
        }

        public QueueEntry getEntry() {
            return this.entry;
        }

        public void setEntry(BrodalEntry entry) {
            this.entry = entry;
        }

        public boolean isSonOfRoot() {
            return this.parent != null && this.parent.parent == null;
        }

        public boolean isSibling(BrodalNode other) {
            return this.parent == other.parent;
        }

        public int sameRankSiblingCount() {
            int count = 0;
            BrodalNode itemPrev = this.prev;
            BrodalNode itemNext = this.next;

            while (itemPrev != null && itemPrev.rank == this.rank) {
                count++;
                itemPrev = itemPrev.prev;
            }

            while (itemNext != null && itemNext.rank == this.rank) {
                count++;
                itemNext = itemNext.next;
            }

            return count;
        }

        public void addChildAtFront(BrodalNode newChild) {
            newChild.parent = this;

            if (this.child == null) {
                this.child = newChild;
                newChild.prev = null;
                newChild.next = null;
                this.rank = 1;
            } else {
                newChild.prev = null;
                newChild.next = this.child;
                this.child.prev = newChild;
                this.child = newChild;
                this.checkRank();
            }
        }

        /**
         * Odstr??ni removed z potomkov this. Ak je treba nastav?? nov?? rank.
         * To ist?? ako removeNChildren(removed, 1).
         * @param removed 
         */
        public void removeChild(BrodalNode removed) {
            if (this.child == removed) {
                this.child = removed.next;
                this.rank = (this.child != null) ? this.child.rank + 1 : 0;
            }

            if (removed.prev != null) {
                removed.prev.next = removed.next;
            }

            if (removed.next != null) {
                removed.next.prev = removed.prev;
            }

            removed.prev = null;
            removed.next  = null;
            removed.parent = null;
        }

        /**
         * Odstr??ni n childov po????naj??c first a ak je treba nastav?? nov?? rank.
         * @param first
         * @param n 
         */
        public void removeNChildren(BrodalNode first, int n) {
            BrodalNode last = first;
            first.parent = null;
            for (int i = 1; i < n; i++) {
                last = last.next;
                last.parent = null;
            }

            if (this.child == first) {
                this.child = last.next;
                this.rank = (this.child != null) ? this.child.rank + 1 : 0;
            }

            if (first.prev != null) {
                first.prev.next = last.next;
            }

            if (last.next != null) {
                last.next.prev = first.prev;
            }

            first.prev = null;
            last.next  = null;
        }

        /**
         * Odstr??ni v??etk??ch potomkov ranku (k-1) ak rank this je k.
         * @return 
         */
        public BrodalNode removeLargestSons() {
            BrodalNode ret = this.child;
            this.removeNChildren(this.child, this.lasrgestChildrenCount());
            return ret;
        }

        public void addChildAfter(BrodalNode after, BrodalNode childToAdd) {
            childToAdd.prev = after;
            childToAdd.next = after.next;
            if (after.next != null) {
                after.next.prev = childToAdd;
            }
            after.next = childToAdd;
            childToAdd.parent = this;
        }

        /**
         * Odstr??ni 2 alebo 3 childov najvy????ieho ranku.
         * Ak je treba zn????i rank.
         * @return zre??azen?? odstr??nen?? childi
         */
        public BrodalNode remove2to3LargestChildren() {
            BrodalNode ret = this.child;

            int largeChildCount = this.lasrgestChildrenCount();
            if (largeChildCount == 2 || largeChildCount == 3) {
                this.removeNChildren(this.child, largeChildCount);
            } else {
                this.removeNChildren(this.child, 2);
            }

            return ret;
        }

        /**
         * Vzh??adom na invarianty aspo?? jedn??ho ur??ite m??.
         * @return 
         */
        public BrodalNode getSameRankSibling() {
            return (this.prev != null && this.prev.rank == this.rank) ? this.prev : this.next;
        }

        public void replaceWith(BrodalNode replacement) {
            replacement.prev = this.prev;
            replacement.next = this.next;
            replacement.parent = this.parent;

            if (this.prev != null) {
                this.prev.next = replacement;
            }

            if (this.next != null) {
                this.next.prev = replacement;
            }

            if (this.parent.child == this) {
                this.parent.child = replacement;
            }

            this.prev = null;
            this.next = null;
            this.parent = null;
        }

        public void swapWith(BrodalNode other) {
            BrodalNode oldThisPrev   = this.prev;
            BrodalNode oldThisNext   = this.next;
            BrodalNode oldThisParent = this.parent;

            if (other.prev != null) {
                other.prev.next = this;
            }
            if (other.next != null) {
                other.next.prev = this;
            }
            this.prev   = other.prev;
            this.next   = other.next;
            this.parent = other.parent;
            if (other.parent.child == other) {
                other.parent.child = this;
            }

            if (oldThisPrev != null) {
                oldThisPrev.next = other;
            }
            if (oldThisNext != null) {
                oldThisNext.prev = other;
            }
            other.prev   = oldThisPrev;
            other.next   = oldThisNext;
            other.parent = oldThisParent;
            if (oldThisParent.child == this) {
                oldThisParent.child = other;
            }
        }

        public BrodalNode disconectChildren() {
            BrodalNode ret = this.child;
            this.rank = 0;

            // TODO toto teoreticky nie je nutn?? a keby sa robili nejak?? optimaliz??cie
            // mohlo by to ??s?? pre?? s t??m ??e by bolo treba d??va?? na to pozor
    //        BrodalNode node = this.child;
    //        while (node != null) {
    //            node.parent = null;
    //            node = node.next;
    //        }

            this.child = null;

            return ret;
        }

        /**
         * Vr??ti po??et potomkov ranku (k-1) ak rank this je k.
         * @return 
         */
        private int lasrgestChildrenCount() {
            int count = 0;

            BrodalNode chld = this.child;
            while (chld != null && chld.getRank() == this.child.getRank()) {
                count++;
                chld = chld.getNext();
            }

            return count;
        }

        private void addToWSetInFront(BrodalNode item) {
            if (this.setW == null) {
                this.setW = item;
            } else {
                this.setW.prevInSet = item;
                item.nextInSet = this.setW;
                this.setW = item;
            }
        }

        private void addToWSetAfter(BrodalNode item, BrodalNode afterThis) {
            item.prevInSet = afterThis;
            item.nextInSet = afterThis.nextInSet;

            if (afterThis.nextInSet != null) {
                afterThis.nextInSet.prevInSet = item;
            }
            afterThis.nextInSet = item;
        }

        /**
         * Tu treba o??etri?? pr??pad ??e node je prv?? vo svojem sete teda treba
 aktualizova?? this.setV alebo this.setW.
         * Ak bol node v setW treba tie?? skontrolova?? auxW pole a aktualizova?? ho.
         * @param itemToRemove 
         */
        private void removeFromSet(BrodalNode itemToRemove, int trueRank) {
            if (this.setV == itemToRemove) {
                this.setV = itemToRemove.nextInSet;
            } else if (this.setW == itemToRemove) {
                this.setW = itemToRemove.nextInSet;
            }

            // ak je auxW mensie ako rank itemu tak tam urcite nie je takze 
            // netreba nic kontrolovat
            if (this.auxW.length <= trueRank) {
                return; 
            }

            if (this.auxW[trueRank] == itemToRemove) {
                if (itemToRemove.nextInSet != null && (trueRank == itemToRemove.nextInSet.rank)) {
                    this.auxW[trueRank] = itemToRemove.nextInSet;
                } else {
                    this.auxW[trueRank] = null;
                }
            }
        }

        /**
         * Skontroluje ??i sa nezv????il rank. Ak ??no e??te skontroluje ??i netreba
         * zv??????i?? pomocn?? pole pre W set.
         */
        private void checkRank() {
            this.rank = this.child.getRank() + 1;
            if (this.rank >= this.auxW.length) {
                this.auxW = (BrodalNode[])ArrayUtils.doubleRefArray(this.auxW, BrodalNode.class);
            }
        }

    }
    
    private abstract class RootWrap<E extends Comparable<E>> implements Comparable<RootWrap<E>> {
        
        private BrodalNode rootNode;
        private Guide guideUpper;
        private Guide guideLower;
        public int[] childrenCount;
        private BrodalNode[] children;

        public RootWrap(BrodalNode rootItem) {
            this.rootNode = rootItem;
            this.rootNode.parent = null;
            this.children = new BrodalNode[4];
            this.childrenCount  = new int[4];
            this.guideUpper = new Guide(new ReducerUpper(this));
            this.guideLower = new Guide(new ReducerLower(this));
        }

        public RootWrap(RootWrap other) {
            this.rootNode = other.rootNode;
            other.rootNode = null;
            
            this.childrenCount = other.childrenCount;
            other.childrenCount = null;
            
            this.children = other.children;
            other.children = null;
            
            this.guideUpper = other.guideUpper;
            other.guideUpper = null;
            
            this.guideLower = other.guideLower;
            other.guideLower = null;
            
            this.guideUpper.setReducer(new ReducerUpper(this));
            this.guideLower.setReducer(new ReducerLower(this));
        }

        @Override
        public int compareTo(RootWrap<E> o) {
            return this.rootNode.compareTo(o.rootNode);
        }
        
        /**
         * @param newChildren -> aspo?? 2 a najviac 7 itemov s rankom rovnak??m ako m?? root.
         */
        public void increaseRank(BrodalNode... newChildren) {
            for (BrodalNode item : newChildren) {
                this.rootNode.addChildAtFront(item);
            }

            int childCount = newChildren.length;
            BrodalNode firstChild = newChildren[childCount - 1];
            int childIndex = this.getRootNode().getRank() - 1;

            this.childrenCount[childIndex] = childCount;
            this.children[childIndex] = firstChild;
            this.increaseDomains();
        }

        /**
         * Prid?? child pod t1 a ak je v nejakom violation sete, korektne ho z neho odstr??ni.
         * @param child       -> child na pridanie
         * @param guideAdding -> TRUE ak child pridava guide v r??mci reduceUpperBound oper??ci inak FALSE
         */
        public void addChild(BrodalNode child, boolean guideAdding) {
            int childRank = child.getRank();    

            BrodalNode afterThis = this.children[childRank];
            this.rootNode.addChildAfter(afterThis, child);
            this.childrenCount[childRank]++;

            if (childRank < this.rootNode.getRank() - 2 && !guideAdding) {
                int guideValue = this.guideNumberUpper(this.childrenCount[childRank]);
                this.guideUpper.possiblyIncrease(childRank, guideValue);
            } else {
                this.checkLargeChildrenUpper(childRank);                                     
            }
        }

        /**
         * Odstr??ni 1 child dan??ho ranku z childov rootu.
         * @param rank
         * @param guideRemoving
         * @return 
         */
        public BrodalNode removeChild(int rank, boolean guideRemoving) {
            BrodalNode ret = this.removeChildUnchecked(rank);

            if (rank < this.rootNode.getRank() - 2 && !guideRemoving) {
                int guideValue = this.guideNumberLower(this.childrenCount[rank]);
                this.guideLower.possiblyIncrease(rank, guideValue);
            } else {
                this.checkLargeChildrenLower(rank);
            }

            return ret;
        }

        /**
         * Odstr??ni konkr??tny child z childov rootu.
         * @param child 
         */
        public void removeChild(BrodalNode child) {
            this.removeChild(child, child.getRank());
        }

        /**
         * Spoj?? 3 potomkov s dan??m rankom do jedn??ho potomka s rankom (rank+1)
         * a prid?? ho medzi potomkov.
         * Pou????va sa na udr??ovanie hornej hranice potomkov.
         * @param rank
         */
        public void reduceUpperBound(int rank) {
            BrodalNode newChild = this.linkChildren(rank);
            this.addChild(newChild, true);
        }

        /**
         * Rozoberie 1 potomka ranku (k+1) na 2 - 3 potomkov ranku k
         * a jedn??ho potomka ranku najviac (k+1). Novo vzniknut?? itemy 
         * prid?? nasp???? medzi potomkov.
         * @param rank 
         */
        public void reduceLowerBound(int rank) {
            // ??peci??lny pr??pad kedy sa rob?? reduce na hranici guidu
            // childi rankov (k-1) a (k-2) sa rie??ia a?? nakoniec ke?? s?? 
            // guidy hotov??
            // ??asom by sa to mo??no dalo porie??i?? kraj??ie zaia?? takto
            boolean removingAtEdge = rank == this.rootNode.getRank() - 3;

            DelinkingResult result = this.delinkChild(rank + 1);
            BrodalNode items = result.getDelinkedItems();
            this.addLinkedItems(items, true);

            this.addExtraItem(result.getExtraItem());

            if (removingAtEdge) {
                this.checkLargeChildrenLower(rank + 1);
            }
        }

        public int getValUpperBound(int index) {
            return this.guideNumberUpper(this.childrenCount[index]);
        }

        public int getValLowerBound(int index) {
            return this.guideNumberLower(this.childrenCount[index]);
        }

        public void addLinkedItems(BrodalNode items, boolean guideAdding) {
            while (items != null) {
                BrodalNode newNext = items.getNext();
                this.addChild(items, guideAdding);
                items = newNext;
            }
        }

        public BrodalNode getFirstZeroRankChild() {
            return this.children[0];
        }

        public BrodalNode getItemOfRank(int rank) {
            // this.child ukazuje na prv?? item dan??ho ranku
            // je teda lep??ie vr??ti?? next, lebo vieme ??e m?? rovnak?? rank
            // a keby sa napr. swapoval netreba aktualizova?? tento zoznam
            return this.children[rank].getNext();
        }

        public abstract void removeViolation(BrodalNode item);

        public abstract void addExtraItem(BrodalNode item);

        protected BrodalNode getRootNode() {
            return this.rootNode;
        }

        protected void increaseDomains() {
            if (this.rootNode.getRank() == this.children.length) {
                this.children = (BrodalNode[])ArrayUtils.doubleRefArray(this.children, this.children[0].getClass());
                this.childrenCount = ArrayUtils.doubleIntArray(this.childrenCount);
            }

            if (this.rootNode.getRank() > 2) {
                this.guideUpper.increaseDomain();
                this.guideLower.increaseDomain();

                int newIndexToGuide = this.rootNode.getRank() - 3;
                this.guideUpper.possiblyIncrease(newIndexToGuide, this.guideNumberUpper(this.childrenCount[newIndexToGuide]));
                this.guideLower.possiblyIncrease(newIndexToGuide, this.guideNumberLower(this.childrenCount[newIndexToGuide]));
            }
        }

        protected void decreaseDomains() {
            this.guideUpper.decreaseDomain();
            this.guideLower.decreaseDomain();
        }

        /**
         * Niekedy sa stane ??e sa odstra??uje child, ktor??ho rank sa zmenil
         * ale tu je e??te v r??mci po??tu evidovan?? pod p??vodn??m rankom.
         * @param child
         * @param trueRank 
         */
        protected void removeChild(BrodalNode child, int trueRank) {
            this.childrenCount[trueRank]--;

            if (this.children[trueRank] == child) {
                this.children[trueRank] = child.getNext();
            } 

            this.rootNode.removeChild(child);

            this.checkChildrenLower(trueRank);
        }

        /**
         * Odstr??ni 1 child dan??ho ranku z potomkov rootu.
         * Nerob?? v??ak ??iadne kontroly pou????va?? s rozvahou !!!
         * @param rank
         * @return 
         */
        protected BrodalNode removeChildUnchecked(int rank) {
            this.childrenCount[rank]--;
            BrodalNode ret = this.children[rank];
            this.children[rank] = ret.getNext();
            this.rootNode.removeChild(ret);

            return ret;
        }

        protected void checkChildrenLower(int rank) {
            if (rank < this.rootNode.getRank() - 2) {
                int guideValue = this.getValLowerBound(rank);
                this.guideLower.possiblyIncrease(rank, guideValue);
            } else {
                this.checkLargeChildrenLower(rank);
            }
        }

        /**
         * Nech k je rank kore??a.
         * Potom t??to met??da zabezpe???? aby po??et potomkov rankov (k-1) a (k-2)
         * bol men???? alebo rovn?? 7 . M????e sa sta?? ??e sa zv????i rank kore??a.
         * @param childRank 
         */
        private void checkLargeChildrenUpper(int childRank) {
            if (this.childrenCount[childRank] <= 7) {
                return;
            }

            if (childRank == this.rootNode.getRank() - 2) {
                BrodalNode newChild = this.linkChildren(childRank);
                this.addChild(newChild, false);
            } else {
                BrodalNode firstNew  = this.linkChildren(childRank);
                BrodalNode secondNew = this.linkChildren(childRank);
                this.increaseRank(firstNew, secondNew);
            }
        }

        /**
         * Nech k je rank kore??a.
         * Potom t??to met??da zabezpe???? aby po??et potomkov rankov (k-1) a (k-2)
         * bol v???????? alebo rovn?? 2. M????e sa sta?? ??e sa zmen???? rank kore??a.
         * @param childRank 
         */
        private void checkLargeChildrenLower(int childRank) {
            if (this.childrenCount[childRank] >= 2) {
                return;
            }

            if (childRank == this.rootNode.getRank() - 2) {
                // jednoduch?? pr??pad len si zoberie 2-3 z itemov najvy????ieho ranku
                // ktor?? ale treba potom skontrolova?? lebo sa mohlo sta??,
                // ??e ostal len jeden
                DelinkingResult result = this.delinkChild(childRank + 1); //TODO delink child nie je treba viz c++ implemetacia
                BrodalNode item = result.getDelinkedItems();

                // tieto s?? ur??ite ranku childRank tak??e ich netreba kontrolova??
                // preto??e vieme ??e childRank bol menej ako 2
                this.addLinkedItems(item, true);

                // tento m????e ma?? hociak?? rank tak??e ho treba skontrolova??
                // TODO result by sa mal prid??va?? pod t1 aj ke?? sa delinkuje u t2
    //            this.addChild(result.getExtraItem(), false); // TODO zvl?????? met??du pre addExtraItem, ktor?? si T1 a T2 implemetuj?? rozdielne
                this.addExtraItem(result.getExtraItem());
                this.checkLargeChildrenLower(childRank + 1);
            } else {
                // item najvy????ie ranku je v tomto pr??pade len jeden ??o je celkom probl??m
                // najprv sa pok??sime ho delinkova??, ak to zn????i jeho rank tak ho odstr??nime
                // a spolu s odpojen??mi norm??lne prid??me
                // ak mu ostal rovnak?? rank tak proces jednoducho opakujeme
                BrodalNode lastOne = this.children[childRank];
                BrodalNode delinkedOnes = lastOne.remove2to3LargestChildren();
                boolean rankDecreased = false;
                if (lastOne.getRank() < childRank) {
                    rankDecreased = true;
                    this.decreaseDomains();
                    this.children[childRank] = null;
                    this.childrenCount[childRank] = 0;                
                    this.rootNode.removeChild(lastOne);
                    this.addChild(lastOne, false);
                }

                this.addLinkedItems(delinkedOnes, false);

                if (!rankDecreased) {
                    // ak sme sa dostali sme znamen?? to ??e v predu je st??le
                    // len jeden item najvy????ie ranku tak??e proces opakujeme
                    this.checkLargeChildrenLower(childRank);
                }
            }
        }

        /**
         * Zo zoznamu potomkov vyberie prv??ch 3 dan??ho ranku a spoj?? ich do jedn??ho.
         * Aktualizuje v??etky potrebn?? smern??ky u parenta aj v poliach tohto wrapu.
         * Po??et potomkov ranku k je v??dy tak??, ??e sa toto spojenie d?? zrealizova??.
         * Vypl??va to z invariantov.
         * @param rank
         * @return item s rankom (rank + 1)
         */
        private BrodalNode linkChildren(int rank) {
            BrodalNode x1 = this.children[rank];
            BrodalNode x2 = x1.getNext();
            BrodalNode x3 = x2.getNext();

            this.children[rank] = x3.getNext();
            this.rootNode.removeNChildren(x1, 3);
            this.childrenCount[rank] -= 3;


            BrodalNode newRoot = this.maxPrio(x1, this.maxPrio(x2, x3));
            // men?? sa rank tak??e je lep??ie ho odstr??ni?? z viol setu
            // pri prid??van?? sa tam pr??padne znovu prid??
            // toto je tak?? proviz??rne rie??enie
            this.removeViolation(newRoot); 

            if (x1 != newRoot) {
                newRoot.addChildAtFront(x1);
            }
            if (x2 != newRoot) {
                newRoot.addChildAtFront(x2);
            }
            if (x3 != newRoot) {
                newRoot.addChildAtFront(x3);
            }

            return newRoot;
        }

        /**
         * Rozdel?? child ranku k na 2 - 3 itemy ranku (k-1)
         * a jeden item ranku najviac k.
         * !!! Pou????va removeChildUnchecked tak??e po??et childov ranku k je
         * treba v??dy skontrolova??!
         * @param rank 
         */
        private DelinkingResult delinkChild(int rank) {
            BrodalNode childToDelink = this.removeChildUnchecked(rank);
            this.removeViolation(childToDelink);
            BrodalNode result = childToDelink.remove2to3LargestChildren();

            return new DelinkingResult(result, childToDelink);
        }

        private BrodalNode maxPrio(BrodalNode x1, BrodalNode x2) {
            return x1.compareTo(x2) < 0 ? x1 : x2;
        }

        /**
         * Zobrazuje po??ty potomkov z {2, ..., 8} do {0, 1, 2, 3}
         * pre guide zabezpe??uj??ci horn?? hranicu po??tu potomkov.
         * @param count
         * @return 
         */
        private int guideNumberUpper(int count) {
            if (count <= 5) {
                return 0;
            } else {
                return count - 5;
            }
        }

        /**
         * Zobrazuje po??ty potomkov z {1, ..., 7} do {0, 1, 2, 3}
         * pre guide zabezpe??uj??ci doln?? hranicu po??tu potomkov.
         * @param count
         * @return 
         */
        private int guideNumberLower(int count) {
            return this.guideNumberUpper(-count + 9);
        }

    }
    
    private class RootWrapT1<E extends Comparable<E>> extends RootWrap<E> {

        private RootWrapT2 t2Wrap;
        private final Guide guideViolation;
        private int[] violationCount;

        public RootWrapT1(BrodalNode rootItem) {
            super(rootItem);
            this.guideViolation = new Guide(new ReducerViolation(this));
            this.violationCount = new int[4];
            this.t2Wrap = null;
        }

        public void reduceViolation(int rank) {
            int t2ViolsRemoved = this.t2ViolationsReduce(rank);

            if (t2ViolsRemoved < 2) {
                this.normalViolationsReduce(rank, 2 - t2ViolsRemoved);
            }
        }

        public int getValViolation(int index) {
            return this.guideNumberViolation(this.violationCount[index]);
        }

        @Override
        public void increaseRank(BrodalNode... newChildren) {
            for (BrodalNode child : newChildren) {
                // ak sa prid??va pod t1 ur??ite violating nebude
                this.removeViolation(child);
            }

            super.increaseRank(newChildren);

        }

        @Override
        public void addChild(BrodalNode child, boolean guideAdding) {
            // ak sa prid??va pod t1 ur??ite violating nebude
            // m????e sa sta?? ??e pri prid??van?? sa niektor?? itemy linkuj??
            // a child neskon???? ako priamy potomok t1, to ale nevad?? 
            // lebo pri linkovan?? sa zachov??va heap order
            this.removeViolation(child);
            super.addChild(child, guideAdding);
        }

        public void setOtherWrap(RootWrapT2 wrap) {
            this.t2Wrap = wrap;
        }

        public void addPossibleViolation(BrodalNode violatingItem, boolean guideAdding) {
            this.removeViolation(violatingItem);

            if (!violatingItem.isViolating()) {
                return;
            }

            if (violatingItem.getRank() < this.getRootNode().getRank()) {
                this.addViolationSmall(violatingItem, guideAdding);
            } else {
                this.addViolationLarge(violatingItem);
            }
        }

        /**
         * Uprav?? W set tak, aby z ka??d??ho ranku zostala najviac 1 violation.
         */
        public void totallyReduceWViolations() {
            BrodalNode rootItem = this.getRootNode();
            for (int i = 0; i < this.violationCount.length; i++) {

                while (this.violationCount[i] > 1) {
                    // ke?? sa t??to met??da pou????va tak T2 je pr??zdny tak??e v??etky violation s?? v T1
                    this.normalViolReduceSingle(rootItem.getWViolation(i));
                }

            }
        }

        /**
         * Presunie violations z V do W rootu.
         * Violations v newMin sa tie?? presun?? do W rootu.
         * @param newMin 
         */
        public void mergeViolationSets(BrodalNode newMin) {
            this.mergeViolationSetIntoW(this.getRootNode().disconnectSetV());
            this.mergeViolationSetIntoW(newMin.disconnectSetV());
            this.mergeViolationSetIntoW(newMin.disconnectSetW());
        }

        /**
         * Pre pr??pad ??e sa prid??vaj?? linkovan?? itemy rovnak??ho ranku.
         * Itemy musia by?? aspo?? 2 ranku rovnak??ho ako tento.
         * @param linked 
         */
        public void incRankAndAddRest(BrodalNode linked) {
            BrodalNode x1 = linked;
            linked = linked.getNext();
            BrodalNode x2 = linked;
            linked = linked.getNext();
            this.increaseRank(x1, x2);
            if (linked != null) {
                this.addLinkedItems(linked, false);
            }
        }

        /**
         * Korektne odstr??ni item z violation listu.
         * Za t??mto ????elom by sa mala pou????va?? len t??to met??da.
         * @param item 
         */
        @Override
        public void removeViolation(BrodalNode item) {
            int rank = item.getRank();
            this.removeViolation(item, rank);
        }

        @Override
        public void addExtraItem(BrodalNode item) {
            this.addChild(item, false);
        }

        @Override
        protected void increaseDomains() {
            super.increaseDomains();

            int newIndexToGuide = this.getRootNode().getRank() - 1;

            if (this.violationCount.length == newIndexToGuide + 1) {
                this.violationCount = ArrayUtils.doubleIntArray(this.violationCount);
            }

            this.guideViolation.increaseDomain();
            this.guideViolation.possiblyIncrease(
                newIndexToGuide, 
                this.guideNumberViolation(this.violationCount[newIndexToGuide])
            );
        }

        @Override
        protected void decreaseDomains() {
            super.decreaseDomains();
            this.guideViolation.decreaseDomain();
        }

        private void removeViolation(BrodalNode item, int trueRank) {
            BrodalNode t1Root = this.getRootNode();

            if (!item.isInVSet() && item.getInThisOnesSet() == t1Root) {
                this.violationCount[trueRank]--;
            }

            item.removeFromViolationSet(trueRank);
        }

        private void addViolationSmall(BrodalNode violatingItem, boolean guideAdding) {
            this.getRootNode().addToWSet(violatingItem);
            int rank = violatingItem.getRank();
            this.violationCount[rank]++;
            if (!guideAdding) {
                this.guideViolation.possiblyIncrease(rank, this.getValViolation(rank));
            }
        }

        private void addViolationLarge(BrodalNode violationItem) {
            this.getRootNode().addToVSet(violationItem);
        }

        /**
         * Zobrazuje po??ty violation z {0, ... , 7} do {0, 1, 2}.
         * @param count
         * @return 
         */
        private int guideNumberViolation(int count) {
            if (count < 5) {
                return 0;
            } else {
                return count - 4;
            }
        }

        /**
         * Zist?? ko??k?? z itemov vo violation sete ranku violItem.getRank()
         * s?? potomkovia t2. 
         * @param violItem
         * @return 
         */
        private int t2ViolationsCount(int rank) {
            if (this.t2Wrap == null) {
                return 0;
            }

            BrodalNode t2Root = this.t2Wrap.getRootNode();
            BrodalNode wItem = this.getRootNode().getWViolation(rank);
            int count = 0;
            while (wItem != null && wItem.getRank() == rank) {
                if (wItem.getParent() == t2Root) {
                    count++;
                }
                wItem = wItem.getNextInSet();
            }

            return count;
        }

        /**
         * Vr??ti jednu alebo dve violations, ktor?? nie su sons of t2.
         * Ak by bol count > 2 tak hod?? ??kared?? Exception.
         * @param rank
         * @param count
         * @return 
         */
        private NodePair getNormalViolations(int rank, int count) {
            BrodalNode[] ret = new BrodalNode[2];
            BrodalNode wItem = this.getRootNode().getWViolation(rank);
            BrodalNode t2Root = this.t2Wrap != null ? this.t2Wrap.getRootNode() : null;
            int foundCount = 0;

            while (foundCount < count) {
                if (wItem.getParent() != t2Root) {
                    ret[foundCount++] = wItem;
                }
                wItem = wItem.getNextInSet();
            }

            return new NodePair(ret[0], ret[1]);
        }

        /**
         * Odstr??ni count violations, ktor?? s?? sons of t2
         * a prid?? ich pod t1.
         * @param rank
         * @param toRemoveCount 
         */
        private int t2ViolationsReduce(int rank) {
            BrodalNode t1 = this.getRootNode();
            BrodalNode wItem = t1.getWViolation(rank);
            int removedCount = 0;

            while (wItem != null && wItem.getRank() == rank && removedCount < 2) {
                BrodalNode toRemoveItem = wItem;
                wItem = wItem.getNextInSet();

                if (toRemoveItem.isSonOfRoot()) {
                    removedCount++;
                    this.t2Wrap.removeChild(toRemoveItem);
                    this.addChild(toRemoveItem, false);
                }
            }

            return removedCount;
        }

        /**
         * Normal violation je violating item, ktor?? nie je son t2.
         * @param rank
         * @param toRemoveCount 
         */
        private void normalViolationsReduce(int rank, int toRemoveCount) {
            if (toRemoveCount == 1) {
                BrodalNode x1 = this.getNormalViolations(rank, 1).getFirst();
                this.normalViolReduceSingle(x1);
            } else {
                NodePair items = this.getNormalViolations(rank, 2);
                BrodalNode x1 = items.getFirst();
                BrodalNode x2 = items.getSecond();

                if (!x1.isSibling(x2)) {
                    this.makeSiblings(x1, x2);
                }

                this.normalViolReduceSingle(x1);
                this.normalViolReduceSingle(x2);
            }
        }

        private void normalViolReduceSingle(BrodalNode x1) {
            this.removeViolation(x1);

            if (!x1.isViolating()) {
                return;
            }

            if (x1.sameRankSiblingCount() > 2) {
                x1.getParent().removeChild(x1);
                this.addChild(x1, false);
            } else {
                this.cutWithSibling(x1);
            }
        }

        private void cutWithSibling(BrodalNode x1) {
            BrodalNode parent = x1.getParent();
            BrodalNode sibling = x1.getSameRankSibling();

            int originalParentRank = parent.getRank();

            parent.removeChild(x1);
            parent.removeChild(sibling); 

            int actualParentRank = parent.getRank();

            if (actualParentRank < originalParentRank) {
                if (parent.isSonOfRoot()) {                
                    this.removeChild(parent, originalParentRank);
                } else {
                    // replacement sa mus?? removnu?? unsafe aby sa nespustili guide transform??cie
                    // preto??e parent m?? v tomto bode in?? rank a pracuje sa s n??m tak??e nie
                    // je dobr?? vykon??va?? 2 transform??cie s????asne
                    BrodalNode parentReplacement = this.removeChildUnchecked(originalParentRank);
                    parent.replaceWith(parentReplacement); 
                    this.addPossibleViolation(parentReplacement, true);
                    this.checkChildrenLower(originalParentRank);
                }

                this.removeViolation(parent, originalParentRank);
                this.addChild(parent, false); 
            }

            this.addChild(x1, false);
            this.addChild(sibling, false);
        }

        /**
         * Z x1 a x2 sprav?? s??rodencov tak by nevznikla nov?? violation.
         * Ke????e nemen?? ni?? v ??trukt??re stromov ale iba prehod?? entries,
         * tak vr??ti nov?? 2 itemy, ktor?? obsahuj?? p??vodn?? d??ta a prioritu
         * a ktor?? s?? s??rodenci. Nov?? violation tu nem????e vznikn????.
         * @param x1
         * @param x2 
         * @return 
         */
        private void makeSiblings(BrodalNode x1, BrodalNode x2) {
            if (x1.getParent().compareTo(x2.getParent()) < 0) {
                BrodalNode sibling = x2.getSameRankSibling();
                x1.swapWith(sibling);
            } else {
                BrodalNode sibling = x1.getSameRankSibling();
                x2.swapWith(sibling);
            }
        }

        private void mergeViolationSetIntoW(BrodalNode set) {
            while (set != null) {
                BrodalNode next = set.getNextInSet();
                this.addPossibleViolation(set, false);
                set = next;
            }
        }

    }

    private class RootWrapT2<E extends Comparable<E>> extends RootWrap<E> {

        private RootWrapT1 t1Wrap;

        public RootWrapT2(BrodalNode rootItem) {
            super(rootItem);
        }

        /**
         * Move kon??truktor ktor?? presunie obsah otherWrap do tejto in??tancie.
         * @param otherWrap 
         */
        public RootWrapT2(RootWrap otherWrap) {
            super(otherWrap);
        }

        @Override
        public void addChild(BrodalNode child, boolean guideAdding) {
            super.addChild(child, guideAdding);
            this.t1Wrap.addPossibleViolation(child, false);
        }

        public void setOtherWrap(RootWrapT1 wrap) {
            this.t1Wrap = wrap;
        }

        @Override
        public void removeViolation(BrodalNode item) {
            this.t1Wrap.removeViolation(item);
        }

        @Override
        public void addExtraItem(BrodalNode item) {
            if (item.getRank() < this.t1Wrap.getRootNode().getRank()) {
                this.t1Wrap.addExtraItem(item);
            } else {
                this.addChild(item, false);
            }
        }

    }
    
    private abstract class Reducer {

        private final RootWrap root;

        public Reducer(RootWrap root) {
            this.root = root;
        }

        protected RootWrap getRoot() {
            return this.root;
        }

        public abstract void reduce(int rank);

        public abstract int getValue(int index);

    }
    
    private class ReducerLower extends Reducer {

        public ReducerLower(RootWrap root) {
            super(root);
        }

        @Override
        public void reduce(int rank) {
            this.getRoot().reduceLowerBound(rank);
        }

        @Override
        public int getValue(int index) {
            return this.getRoot().getValLowerBound(index);
        }

    }
    
    private class ReducerUpper extends Reducer {

        public ReducerUpper(RootWrap root) {
            super(root);
        }

        @Override
        public void reduce(int rank) {
            this.getRoot().reduceUpperBound(rank);
        }

        @Override
        public int getValue(int index) {
            return this.getRoot().getValUpperBound(index);
        }

    }
    
    private class ReducerViolation extends Reducer {

        private final RootWrapT1 root;
    
        public ReducerViolation(RootWrapT1 root) {
            super(null);
            this.root = root;
        }

        @Override
        public void reduce(int rank) {
            this.root.reduceViolation(rank);
        }

        @Override
        public int getValue(int index) {
            return this.root.getValViolation(index);
        }

    }
    
    private class Guide {

        private int domainSize;
        private int[] numbers;
        private FirstInBlock[] blockPointers;
        private Reducer reducer;
        
        public Guide(Reducer reducer) {
            this.domainSize = 0;
            this.numbers = new int[4];            
            this.blockPointers = new FirstInBlock[4];
            this.reducer = reducer;
        }

        public void increaseDomain() {
            if (this.numbers.length == this.domainSize) {
                this.numbers = ArrayUtils.doubleIntArray(this.numbers);
                this.blockPointers = (FirstInBlock[])ArrayUtils.doubleRefArray(this.blockPointers, this.blockPointers[0].getClass());
            }

            this.blockPointers[this.domainSize] = new FirstInBlock(null);
            this.domainSize++;
        }

        public void decreaseDomain() {
            if (this.domainSize < 1) {
                return;
            }

            this.domainSize--;
            if (this.blockPointers[this.domainSize] != null && this.blockPointers[this.domainSize].index != null) {
                this.blockPointers[this.domainSize].index = null;
                this.numbers[this.domainSize] = 0;
            }
        }

        public void setReducer(Reducer newReducer) {
            this.reducer = newReducer;
        }

        public void possiblyIncrease(int index, int val) {
            if (this.numbers[index] >= val) {
                return;
            }

            if (this.isInBlock(index)) {
                this.incInBlock(index);
            } else {
                this.incOutOfBlock(index);
            }
        }

        private void incInBlock(int i) {
            if (this.numbers[i] >= this.reducer.getValue(i)) {
                return;
            }

            this.numbers[i] = this.reducer.getValue(i);

            if (this.numbers[i] == 1 || this.numbers[i] == 2) {
                // 0 -> 1
                // blok, ktor?? t??to 0 uzatv??rala sa zru???? a 2, ktor?? ho za????nala 
                // sa vyrie??i ako 2, ktor?? u?? nepatr?? do ??iadneho bloku

                // 1 -> 2
                // taktie?? sa zru???? blok a e??te k tomu sa zru???? aj t??to 2

                int firstInBlock = this.blockPointers[i].index;
                this.blockPointers[i].index = null;
                this.incOutOfBlock(firstInBlock);

                if (this.numbers[i] == 2) {
                    this.incOutOfBlock(i);
                }
            } else {
                // 2 -> 3
                // blok, ktor?? t??to 2 za????nala sa zru???? a ????slo pred ??ou sa
                // porie??i u?? existuj??cimi met??dami pod??a toho ??i je alebo nie je
                // z nejak??ho bloku

                this.reduce(i);
                this.blockPointers[i].index = null;

                if (this.isInBlock(i + 1)) {
                    // ak bolo ????slo pred v bloku tak to musela by?? 0
                    // a na jej zmenu sta???? len jeden reduce

                    this.incInBlock(i + 1); 
                } else if (i != this.domainSize - 1) {
                    this.incOutOfBlock(i + 1);
                }
            }
        }

        private void incOutOfBlock(int i) {
            this.numbers[i] = this.reducer.getValue(i);

            if (this.numbers[i] < 2) {
                return;
            }

            this.reduce(i);

            if (this.isInBlock(i + 1) && this.reducer.getValue(i + 1) == 1) {
                // kedze cislo na (i+1) patrilo do bloku a cislo na (i) nepatrilo
                // tak cislo na (i+1) bola 0, z ktorej sa mozno stala 1
                // z cisla na (i) sa stala 0 takze blok sa rozsiril na (i)
                // ..., 0, 2 -> ..., 1, 0

                this.numbers[i + 1] = 1;
                this.blockPointers[i] = this.blockPointers[i + 1];
            } else if (i < this.domainSize - 1 && this.reducer.getValue(i + 1) == 2) {
                // ak na (i+1) bola 0 a stala sa z nej 1 a ni?? netreba robi??
                // ak tam bola 1 a stala sa z nej 2 tak vznikol nov?? blok 2, 0

                this.numbers[i + 1] = 2;
                FirstInBlock newBlock = new FirstInBlock(i + 1);
                this.blockPointers[i] = newBlock;
                this.blockPointers[i + 1] = newBlock;
            }
        }

        private void reduce(int i) {
            this.reducer.reduce(i);

            this.numbers[i] = this.reducer.getValue(i); 
        }

        private boolean isInBlock(int index) {
            return index < this.domainSize && this.blockPointers[index].index != null;
        }

        private class FirstInBlock<E> {

            private Integer index;

            public FirstInBlock(Integer index) {
                this.index = index;
            }

        }
        
    }
    
    private class NodePair {
        
        private final BrodalNode first;
        private final BrodalNode second;

        public NodePair(BrodalNode first, BrodalNode second) {
            this.first  = first;
            this.second = second;
        }

        public BrodalNode getFirst() {
            return this.first;
        }

        public BrodalNode getSecond() {
            return this.second;
        }
    }
    
    private class DelinkingResult {

        private final BrodalNode delinkedItems;
        private final BrodalNode extraItem;

        public DelinkingResult(BrodalNode delinkedItems, BrodalNode extraItem) {
            this.delinkedItems = delinkedItems;
            this.extraItem = extraItem;
        }

        /**
         * Vr??ti 2 alebo 3 zre??azen?? itemy ranku (k-1).
         * @return 
         */
        public BrodalNode getDelinkedItems() {
            return this.delinkedItems;
        }

        /**
         * Vr??ti 1 item ranku najviac k.
         * @return 
         */
        public BrodalNode getExtraItem() {
            return this.extraItem;
        }

    }
    
    private class BrodalWrapList {

        private final List<RootWrap> wraps;
        private final List<RootWrap> sameRanked;
        private RootWrap maxRank;
        private RootWrap maxPrio;

        public BrodalWrapList() {
            this.wraps = new ArrayList<>();
            this.sameRanked = new ArrayList<>();
            this.maxRank = null;
            this.maxPrio = null;
        }

        public void addIfNotuNull(RootWrap... wraps) {
            for (RootWrap wrap : wraps) {
                if (wrap == null) {
                    continue;
                }

                this.wraps.add(wrap);

                if (this.maxRank != null) {
                    if (wrap.getRootNode().getRank() > this.maxRank.getRootNode().getRank()) {
                        this.maxRank = wrap;
                    }
                } else {
                    this.maxRank = wrap;
                }

                if (this.maxPrio != null) {
                    if (wrap.getRootNode().compareTo(this.maxPrio.getRootNode()) < 0) {
                        this.maxPrio = wrap;
                    }
                } else {
                    this.maxPrio = wrap;
                }
            }
        }

        public void addSameRank(RootWrap wrap) {
            this.sameRanked.add(wrap);
        }

        public RootWrap popWrap() {
            return this.wraps.remove(this.wraps.size() - 1);
        }

        public RootWrap popSameRank() {
            return this.sameRanked.remove(this.sameRanked.size() - 1);
        }

        public BrodalNode[] sameRankedAsArray() {
            BrodalNode[] array = new BrodalNode[this.sameRanked.size()];

            for (int i = 0; i < this.sameRanked.size(); i++) {
                array[i] = this.sameRanked.get(i).getRootNode();
            }

            return array;
        }

        public RootWrap getMaxRank() {
            return this.maxRank;
        }

        public RootWrap getMaxPrio() {
            return this.maxPrio;
        }

        public int wrapCount() {
            return this.wraps.size();
        }

        public int sameRankCount() {
            return this.sameRanked.size();
        }

        public void removeMaxesFromList() {
            this.wraps.removeIf( (wrap) -> wrap == this.maxPrio || wrap == this.maxRank );
        }

    }
           
}
