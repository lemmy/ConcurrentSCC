package tarjanUF;

import java.util.ArrayList;
import java.util.List;

import tarjanUF.UFNode.ListStatus;
import tarjanUF.UFNode.UFStatus;

public class UF {

    private List<UFNode> list;
    public final List<Boolean> visited;

    // ClaimStatus is used to denote the return value of `makeClaim`
    // where a worker tries to claim rights on a node.
    // It can take the following values:
    // 1. claimSuccess:
    //      Denotes that the node is not dead and
    //      worker making claim was not present in node's workerSet.
    // 2. claimFound:
    //      Denotes that the node is not dead and
    //      worker making clai is already present in node's workerSet.
    // 3. claimDead:
    //      Denotes that the node is already dead.
    //      Meaning that maximal SCC in which this node is present has been discovered.
    public enum ClaimStatus {
        claimSuccess, claimFound, claimDead;
    };

    // PickStatus is used to check status of cyclic list.
    // This is helpful in knowing whether list contains elements
    // which are all dead. Values taken are:
    // 1. pickSuccess
    // 2. pickDead
    public enum PickStatus {
        pickSuccess, pickDead;
    };

    // Constructor.
    // Initializes a list of UFNodes required.
    // Also maintains a boolean list to know which nodes to avoid for DFS root.
    public UF(int n) {
        this.list = new ArrayList<UFNode>(n);
        this.visited = new ArrayList<Boolean>(n);
        for (int i = 0; i < n; i++) {
            this.visited.add(false);
            this.list.add(new UFNode());
        }
    }

    /********* Union find Operations ****************/

    // find is used to find the root of the union find tree
    // in which the node belongs. It uses path compression
    // as an optimization technique.
    public int find(int nodeId) {
        UFNode node = this.list.get(nodeId);
        int parent = node.parent();

        // The node is itself a root in the union find tree.
        if (parent == 0) {
            return nodeId;
        }

        int root = this.find(parent);
        // Compress the path from the node to root of the tree atomically.
        if (root != parent) {
            UFNode.parentUpdater.set(node, root);
        }
        return root;
    }

    // sameSet checks whether 'node a' and 'node b' are in the same union find tree.
    public boolean sameSet(int a, int b) {
        // If they are equal they are in the same UF tree.
        if (a == b)
            return true;

        // Find the root of b's tree.
        int rb = this.find(b);
        // Assume that a == root in a's tree.

        // If the roots of the two trees are equal then they are in the same tree.
        if (a == rb) {
            return true;
        }

        // We are taking higher indexed node as root during linking.
        // Since rb was already a root and a is a higher index then if the parent
        // for rb has not changed/ rb is still a root then they cannot be in the sameset.
        if (rb < a) {
            if (this.list.get(rb).parent() == 0) {
                return false;
            }
        }

        // We will arrive here because of the following situations:
        // 1. a < rb: Again since higher index is a root
        //      we cannot have them in the same tree as rb is also a root not equal to a.
        // 2. rb < a and rb's parent was changed.
        //      If rb's parent was changed for a and b to be in the same tree a's parent should also change.
        //      This is not the case if a's parent in null/0.
        if (this.list.get(a).parent() == 0) {
            return false;
        }

        // Now we can recurse be making our assumption to be true.
        return this.sameSet(this.find(a), rb);
    }

    //  unite tries to unite the nodes a and b until they are in the same tree.
    //  It also merges the two cyclic linked list in which a and b belong by the following
    //  O(1) algorithm:
    //  Consider the following two lists:
    //  ......| some node | -> a -> na -> | some node |...... (cyclic list)
    //  ......| some node | -> b -> nb -> | some node |...... (cyclic list)
    //  They can be merged like this:
    //  ......| some node | -> a  na -> | some node |......
    //                         |  ^
    //                         |  |
    //                          \/
    //                          /\
    //                         |  |
    //                         |  v
    //  ......| some node | -> b  nb -> | some node |......
    //           (A single merged cyclic list)
    public void unite(int a, int b) {
        // Some terminologies:
        // r_ - root of union find tree of _
        // n_ - next element in the list of _
        // l_ - first node in the list of _ that is listLive.
        //      returns -1 if the entire list is dead.
        int ra, rb, la, lb, na, nb;
        int Q, R;
        ConcurrentBitSet workerQ, workerR;

        while (true) {
            // Find roots of the union tree.
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

            // Else try to obtain a lock on the node Q, that is whose parent is to be set.
            if (!this.lockUF(Q)) {
                continue;
            }
            break;
        }
        // Now we have a lock on Q. We need to unlock Q before returning from function.

        // Obtain a lock on a's list.
        la = this.lockList(a);
        if (la == -1) {
            this.unlockUF(Q);
            return;
        }

        // Obtain a lock on b's list.
        lb = this.lockList(b);
        // Both list and Q needs to be unlocked.
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

        // Merge the two lists in O(1) as described in the ASCII art above.
        UFNode.listNextUpdater.set(this.list.get(la), nb);
        UFNode.listNextUpdater.set(this.list.get(lb), na);

        UFNode.parentUpdater.set(this.list.get(Q), R);

        // We also need to merge the worker sets.
        workerQ = this.list.get(Q).workerSet;
        workerR = this.list.get(R).workerSet;

        // An iterative version to "or" the two worker sets in case of race conditions.
        if (!ConcurrentBitSet.equals(ConcurrentBitSet.getOr(workerQ, workerR), workerR)) {
            this.list.get(R).workerSet.or(workerQ);
            while (this.list.get(R).parent() != 0) {
                R = this.find(R);
                this.list.get(R).workerSet.or(workerQ);
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
        ConcurrentBitSet workerId = new ConcurrentBitSet(UFNode.workerCount);
        workerId.set(worker - 1, true);
        int rootId = this.find(nodeId);
        UFNode root = this.list.get(rootId);

        if (root.ufStatus() == UFStatus.UFdead) {
            return ClaimStatus.claimDead;
        }

        if (!ConcurrentBitSet.getAnd(root.workerSet, workerId).isEmpty()) {
            return ClaimStatus.claimFound;
        }

        root.workerSet.or(workerId);
        while (root.parent() != 0) {
            root = this.list.get(this.find(rootId));
            root.workerSet.or(workerId);
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
