package solvers;

import graph.Graph;

public abstract class Solver {
    protected Graph graph;
    protected long start = System.currentTimeMillis();
    abstract public double solve();
    abstract public Solver newEmptyInstance();
    abstract public double getOptimalCrossingNumber();
    abstract public String getName();
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    public double getExecutionTime() { return System.currentTimeMillis()-start; }
}
