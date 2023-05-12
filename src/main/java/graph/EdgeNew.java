package graph;

public class EdgeNew {
    private final double v1;
    private final double v2;
    public EdgeNew(double v1, double v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
    public double v1() { return v1; }
    public double v2() { return v2; }
    public String toString() { return "edge={" + v1 + ", " + v2 + "}"; }
    public boolean equals(EdgeNew edge) { return v1 == edge.v1() && v2 == edge.v2(); }
}
