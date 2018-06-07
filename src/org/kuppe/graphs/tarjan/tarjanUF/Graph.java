package tarjanUF;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Graph {

    private final Map<Integer, GraphNode> nodePtrTable;
    private final String name;

    public Graph() {
        this(null);
    }

    public Graph(final String name) {
        this.name = name;
        this.nodePtrTable = new HashMap<Integer, GraphNode>();
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public GraphNode get(final int id) {
        return this.nodePtrTable.get(id);
    }

    void addArc(int nodeId, int arcId) {
        assert this.nodePtrTable.containsKey(nodeId);
        GraphNode graphNode = this.nodePtrTable.get(nodeId);
        graphNode.getArcs().add(arcId);
    }

}
