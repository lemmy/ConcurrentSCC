package tarjanUF;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javafx.util.Pair;

import tarjanUF.UF.PickStatus;
import tarjanUF.UF.ClaimStatus;

public class SCCWorker implements Runnable {

    private final Graph graph;
    private final Map<Long, Integer> workerMap;
    private final AtomicInteger workerCount;
    private int workerId;
    private int nodeId;
    private UF unionfind;
    private Stack<Integer> recursionStack;
    private Stack<Integer> rootStack;

    public SCCWorker(final Graph graph,
                     final Map<Long, Integer> workerMap,
                     final AtomicInteger workerCount,
                     final int nodeId,
                     UF unionfind) {
        this.graph = graph;
        this.workerMap = workerMap;
        this.workerCount = workerCount;
        this.nodeId = nodeId;
        this.unionfind = unionfind;
        this.recursionStack = new Stack<Integer>();
        this.rootStack = new Stack<Integer>();
    }

    @Override
    public void run() {
        if (workerMap.containsKey(Thread.currentThread().getId())) {
            this.workerId = workerMap.get(Thread.currentThread().getId());
        } else {
            this.workerId = workerCount.incrementAndGet();
            workerMap.put(Thread.currentThread().getId(), this.workerId);
        }
        unionfind.makeClaim(nodeId + 1, workerId);

        PickStatus picked;
        int v, vp, w, ei, root;
        int random_ei;
        ClaimStatus claimed;
        boolean backtrack = false;

        v = this.nodeId;

        List<Integer> arcs = null;
        START: while (true) {
            if (!backtrack) {
                rootStack.push(v);
            }

            LOOP: while (true) {
                if (!backtrack) {
                    if (!recursionStack.empty() && unionfind.sameSet(recursionStack.peek() + 1, v + 1)) {
                        break;
                    }

                    Pair<PickStatus, Integer> p = unionfind.pickFromList(v + 1);
                    picked = p.getKey();
                    if (picked != PickStatus.pickSuccess) {
                        break;
                    }
                    vp = p.getValue() - 1;
                    ei = 0;
                } else {
                    v = recursionStack.pop();
                    ei = recursionStack.pop() + 1;
                    vp = recursionStack.pop();
                    backtrack = false;
                    if (unionfind.isDead(v + 1)) {
                        unionfind.removeFromList(vp + 1);
                        continue LOOP;
                    }
                }

                arcs = graph.get(vp).getArcs();
                for (; ei < arcs.size(); ei++) {
                    random_ei = (ei + workerId) % arcs.size();
                    w = arcs.get(random_ei);
                    if (w == vp) {
                        continue;
                    }
                    claimed = unionfind.makeClaim(w + 1, workerId);

                    if (claimed == ClaimStatus.claimDead) {
                        continue;
                    } else if (claimed == ClaimStatus.claimSuccess) {
                        recursionStack.push(vp);
                        recursionStack.push(ei);
                        recursionStack.push(v);
                        v = w;
                        continue START;
                    } else {
                        while (!unionfind.sameSet(w + 1, v + 1)) {
                            root = rootStack.pop();
                            unionfind.unite(rootStack.peek() + 1, root + 1);
                        }
                    }
                }

                unionfind.removeFromList(vp + 1);
            }

            if (rootStack.peek() == v) {
                rootStack.pop();
            }
            if (!recursionStack.empty()) {
                backtrack = true;
            } else {
                break;
            }
        }
    }

}
