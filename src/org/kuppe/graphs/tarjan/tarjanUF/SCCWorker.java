package tarjanUF;

import java.util.concurrent.ExecutorService;
import java.util.List;
import java.util.Stack;
import javafx.util.Pair;

import tarjanUF.UF.PickStatus;
import tarjanUF.UF.ClaimStatus;

public class SCCWorker implements Runnable {

    private final ExecutorService executor;
    private final Graph graph;
    private final int workerId;
    private int nodeId;
    private UF unionfind;
    private Stack<Integer> recursionStack;
    private Stack<Integer> rootStack;

    public SCCWorker(final ExecutorService executor,
                     final Graph graph,
                     final int workerId,
                     final int nodeId,
                     UF unionfind,
                     Stack<Integer> recursionStack,
                     Stack<Integer> rootStack) {
        this.executor = executor;
        this.graph = graph;
        this.workerId = workerId;
        this.nodeId = nodeId;
        this.unionfind = unionfind;
        this.recursionStack = recursionStack;
        this.rootStack = rootStack;
    }

    @Override
    public void run() {
        PickStatus picked;
        int v, vp, w, ei, root;
        ClaimStatus claimed;
        boolean backtrack = false;

        v = this.nodeId;

        START: while (true) {
            if (!backtrack)
                rootStack.push(v);

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
                // @require: Random Order
                List<Integer> arcs = graph.get(vp).getArcs();
                for (; ei < arcs.size(); ei++) {
                    w = arcs.get(ei);
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
                        while (unionfind.sameSet(w + 1, v + 1)) {
                            root = rootStack.pop();
                            unionfind.unite(rootStack.peek() + 1, root + 1);
                        }
                    }
                }

                unionfind.removeFromList(vp + 1);
            }

            break;
        }

        if (rootStack.peek() == v) {
            rootStack.pop();
        }
        if (!recursionStack.empty()) {
            backtrack = true;
        }

    }
}
