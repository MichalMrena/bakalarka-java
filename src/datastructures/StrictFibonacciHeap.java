package datastructures;

public class StrictFibonacciHeap<E extends Comparable<E>> implements IPriorityQueue<E> {

    private long nodeId;
    private int size;
    private StrictFibNode  root;
    private ActiveRecord   activeRecord;
    private StrictFibNode  nonLinkable;
    private StrictFibNode  queueHead;
    private RankListRecord rankList;
    private FixListRecord  fixListActRoots;
    private FixListRecord  fixListLoss;

    public StrictFibonacciHeap() {
        this.size = 0;
        this.rankList = new RankListRecord();
        this.activeRecord = new ActiveRecord(true);
        this.nodeId = Long.MIN_VALUE;
    }
    
    @Override
    public QueueEntry<E> insert(E data) {
        StrictFibEntry entry = new StrictFibEntry(data);
        StrictFibNode node = new StrictFibNode(this.nodeId++);
        entry.node = node;
        node.entry = entry;

        if (this.isEmpty()) {
            this.root = node;
            node.left = null;
            node.right = null;
        } else {
            if (node.compareTo(this.root) < 0) {
                node.addPassiveChild(this.root);
                this.nonLinkable = (this.root.isPassiveLinkable()) ? null : this.root;
                this.root.qprev = this.root;
                this.root.qnext = this.root;
                this.prependQueue(this.root);
                this.root = node;
            } else {
                this.root.addPassiveChild(node);
                this.prependQueue(node);
            }
        }

        while (this.doActiveRootReduce()) {};
        while (this.doRootDegreeReduce()) {};

        ++this.size;
        
        return entry;
    }

