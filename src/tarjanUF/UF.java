package tarjanUF;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

import tarjanUF.UFNode.ListStatus;
import tarjanUF.UFNode.UFStatus;

public class UF {

    private List<UFNode> list;
    public final List<Boolean> visited;

    public enum ClaimStatus {
        /*
         * claimSuccess: not dead and not yet visited its SCC.
         * claimFound: not dead and visited its SCC before.
         * claimDead: dead => SCC was found.
         */
        claimSuccess, claimFound, claimDead;
    };

    public enum PickStatus {
        // Used in locking of cyclic list.
        pickSuccess, pickDead;
    };

    public UF(int n) {
        this.list = new ArrayList<UFNode>(n);
        this.visited = new ArrayList<Boolean>(n);
        for (int i = 0; i < n; i++) {
            this.visited.add(false);
            this.list.add(new UFNode());
        }
    }

    /********* Union find Operations ****************/

    public int find(int nodeId) {
        UFNode node = this.list.get(nodeId);
        int parent = node.parent();
        if (parent == 0) {
            return nodeId;
        }

        int root = this.find(parent);
        if (root != parent) {
            UFNode.parentUpdater.set(node, root);
        }
        return root;
    }

    public boolean sameSet(int a, int b) {
        if (a == b)
            return true;
        int rb = this.find(b);

        // Assuming a == find(a)
        if (a == rb) {
            return true;
        }

        if (rb < a) {
            if (this.list.get(rb).parent() == 0) {
                return false;
            }
        }

        if (this.list.get(a).parent() == 0) {
            return false;
        }

        return this.sameSet(this.find(a), rb);
    }

    // Unite the sets of a and b. Also merges the cyclic lists together.
    public void unite(int a, int b) {
        int ra, rb, la, lb, na, nb;
        int Q, R;
        long workerQ, workerR;

        while (true) {
            ra = this.find(a);
            rb = this.find(b);

            // No need to unite.
            if (ra == rb) {
                return;
            }

            // Take highest index node as a root.
            if (ra < rb) {
                R = rb;
                Q = ra;
            } else {
                R = ra;
                Q = rb;
            }

            if (!this.lockUF(Q)) {
                continue;
            }
            break;
        }

        la = this.lockList(a);
        if (la == -1) {
            this.unlockUF(Q);
            return;
        }

        lb = this.lockList(b);
        if (lb == -1) {
            this.unlockList(la);
            this.unlockUF(Q);
            return;
        }

        na = this.list.get(la).listNext();
        nb = this.list.get(lb).listNext();

        // Handle 1 element sets.
        if (na == 0) {
            na = la;
        }
        if (nb == 0) {
            nb = lb;
        }

        // Merge the two lists in O(1).
        UFNode.listNextUpdater.set(this.list.get(la), nb);
        UFNode.listNextUpdater.set(this.list.get(lb), na);

        UFNode.parentUpdater.set(this.list.get(Q), R);

        // Merge the worker sets.
        workerQ = this.list.get(Q).workerSet();
        workerR = this.list.get(R).workerSet();

        if ((workerQ | workerR) != workerR) {
            this.list.get(R).workerSet.accumulateAndGet(workerQ, (x, y) -> x | y);
            while (this.list.get(R).parent() != 0) {
                R = this.find(R);
                this.list.get(R).workerSet.accumulateAndGet(workerQ, (x, y) -> x | y);
            }
        }

        // Remove locks from everywhere.
        this.unlockList(la);
        this.unlockList(lb);
        this.unlockUF(Q);

        return;
    }

    /*************** Cyclic List Operations *****************/

    public boolean inList(int a) {
        return (this.list.get(a).listStatus() != ListStatus.listTomb);
    }

