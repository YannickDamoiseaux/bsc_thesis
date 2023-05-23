package algorithms;

public class SortObject<V> {
    public final V object;
    public final double sortParameter;
    public SortObject(V object, double sortParameter) {
        this.object = object;
        this.sortParameter = sortParameter;
    }

    public String toString() {
        return "(Node: " + object + " sortParameter: " + sortParameter + ")";
    }
}