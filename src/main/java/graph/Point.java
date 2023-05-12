package graph;

public record Point(int x, int y) {
    public String toString() { return "point={" + x + ", " + y + "}"; }
}
