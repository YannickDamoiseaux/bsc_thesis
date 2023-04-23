public class AnchorEdge extends Edge {
    private boolean edgeCausingCrossing;
    public AnchorEdge(int v1, int v2) {
        super(v1, v2);
        this.edgeCausingCrossing = false;
    }

    public void setEdgeCausingCrossing(boolean bool) { this.edgeCausingCrossing = bool; }
    public boolean isEdgeCausingCrossing() { return edgeCausingCrossing; }
}
