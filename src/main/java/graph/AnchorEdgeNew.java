package graph;

import java.util.ArrayList;
import java.util.List;

public class AnchorEdgeNew extends EdgeNew {
    private ArrayList<AnchorEdgeNew> edgesCausingCrossing;
    private Edge parent;
    public AnchorEdgeNew(double v1, double v2, Edge parent) {
        super(v1, v2);
        this.parent = parent;
        this.edgesCausingCrossing = new ArrayList<>();
    }

    public int getNrOfCrossingCausing() { return edgesCausingCrossing.size(); }
    public void addEdgeCausingCrossing(AnchorEdgeNew edge) { edgesCausingCrossing.add(edge); }
    public void removeEdgeCausingCrossing(AnchorEdgeNew edge) {
        edgesCausingCrossing.remove(edge);
    }
    public boolean isEdgeCausingCrossing() { return edgesCausingCrossing.size() > 0; }
    public List<AnchorEdgeNew> getOtherEdgeCausingCrossing() { return edgesCausingCrossing; }
    public Edge getParentEdge() { return parent; }
    public boolean equals(AnchorEdgeNew edge) { return super.equals(edge) && edge.getParentEdge().equals(parent); }
}
