package graph;

import java.util.ArrayList;
import java.util.List;

public class AnchorEdgeTemp extends Edge {
    private ArrayList<AnchorEdgeTemp> edgesCausingCrossing;
    private Edge parent;
    public AnchorEdgeTemp(int v1, int v2, Edge parent) {
        super(v1, v2);
        this.parent = parent;
        this.edgesCausingCrossing = new ArrayList<>();
    }

    public int getNrOfCrossingCausing() { return edgesCausingCrossing.size(); }
    public void addEdgeCausingCrossing(AnchorEdgeTemp anchorEdgeTemp) {
        for (AnchorEdgeTemp edge : edgesCausingCrossing) {
            if (edge.getParentEdge().equals(anchorEdgeTemp.getParentEdge())) return;
        }
        edgesCausingCrossing.add(anchorEdgeTemp);
    }
    public void removeEdgeCausingCrossing(AnchorEdgeTemp anchorEdgeTemp) {
        for (AnchorEdgeTemp edge : edgesCausingCrossing) {
            if (anchorEdgeTemp.equals(edge)) {
                edgesCausingCrossing.remove(edge);
                break;
            }
        }
    }
    public boolean isEdgeCausingCrossing() { return edgesCausingCrossing.size() > 0; }
    public List<AnchorEdgeTemp> getOtherEdgeCausingCrossing() { return edgesCausingCrossing; }
    public Edge getParentEdge() { return parent; }
    public boolean equals(AnchorEdgeTemp edge) { return super.equals(edge) && edge.getParentEdge().equals(parent); }
}