    @Override
    public IPriorityQueue<E> meld(IPriorityQueue<E> other) {
        /*
            Pre experimenty táto operácia nebola potrebná.
            Je implementovaná v C++ verzii štruktúr.
        */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E deleteMin() {
        if (this.isEmpty()) {
            return null;
        }

        Object retData = this.root.entry.getData();

        if (this.size > 1) {
            StrictFibNode x = this.findNewRoot();
            this.makePassive(x);
            this.rmRootChild(x);
            this.moveChildrenTo(x);
            this.removeFromQueue(x);

            for (int i = 0; i < 2; i++) {
                if (this.queueHead == null) break;

                StrictFibNode passive1 = this.queueHead.disconn1PassChild();
                StrictFibNode passive2 = this.queueHead.disconn1PassChild();

                if (passive1 != null) {
                    this.addRootChild(passive1);
                }

                if (passive2 != null) {
                    this.addRootChild(passive2);
                }

                this.queueHead = this.queueHead.right;
                this.doLossReduce();
            }

            while (this.doActiveRootReduce()) {};
            while (this.doRootDegreeReduce()) {};

        } else {
            this.root = null;
        }

        --this.size;		

        return (E)retData;
    }

    @Override
    public E findMin() {
        if (this.isEmpty()) {
            return null;
        } else {
            return (E)this.root.entry.getData();
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void decreaseKey(QueueEntry<E> entry) {
        StrictFibEntry entrySf = (StrictFibEntry)entry;
        StrictFibNode x = entrySf.node;

        if (x.isRoot()) {
            return;
        }

        if (x.compareTo(this.root) < 0) {
            this.root.swapEntries(x);
        }

        if (x.isViolating()) {
            StrictFibNode y = x.parent;

            if (x.isActive() && !x.isActiveRoot()) {
                if (x.loss > 0) {
                    FixListRecord flr = x.flRecord;
                    this.rmFlLossChckd(flr);
                    x.loss = 0;
                }
                
                y.removeChild(x);
                this.addRootChild(x);
                this.addToFixList(x, x.rank, true);
                this.decreaseRank(y);
            } else {
                y.removeChild(x);
                this.addRootChild(x);
            }

            if (y.isActive() && !y.isActiveRoot()) {
                this.increaseLoss(y);
                this.doLossReduce();
            }

            int counter1 = 0;
            int counter2 = 0;
            while (++counter1 <= 6 && this.doActiveRootReduce());
            while (++counter2 <= 4 && this.doRootDegreeReduce());
        }
    }

    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
        this.fixListActRoots = null;
        this.fixListLoss = null;
        this.rankList = new RankListRecord();
    }
    
    // Heap operacie
    
    private void addRootChild(StrictFibNode node) {
        if (node.isPassiveLinkable()) {
            this.root.addPassiveChild(node);
            return;
        }

        if (this.nonLinkable != null) {
            if (this.nonLinkable.isActive()) {
                this.nonLinkable.addAfter(node);
                this.nonLinkable = node; 
            } else {
                if (node.isActive()) {
                    this.root.addActiveChild(node);
                } else {
                    this.nonLinkable.addAfter(node);
                }
            }
        } else {
            this.root.addActiveChild(node);
            this.nonLinkable = node;
        }
    }
    
    private void rmRootChild(StrictFibNode node) {
        if (node == node.left) {
            this.nonLinkable = null;
        }  else if (node == this.nonLinkable) {
            if (node.right.isPassive() && !node.right.isPassiveLinkable()) {
                this.nonLinkable = node.right;
            } else if (node.left.isActive()) {
                this.nonLinkable = node.left;
            } else {
                this.nonLinkable = null;
            }
        }

        this.root.removeChild(node);
    }
    
    private void prependQueue(StrictFibNode node) {
        if (this.queueHead != null) {
            StrictFibNode last = this.queueHead.qprev;
            node.qprev = last;
            node.qnext = this.queueHead;
            last.qnext = node;
            this.queueHead.qprev = node;
            this.queueHead = node;
        } else {
            this.queueHead = node;
        }
    }
    
    private void removeFromQueue(StrictFibNode node) {
        if (node == node.qnext) {
            this.queueHead = null;
        } else {
            if (this.queueHead == node) {
                this.queueHead = node.qnext;
            }

            node.qprev.qnext = node.qnext;
            node.qnext.qprev = node.qprev;
        }

        node.qprev = null;
        node.qnext = null;       
    }
    
    // deleteMin utils
    
    private void rootify(StrictFibNode min) {
        this.setNewNonlinkable(min);
        this.root = min;
        this.root.left  = null;
        this.root.right = null;        
    }
    
    private void makePassive(StrictFibNode min) {
        if (!min.isActive()) {
            return;
        }

        this.rmFlActRootChckd(min.flRecord);

        min.makePassive();

        StrictFibNode it = min.child;
        StrictFibNode endIt = min.child;

        while (it != null && it.isActive()) {
            if (it.loss > 0) {
                this.rmFlLossChckd(it.flRecord);
                it.loss = 0;
            }

            this.addToFixList(it, it.rank, true);
            it = it.right;

            if (it == endIt) break;
        }
    }
    
    private void moveChildrenTo(StrictFibNode min) {
        if (this.root.child == null) {
                this.rootify(min);
        } else {
            StrictFibNode it = this.root.child;
            do {
                it.parent = min;
                it = it.right;
            } while (it != this.root.child);

            this.moveActiveChildren(min);

            StrictFibNode oldchild = this.root.child;
            this.root.child = null;
            this.rootify(min);

            if (oldchild == null) {
                return;
            }

            oldchild.left.right = null;
            while (oldchild != null) {
                StrictFibNode nextit = oldchild.right;
                this.addRootChild(oldchild);
                oldchild = nextit;
            }
        }     
    }
    
    private void moveActiveChildren(StrictFibNode min) {
        StrictFibNode first = this.root.child;

        if (!first.isActive()) {
            return;
        }

        StrictFibNode last = (this.nonLinkable.isActive() ? this.nonLinkable : this.nonLinkable.left);

        if (!this.root.child.left.isActive()) {
            StrictFibNode rightmost = this.root.child.left;
            this.root.child = last.right;

            first.left = last;
            last.right = first;

            this.root.child.left = rightmost;
            rightmost.right = this.root.child;
        } else {
            this.root.child = null;
        }

        if (min.child == null) {
            min.child = first;
        } else {
            StrictFibNode rightmost = min.child.left;

            min.child.left = last;
            last.right = min.child;

            rightmost.right = first;
            first.left = rightmost;
            min.child = first;
        }    
    }
    
    private void setNewNonlinkable(StrictFibNode min) {
        if (min.child == null) {
            this.nonLinkable = null;
        } else if (min.child.left.isActive()) {
            this.nonLinkable = min.child.left;
        } else {
            this.nonLinkable = min.child.left;
            while (this.nonLinkable.left.isPassive()) {
                if (this.nonLinkable.left == min.child.left) break;
                this.nonLinkable = this.nonLinkable.left;
            }

            if (this.nonLinkable.isPassiveLinkable()) {
                this.nonLinkable = this.nonLinkable.left.isActive() ? this.nonLinkable.left : null;
            }
        }
    }
    
    private StrictFibNode findNewRoot() {
        StrictFibNode newRoot = this.root.child;
        StrictFibNode endit = newRoot;
        StrictFibNode iterated = newRoot.right;

        while (iterated != endit) {
            if (iterated.compareTo(newRoot) < 0) {
                newRoot = iterated;
            }

            iterated = iterated.right;
        }

        return newRoot;
    }
    
    // Transformacie
    
    private boolean doActiveRootReduce() {
        if (this.fixListActRoots == null) {
            return false;
        }

        FixListRecord fx = this.fixListActRoots;
        FixListRecord fy = fx.right;

        if (fx == fy || fx.rank != fy.rank) {
            return false;
        }

        StrictFibNode x = fx.node;
        StrictFibNode y = fy.node;

        this.rmFlActRootUnchcked(fy);
        this.rmFlActRootChckd(fx);

        NodePtr px = new NodePtr(x);
        NodePtr py = new NodePtr(y);

        this.sort(px, py);
        this.activeRootReduce(px.val, py.val);
        this.addToFixList(px.val, px.val.rank, true);

        return true;
    }
    
    private boolean doRootDegreeReduce() {
        if (this.root.child == null) {
            return false;
        }

        StrictFibNode x = this.root.child.left;
        StrictFibNode y = x.left;
        StrictFibNode z = y.left;

        if (!(x.isPassiveLinkable() && y.isPassiveLinkable() && z.isPassiveLinkable())) {
            return false;
        }

        if (x == y || y == z || x == z) {
            return false;
        }

        this.rmRootChild(x);
        this.rmRootChild(y);
        this.rmRootChild(z);
        
        NodePtr px = new NodePtr(x);
        NodePtr py = new NodePtr(y);
        NodePtr pz = new NodePtr(z);

        this.sort(px, py, pz);
        this.rootDegreeReduce(px.val, py.val, pz.val);

        return true;
    }
    
    private boolean doLossReduce() {
        if (this.fixListLoss == null) {
            return false;
        }

        FixListRecord fx = this.fixListLoss;
        StrictFibNode x = fx.node;

        if (x.loss >= 2) {
            this.rmFlLossChckd(fx);
            this.oneNodeLossReduce(x);
            return true;
        }

        FixListRecord fy = fx.right;
        StrictFibNode y = fy.node;

        if (fy == fx || fx.rank != fy.rank) {
            return false;
        }

        if (y.loss >= 2) {
            this.rmFlLossChckd(fy);
            this.oneNodeLossReduce(y);
            return true;
        }

        this.rmFlLossUnchcked(fy);
        this.rmFlLossChckd(fx);

        NodePtr px = new NodePtr(x);
        NodePtr py = new NodePtr(y);
        
        this.sort(px, py);
        this.twoNodeLossReduce(px.val, py.val);

        return true;        
    }
    
    private void activeRootReduce(StrictFibNode x, StrictFibNode y) {
        if (y.isSonOfRoot()) {
            this.rmRootChild(y);
        } else {
            y.parent.removeChild(y);
        }

        x.addActiveChild(y);
        this.justIncRank(x);
        StrictFibNode z = x.disconn1PassChild();
        if (z != null) {
            this.addRootChild(z);
        }
    }
    
    private void rootDegreeReduce(StrictFibNode x, StrictFibNode y, StrictFibNode z) {
//        x.makeActive(this.activeRecord, this.rankList);
//        y.makeActive(this.activeRecord, this.rankList);
//
//        this.justIncRank(x);
//        this.addToFixList(x, x.rank, true);
//
//        x.loss = 0;
//        y.loss = 0;
//
//        x.addActiveChild(y);
//        y.addPassiveChild(z);
//        this.addRootChild(x);

        x.makeActive(this.activeRecord, this.rankList);
        y.makeActive(this.activeRecord, this.rankList);

        this.justIncRank(x);
        this.addToFixList(x, x.rank, true);
        
        x.loss = 0;
        y.loss = 0;

        x.addActiveChild(y);
        y.addPassiveChild(z);
        this.addRootChild(x);
    }
    
    private void oneNodeLossReduce(StrictFibNode x) {
        StrictFibNode y = x.parent;
        y.removeChild(x);
        x.loss = 0;
        this.addRootChild(x);
        this.addToFixList(x, x.rank, true);
        
        this.decreaseRank(y);
        if (!y.isActiveRoot())
        {
            this.increaseLoss(y);
        }
    }
    
    private void twoNodeLossReduce(StrictFibNode x, StrictFibNode y) {
        x.loss = 0;
        y.loss = 0;

        StrictFibNode z = y.parent;

        z.removeChild(y);
        x.addActiveChild(y);
        this.justIncRank(x);
        this.decreaseRank(z);

        if (!z.isActiveRoot()) {
            this.increaseLoss(z);
        }
    }
    
    // FixLists
    
    private void rmFlActRootUnchcked(FixListRecord record) {
        record.node.rank = record.rank;

        if (record.left == record) {
            this.fixListActRoots = null;
        } else if (this.fixListActRoots == record) {
            this.fixListActRoots = record.right;
        }

        record.left.right = record.right;
        record.right.left = record.left;
    }
    
    private void rmFlLossUnchcked(FixListRecord record) {
        record.node.rank = record.rank;

        if (record.left == record) {
            this.fixListLoss = null;
        } else if (this.fixListLoss == record) {
            this.fixListLoss = record.right;
        }

        record.left.right = record.right;
        record.right.left = record.left;
    }
    
    private void rmFlActRootChckd(FixListRecord record) {
        RankListRecord rank = record.rank;

        if (rank.activeRoots == record) {
            if (record.left == record) {
                rank.activeRoots = null;
            } else if (record.right.rank == rank) {
                rank.activeRoots = record.right;
            } else {
                rank.activeRoots = null;
            }
        }

        this.rmFlActRootUnchcked(record);

        if (rank.activeRoots != null) {
            if (this.isSingle(rank.activeRoots)) {
                this.rmFlActRootUnchcked(rank.activeRoots);
                this.appendFlActRoot(rank.activeRoots);
            }
        }
    }
    
    private void rmFlLossChckd(FixListRecord record) {
        RankListRecord rank = record.rank;

        if (rank.loss == record) {
            if (record.left == record) {
                rank.loss = null;
            } else if (record.right.rank == rank) {
                rank.loss = record.right;
            } else {
                rank.loss = null;
            }
        }

        this.rmFlLossUnchcked(record);

        if (rank.loss != null) {
            if (this.isSingle(rank.loss)) {
                this.rmFlLossUnchcked(rank.loss);
                this.appendFlLoss(rank.loss);
            }
        }
    }
    
    private void addToFixList(StrictFibNode node, RankListRecord rank, boolean isActRoot) {
        FixListRecord record = new FixListRecord(node, rank);
        node.flRecord = record;
        if (isActRoot) {
            this.addToFlActRoot(record);
        } else {
            this.addToFlLoss(record);
        }
    }
    
    private void addToFlActRoot(FixListRecord record) {
        if (record.rank.activeRoots == null) {
            record.rank.activeRoots = record;
            this.appendFlActRoot(record);
        } else if (this.isSingle(record.rank.activeRoots)) {
            this.rmFlActRootUnchcked(record.rank.activeRoots);
            this.prependFlActRoot(record);
            this.prependFlActRoot(record.rank.activeRoots);
        } else {
            this.addAfterFl(record.rank.activeRoots, record);
        }
    }
    
    private void addToFlLoss(FixListRecord record) {
        if (record.rank.loss == null) {
            record.rank.loss = record;
            if (record.node.loss >= 2) {
                this.prepednFlLoss(record);
            } else {
                this.appendFlLoss(record);
            }
        } else if (this.isSingle(record.rank.loss)) {
            this.rmFlLossUnchcked(record.rank.loss);
            this.prepednFlLoss(record);
            this.prepednFlLoss(record.rank.loss);
        } else {
            this.addAfterFl(record.rank.loss, record);
        }
    }
    
    private void appendFlLoss(FixListRecord record) {
        if (this.fixListLoss != null) {
            this.addAfterFl(this.fixListLoss.left, record);
        } else {
            this.fixListLoss = record;
        }
    }
    
    private void appendFlActRoot(FixListRecord record) {
        if (this.fixListActRoots != null) {
            this.addAfterFl(this.fixListActRoots.left, record);
        } else {
            this.fixListActRoots = record;
        }
    }
    
    private void prepednFlLoss(FixListRecord record) {
        if (this.fixListLoss != null) {
            this.addAfterFl(this.fixListLoss.left, record);
        }

        this.fixListLoss = record;
    }
    
    private void prependFlActRoot(FixListRecord record) {
        if (this.fixListActRoots != null) {
            this.addAfterFl(this.fixListActRoots.left, record);
        }

        this.fixListActRoots = record;
    }
    
    private void addAfterFl(FixListRecord after, FixListRecord record) {
        record.left = after;
        record.right = after.right;
        after.right.left = record;
        after.right = record;
    }
    
    private void increaseLoss(StrictFibNode node) {
        if (node.loss == 0) {
            this.addToFixList(node, node.rank, false);
            ++node.loss;
        } else if (node.loss == 1 && this.isSingle(node.flRecord)) {
            this.rmFlLossUnchcked(node.flRecord);
            ++node.loss;
            this.prepednFlLoss(node.flRecord);
        } else {
            ++node.loss;
}
    }
    
    private boolean isSingle(FixListRecord record) {
        if (record == record.left || record.node.loss >= 2) {
            return false;
        } else {
            return record.rank != record.left.rank && record.rank != record.right.rank;
        }
    }
    
    // RankList
    
    private void justIncRank(StrictFibNode node) {
        RankListRecord rank = node.rank;
        if (rank.inc == null) {
            rank.inc = new RankListRecord();
            rank.inc.dec = rank;
        }
        node.rank = rank.inc;
    }
    
    private void justDecRank(StrictFibNode node) {
        node.rank = node.rank.dec;
    }
    
    private void decreaseRank(StrictFibNode node) {
        if (node.isActiveRoot()) {
            FixListRecord flr = node.flRecord;
            this.rmFlActRootChckd(flr);
            this.justDecRank(node);
            this.addToFixList(node, node.rank, true);
        } else if (node.loss > 0) {
            FixListRecord flr = node.flRecord;
            this.rmFlLossChckd(flr);
            this.justDecRank(node);
            this.addToFixList(node, node.rank, false);
        }
    }
    
    // Utils
    
    private void sort(NodePtr x, NodePtr y, NodePtr z) {
        if (y.compareTo(x) < 0) {
            y.swapVals(x);
        }
        
        if (z.compareTo(y) < 0) {
            z.swapVals(y);
        }
        
        if (y.compareTo(x) < 0) {
            y.swapVals(x);
        }
    }
    
    private void sort(NodePtr x, NodePtr y) {
        if (y.compareTo(x) < 0) {
            y.swapVals(x);
        }
    }
    
    // Nested Classes
    
    /*
        Malá pomocná trieda, ktorá obaľuje referenciu na Node.
        Používa sa keď je treba utriediť lokálne premenné v inej metóde.
        Viz. metódy sort v časti Utils
    */    
    private class NodePtr implements Comparable<NodePtr> {
        
        StrictFibNode val;

        NodePtr(StrictFibNode val) {
            this.val = val;
        }

        @Override
        public int compareTo(NodePtr o) {
            return this.val.compareTo(o.val);
        }
        
        void swapVals(NodePtr other) {
            StrictFibNode tmp = this.val;
            this.val = other.val;
            other.val = tmp;
        }
        
    }
    
    private class StrictFibEntry<E extends Comparable<E>> extends QueueEntry<E> {
        
        StrictFibNode node;
        
        public StrictFibEntry(E data) {
            super(data);
        }
        
    }
    
    private class StrictFibNode implements Comparable<StrictFibNode> {

        final long id;
        
        StrictFibEntry entry;
        
        StrictFibNode parent;
        StrictFibNode left;
        StrictFibNode right;
        StrictFibNode child;
        
        StrictFibNode qprev;
        StrictFibNode qnext;
        
        ActiveRecord active;
        RankListRecord rank;
        FixListRecord flRecord;
        
        int loss;

        StrictFibNode(long id) {
            this.active = new ActiveRecord(false);
            this.qprev = this;
            this.qnext = this;
            this.loss = -1;
            this.id = id;
        }
        
        @Override
        public int compareTo(StrictFibNode o) {
            if (this.entry.compareTo(o.entry) == 0) {
                return Long.compare(this.id, o.id);
            } else {
                return this.entry.compareTo(o.entry);
            }
        }
        
        void swapEntries(StrictFibNode other) {
            StrictFibEntry tmp = this.entry;
            this.entry = other.entry;
            other.entry = tmp;
        }
        
        void addActiveChild(StrictFibNode newChild) {
            this.addChld(newChild);
            this.child = newChild;
        }
        
        void addPassiveChild(StrictFibNode newChild) {
            this.addChld(newChild);
        }
        
        void addChld(StrictFibNode newChild) {
            if (this.child == null) {
                this.child = newChild;
                newChild.left = newChild;
                newChild.right = newChild;
            } else {
                StrictFibNode rightmost = this.child.left;
                newChild.left = rightmost;
                newChild.right = this.child;
                rightmost.right = newChild;
                this.child.left = newChild;
            }

            newChild.parent = this;
        }
        
        void addAfter(StrictFibNode sibling) {
            sibling.left = this;
            sibling.right = this.right;
            this.right.left = sibling;
            this.right = sibling;

            sibling.parent = this.parent;
        }
        
        void removeChild(StrictFibNode oldChild) {
            oldChild.parent = null;

            if (oldChild.right == oldChild) {
                    this.child = null;
            } else if (this.child == oldChild) {
                    this.child = this.child.right;
            }

            oldChild.left.right = oldChild.right;
            oldChild.right.left = oldChild.left;
            oldChild.left = oldChild;
            oldChild.right = oldChild;
        }
    
        void makeActive(ActiveRecord actRec, RankListRecord rank) {
            this.active = actRec;
            this.rank = rank;
        }
        
        void makePassive() {
            this.active = new ActiveRecord(false);
        }
        
        boolean isRoot() {
            return this.parent == null;
        }
        
        boolean isSonOfRoot() {
            return this.parent != null && this.parent.parent == null;
        }
        
        boolean isViolating() {
            return this.parent != null && this.parent.parent != null && this.compareTo(this.parent) < 0;
        }
        
        boolean isActive() {
            return this.active.isActive;
        }
        
        boolean isPassive() {
            return !this.isActive();
        }
        
        boolean isActiveRoot() {
            return this.parent != null && !this.parent.isActive() && this.isActive();
        }
        
        boolean isPassiveLinkable() {
            return this.isPassive() && (this.child == null || this.child.isPassive());
        }
        
        StrictFibNode disconnectChildren() {
            StrictFibNode ret = this.child;
            this.child = null;
            return ret;
        }
        
        StrictFibNode disconn1PassChild() {
            if (this.child == null) {
                return null;
            }

            StrictFibNode rightmost = this.child.left;
            if (rightmost.isPassive()) {
                this.removeChild(rightmost);
                return rightmost;
            } else {
                return null;
            }
        }
        
        StrictFibNode disconn2PassChild() {
            if (this.child == null)
            {
                return null;
            }

            StrictFibNode first = this.child.left;
            StrictFibNode secnd = first.left;

            if (first.isPassive() && secnd.isPassive() && first != secnd)
            {
                return secnd;
            }
            else
            {
                return null;
            }
        }
        
        int subtreeSize() {
            int size = 1;
            
            StrictFibNode it = this.child;
            
            if (it == null) {
                return size;
            }
            
            do {
                size += it.subtreeSize();
                it = it.right;
            } while (it != this.child);
            
            return size;
        }
    
    }
    
    private class ActiveRecord {
        
        boolean isActive;

        public ActiveRecord(boolean isActive) {
            this.isActive = isActive;
        }
        
    }
    
    private class FixListRecord {
        
        StrictFibNode node;
        FixListRecord left;
        FixListRecord right;
        RankListRecord rank;

        FixListRecord(StrictFibNode node, RankListRecord rank) {
            this.node = node;
            this.rank = rank;
            this.left = this;
            this.right = this;
        }
        
        void addAfter(FixListRecord other) {
            other.left = this;
            other.right = this.right;
            this.right.left = other;
            this.right = other;            
        }
        
        void addBefor(FixListRecord other) {
            other.right = this;
            other.left = this.left;
            this.left.right = other;
            this.left = other;
        }
        
    }
    
    private class RankListRecord {
        
        RankListRecord inc;
        RankListRecord dec;
        FixListRecord loss;
        FixListRecord activeRoots;
        
    }
    
}
