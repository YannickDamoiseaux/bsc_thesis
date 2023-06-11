package solvers;

public abstract class UpperBoundSolver extends Solver {
    protected int nrVerticesPerPartition;
    protected int nrPartitions;

    public void setNrVerticesPerPartition(int nrVerticesPerPartition) { this.nrVerticesPerPartition = nrVerticesPerPartition; }
}
