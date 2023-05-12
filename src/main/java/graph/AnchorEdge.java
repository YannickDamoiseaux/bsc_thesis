package graph;

import java.util.ArrayList;
import java.util.List;

public class AnchorEdge extends Edge {
    private ArrayList<AnchorEdge> edgesCausingCrossing;
    private Edge parent;
    public AnchorEdge(int v1, int v2, Edge parent) {
        super(v1, v2);
        this.parent = parent;
        this.edgesCausingCrossing = new ArrayList<>();
    }

    public int getNrOfCrossingCausing() { return edgesCausingCrossing.size(); }
    public void addEdgeCausingCrossing(AnchorEdge anchorEdge) {
        for (AnchorEdge edge : edgesCausingCrossing) {
            if (edge.getParentEdge().equals(anchorEdge.getParentEdge())) return;
        }
        edgesCausingCrossing.add(anchorEdge);
    }
    public void removeEdgeCausingCrossing(AnchorEdge anchorEdge) {
        for (AnchorEdge edge : edgesCausingCrossing) {
            if (anchorEdge.equals(edge)) {
                edgesCausingCrossing.remove(edge);
                break;
            }
        }
    }
    public boolean isEdgeCausingCrossing() { return edgesCausingCrossing.size() > 0; }
    public List<AnchorEdge> getOtherEdgeCausingCrossing() { return edgesCausingCrossing; }
    public Edge getParentEdge() { return parent; }
    public boolean equals(AnchorEdge edge) { return super.equals(edge) && edge.getParentEdge().equals(parent); }
}
