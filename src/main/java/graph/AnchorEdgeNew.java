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
    public void addEdgeCausingCrossing(AnchorEdgeNew anchorEdge) {
        for (AnchorEdgeNew edge : edgesCausingCrossing) {
            if (edge.getParentEdge().equals(anchorEdge.getParentEdge())) return;
        }
        edgesCausingCrossing.add(anchorEdge);
    }
    public void removeEdgeCausingCrossing(AnchorEdgeNew anchorEdge) {
        for (AnchorEdgeNew edge : edgesCausingCrossing) {
            if (anchorEdge.equals(edge)) {
                edgesCausingCrossing.remove(edge);
                break;
            }
        }
    }
    public boolean isEdgeCausingCrossing() { return edgesCausingCrossing.size() > 0; }
    public List<AnchorEdgeNew> getOtherEdgeCausingCrossing() { return edgesCausingCrossing; }
    public Edge getParentEdge() { return parent; }
    public boolean equals(AnchorEdgeNew edge) { return super.equals(edge) && edge.getParentEdge().equals(parent); }
}
