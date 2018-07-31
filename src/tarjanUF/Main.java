package tarjanUF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static void readFile(Graph graph, String filename) throws IOException {
        final long start = System.nanoTime();

        final FileInputStream in = new FileInputStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                final String[] split = line.trim().split("\\s+");
                final int nodeId = Integer.parseInt(split[0]);
                final int arcId = Integer.parseInt(split[1]);
                if (graph.hasNode(nodeId)) {
                    graph.addArc(nodeId, arcId);
                } else {
                    graph.addNode(new GraphNode(nodeId));
                    graph.addArc(nodeId, arcId);
                }

                if (!graph.hasNode(arcId)) {
                    graph.addNode(new GraphNode(arcId));
                }
            }
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for input: " + duration);
    }

    public static void readInits(List<Integer> initNodes, String filename) throws IOException {
        final FileInputStream in = new FileInputStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                initNodes.add(Integer.parseInt(line));
            }
        }
    }

    public static void printSCCs(Map<Integer, Set<GraphNode>> sccs) {
        final long start = System.nanoTime();

        for (Set<GraphNode> sgn: sccs.values()) {
            for (GraphNode gn: sgn) {
                System.out.print(gn.getId());
                System.out.print(" ");
            }
            System.out.println();
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for output: " + duration);
    }

    public static void main(String[] args) {
        System.err.println("Processing graph: " + args[0] + " starting with initial nodes from " + args[2] + " with " + args[1] + " threads.");
        System.err.println("Runtimes are in nanoseconds.");
        final long start = System.nanoTime();

        assert args.length == 3;
        final Graph graph = new Graph(args[0]);
        final List<Integer> initNodes = new ArrayList<Integer>();
        try {
            readFile(graph, args[0]);
            readInits(initNodes, args[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        UFNode.workerCount = ConcurrentFastSCC.requiredProcessors(Integer.parseInt(args[1]));
        final UF unionfind = new UF(graph.N() + 1);
        final Map<Integer, Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph, initNodes, unionfind, UFNode.workerCount);

        printSCCs(sccs);

        final long duration = System.nanoTime() - start;
        System.err.println("Total runtime: " + duration);
    }
}