    public Pair<PickStatus, Integer> pickFromList(int state) {
        int a, b, c;
        int ret;
        ListStatus statusA, statusB;
        a = state;

        while(true) {
            // Loop until state of `a` is not locked.
            while (true) {
                statusA = this.list.get(a).listStatus();

                if (statusA == ListStatus.listLive) {
                    return (new Pair<PickStatus, Integer>(PickStatus.pickSuccess, a));
                } else if (statusA == ListStatus.listTomb) {
                    break;
                }
            }

            b = this.list.get(a).listNext();
            if (a == b || b == 0) {
                markDead(a);
                return (new Pair<PickStatus, Integer>(PickStatus.pickDead, -1));
            }

            // Loop until state of `b` is not locked.
            while (true) {
                statusB = this.list.get(b).listStatus();

                if (statusB == ListStatus.listLive) {
                    return (new Pair<PickStatus, Integer>(PickStatus.pickSuccess, b));
                } else if (statusB == ListStatus.listTomb) {
                    break;
                }
            }

            c = this.list.get(b).listNext();

            if (this.list.get(a).listNext() == b) {
                // Shorten the list.
                UFNode.listNextUpdater.set(this.list.get(a), c);
            }

            a = c;
        }
    }

    public boolean removeFromList(int a) {
        ListStatus statusA;

        while (true) {
            statusA = this.list.get(a).listStatus();
            if (statusA == ListStatus.listLive) {
                if (UFNode.listStatusUpdater.compareAndSet(this.list.get(a), ListStatus.listLive, ListStatus.listTomb)) {
                    this.visited.set(a - 1, true);
                    return true;
                }
            } else if (statusA == ListStatus.listTomb) {
                return false;
            }
        }
    }

    /*************** Obtain the colour of node *************/

    public ClaimStatus makeClaim(int nodeId, int worker) {
        long workerId = 1L << ((long) worker);
        int rootId = this.find(nodeId);
        UFNode root = this.list.get(rootId);

        if (root.ufStatus() == UFStatus.UFdead) {
            return ClaimStatus.claimDead;
        }

        if ((root.workerSet() & workerId) != 0L) {
            return ClaimStatus.claimFound;
        }

        root.workerSet.accumulateAndGet(workerId, (x, y) -> x | y);
        while (root.parent() != 0) {
            root = this.list.get(this.find(rootId));
            root.workerSet.accumulateAndGet(workerId, (x, y) -> x | y);
        }
        return ClaimStatus.claimSuccess;
    }

    /************** Check whether(or Mark) node is(or as) dead **************/

    public boolean isDead(int a) {
        int ra = this.find(a);
        return (this.list.get(ra).ufStatus() == UFStatus.UFdead);
    }

    public boolean markDead(int a) {
        boolean result = false;
        int ra = this.find(a);
        UFStatus stat = this.list.get(ra).ufStatus();

        while (stat != UFStatus.UFdead) {
            if (stat == UFStatus.UFlive) {
                result = UFNode.ufStatusUpdater.compareAndSet(this.list.get(ra), UFStatus.UFlive, UFStatus.UFdead);
            }
            stat = this.list.get(ra).ufStatus();
        }
        return result;
    }

    /************** Locking Operations ***************/

    public boolean lockUF(int a) {
        if (this.list.get(a).ufStatus() == UFStatus.UFlive) {
            if (UFNode.ufStatusUpdater.compareAndSet(this.list.get(a), UFStatus.UFlive, UFStatus.UFlock)) {
                if (this.list.get(a).parent() == 0) {
                    return true;
                }

                UFNode.ufStatusUpdater.set(this.list.get(a), UFStatus.UFlive);
            }
        }
        return false;
    }

    public void unlockUF(int a) {
        UFNode.ufStatusUpdater.set(this.list.get(a), UFStatus.UFlive);
    }

    public int lockList(int a) {
        PickStatus picked;
        int la;

        while (true) {
            Pair<PickStatus, Integer> p = pickFromList(a);
            picked = p.getKey();
            la = p.getValue();
            if (picked == PickStatus.pickDead) {
                return -1;
            }
            if (UFNode.listStatusUpdater.compareAndSet(this.list.get(la), ListStatus.listLive, ListStatus.listLock)) {
                return la;
            }
        }
    }

    public void unlockList(int la) {
        UFNode.listStatusUpdater.set(this.list.get(la), ListStatus.listLive);
    }

}
