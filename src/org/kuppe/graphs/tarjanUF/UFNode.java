package tarjanUF;

public class UFNode {

    // Denotes a bitmask of threads currently processing it.
    // Limits number of threads to 64.
    volatile long workerSet;

    // Parent in the union find tree.
    volatile int parent;

    // Id of next element in the list.
    volatile int listNext;

    public enum UFStatus {
        /*
         * UFlive: Available for processing.
         * UFlock: Prevent other threads from changing parent.
         * UFdead: The node's maximal SCC is found.
         */
        UFlive, UFlock, UFdead;
    };

    volatile UFStatus ufStatus = UFStatus.UFlive;

    public enum ListStatus {
        /*
         * listLive: Node can be modified in the list.
         * listLock: Node is busy in list operations.
         * listTomb: Node has been fully explored.
         */
        listLive, listLock, listTomb;
    };

    volatile ListStatus listStatus = ListStatus.listLive;

    public UFNode() {
        this.workerSet = 0L;
        this.parent = 0;
        this.listNext = 0;
        this.ufStatus = UFStatus.UFlive;
        this.listStatus = ListStatus.listLive;
    }
}
