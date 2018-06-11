package tarjanUF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class GraphNode implements Serializable {

    private List<Integer> arcs;
    private int id;

    public GraphNode(int id) {
        this.id = id;
        arcs = new ArrayList<Integer>();
    }

    public void setArcs(List<Integer> arcs) {
        this.arcs = arcs;
    }

    public List<Integer> getArcs() {
        return this.arcs;
    }

    public boolean hasArcs() {
        if (arcs == null) {
            return false;
        }
        return !this.arcs.isEmpty();
    }

    public int getId() {
        return this.id;
    }

    public Iterator<Integer> POST() {
        assert this.arcs != null;
        return this.arcs.iterator();
    }

}
