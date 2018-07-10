package tarjanUF;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class UFNode {

    // Denotes a bitmask of threads currently processing it.
    // Limits number of threads to 64.
    // To overcome implement a concurrent bitset.
    public AtomicLong workerSet;

    public long workerSet() {
        return workerSet.get();
    }

    // Parent in the union find tree.
    private volatile Integer parent;

    public int parent() {
        return parentUpdater.get(this);
    }

    public static final AtomicReferenceFieldUpdater<UFNode, Integer> parentUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, Integer.class, "parent");

    // Id of next element in the list.
    private volatile Integer listNext;

    public int listNext() {
        return listNextUpdater.get(this);
    }

    public static final AtomicReferenceFieldUpdater<UFNode, Integer> listNextUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, Integer.class, "listNext");

    public enum UFStatus {
        /*
         * UFlive: Available for processing.
         * UFlock: Prevent other threads from changing parent.
         * UFdead: The node's maximal SCC is found.
         */
        UFlive, UFlock, UFdead;
    };

    private volatile UFStatus ufStatus;

    public UFStatus ufStatus() {
        return ufStatusUpdater.get(this);
    }

    public static final AtomicReferenceFieldUpdater<UFNode, UFStatus> ufStatusUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, UFStatus.class, "ufStatus");

    public enum ListStatus {
        /*
         * listLive: Node can be modified in the list.
         * listLock: Node is busy in list operations.
         * listTomb: Node has been fully explored.
         */
        listLive, listLock, listTomb;
    };

    private volatile ListStatus listStatus;

    public ListStatus listStatus() {
        return listStatusUpdater.get(this);
    }

    public static final AtomicReferenceFieldUpdater<UFNode, ListStatus> listStatusUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, ListStatus.class, "listStatus");

    public UFNode() {
        this.workerSet = new AtomicLong(0L);
        UFNode.parentUpdater.set(this, 0);
        UFNode.listNextUpdater.set(this, 0);
        UFNode.ufStatusUpdater.set(this, UFStatus.UFlive);
        UFNode.listStatusUpdater.set(this, ListStatus.listLive);
    }
}
