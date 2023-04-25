public class Edge {
    private final int v1;
    private final int v2;
    public Edge(int v1, int v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
    public int v1() { return v1; }
    public int v2() { return v2; }
    public String toString() { return "edge={" + v1 + ", " + v2 + "}"; }
    public boolean equals(Edge edge) { return v1 == edge.v1() && v2 == edge.v2(); }
}
