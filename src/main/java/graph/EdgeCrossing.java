package graph;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EdgeCrossing extends Edge {
    public List<EdgeCrossing> edgesHavingCrossingWith;
    public EdgeCrossing(Edge edge) {
        super(edge.v1(), edge.v2());
        this.edgesHavingCrossingWith = new ArrayList<>();
    }

    public int getEdgeCausesCrossing() { return edgesHavingCrossingWith.size(); }
    public void addEdgeCausesCrossing(EdgeCrossing edge) { edgesHavingCrossingWith.add(edge); }
    public void removeEdgeCausesCrossing(EdgeCrossing edge) {
        edgesHavingCrossingWith.remove(edge);
    }
    public void emptyEdgeCrossings() {
        for (EdgeCrossing edge : edgesHavingCrossingWith) {
            edge.removeEdgeCausesCrossing(this);
        }
        edgesHavingCrossingWith.clear();
    }
}
