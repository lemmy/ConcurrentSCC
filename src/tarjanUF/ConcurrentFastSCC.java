package tarjanUF;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConcurrentFastSCC {

    public Map<Integer, Set<GraphNode>> searchSCCs(final Graph graph, final List<Integer> initNodes, final UF unionfind, final int threads) {
        final int availableProcessors;
        if (threads == -1) {
            availableProcessors = Runtime.getRuntime().availableProcessors();
        } else {
            availableProcessors = threads;
        }
        System.err.println("Using " + availableProcessors + " processesors.");
        return searchHelper(graph, initNodes, unionfind, Integer.getInteger(ConcurrentFastSCC.class.getName() + ".numCores", availableProcessors));
    }

    public Map<Integer, Set<GraphNode>> searchHelper(final Graph graph, final List<Integer> initNodes, final UF unionfind, final int numCores) {
        final ExecutorService executor = Executors.newFixedThreadPool(numCores);
        final Map<Long, Integer> workerMap = new ConcurrentHashMap<Long, Integer>();
        final AtomicInteger workerCount = new AtomicInteger(0);

        final long start = System.nanoTime();

        for (int i = 0; i < initNodes.size(); i++) {
            int nodeId = initNodes.get(i);
            if (unionfind.visited.get(nodeId) == false) {
                executor.execute(new SCCWorker(graph, workerMap, workerCount, nodeId, unionfind));
            }
        }
        if (initNodes.size() < numCores) {
            int leftCores = numCores - initNodes.size();
            for (int i = 0; i < leftCores; i++) {
                int nodeId = initNodes.get(i % initNodes.size());
                if (unionfind.visited.get(nodeId) == false) {
                    executor.execute(new SCCWorker(graph, workerMap, workerCount, nodeId, unionfind));
                }
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for algorithm: " + duration);

        final Map<Integer, Set<GraphNode>> result = new HashMap<Integer, Set<GraphNode>>();
        for (int i = 0; i < graph.N(); i++) {
            int root = unionfind.find(i + 1) - 1;
            if (!result.containsKey(root)) {
                result.put(root, new HashSet<GraphNode>());
            }
            result.get(root).add(graph.get(i));
        }
        return result;
    }

}
