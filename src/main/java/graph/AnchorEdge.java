package graph;

import java.util.ArrayList;
import java.util.List;

public class AnchorEdge extends EdgeDouble {
    private ArrayList<AnchorEdge> edgesCausingCrossing;
    private Edge parent;
    public AnchorEdge(double v1, double v2, Edge parent) {
        super(v1, v2);
        this.parent = parent;
        this.edgesCausingCrossing = new ArrayList<>();
    }

    public int getNrOfCrossingCausing() { return edgesCausingCrossing.size(); }
    public void addEdgeCausingCrossing(AnchorEdge edge) { edgesCausingCrossing.add(edge); }
    public void removeEdgeCausingCrossing(AnchorEdge edge) {
        edgesCausingCrossing.remove(edge);
    }
    public boolean isEdgeCausingCrossing() { return edgesCausingCrossing.size() > 0; }
    public List<AnchorEdge> getOtherEdgeCausingCrossing() { return edgesCausingCrossing; }
    public Edge getParentEdge() { return parent; }
    public boolean equals(AnchorEdge edge) { return super.equals(edge) && edge.getParentEdge().equals(parent); }
}
