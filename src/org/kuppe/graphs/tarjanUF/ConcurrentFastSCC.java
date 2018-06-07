package tarjanUF;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConcurrentFastSCC {

    public Set<Set<GraphNode>> searchSCCs(final Graph graph, final UF unionfind) {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        return searchSCCs(graph, unionfind, Integer.getInteger(ConcurrentFastSCC.class.getName() + ".numCores", availableProcessors));
    }

    public Set<Set<GraphNode>> searchSCCs(final Graph graph, final UF unionfind, final int numCores) {
        final ForkJoinPool executor = new ForkJoinPool(numCores);
        final Map<Integer, Integer> workerMap = new ConcurrentHashMap<Integer, Integer>();
        final AtomicInteger workerCount = new AtomicInteger(0);

        final long start = System.nanoTime();

        for (int i = 0; i < graph.N(); i++) {
            // @require: Check if globally visited or not.
                executor.execute(new SCCWorker(graph, workerMap, workerCount, i, unionfind));
        }
        executor.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        executor.shutdown();

        final long duration = System.nanoTime() - start;
        System.out.println("Runtime for algorithm: " + duration);

        final Set<Set<GraphNode>> result = new HashSet<>(0);
        return result;
    }

}
