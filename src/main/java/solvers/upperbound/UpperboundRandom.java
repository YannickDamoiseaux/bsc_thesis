package solvers.upperbound;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBIP;
import solvers.Solver;
import solvers.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class UpperboundRandom extends Upperbound {
    private final int nrPointsPerPartition;
    private final Random rand;
    private final boolean useDistanceMetric;

    public UpperboundRandom(String src, int nrPointsPerPartition, boolean useDistanceMetric) throws FileNotFoundException, URISyntaxException {
        super(src);
        this.nrPointsPerPartition = nrPointsPerPartition;
        this.useDistanceMetric = useDistanceMetric;
        this.rand = new Random();
        //System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public UpperboundRandom(int nrPointsPerPartition, boolean useDistanceMetric) {
        this.nrPointsPerPartition = nrPointsPerPartition;
        this.useDistanceMetric = useDistanceMetric;
        this.rand = new Random();
    }

    @Override
    public double solve() {
        Long startTime = System.nanoTime();
        double upperbound = Integer.MAX_VALUE;
        while (!Thread.currentThread().isInterrupted()) {
            List<Point[]> pointPartitions = bisectionPartition(graph.getPoints(), true);
            List<int[]> vertexPartitions = getVertexPartitions(pointPartitions);

            double crossingNumber = super.solve(pointPartitions, vertexPartitions, useDistanceMetric, startTime);
            if (crossingNumber < upperbound) {
                upperbound = crossingNumber;
                //System.out.println("New best: "+ upperbound);
                if (upperbound == 0) return upperbound;
            }
        }

        return upperbound;
    }

    private List<int[]> getVertexPartitions(List<Point[]> pointPartitions) {
        ArrayList<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertices.add(i);
        }

        ArrayList<ArrayList<Integer>> vertexPartitionsTemp = new ArrayList<>();
        for (int i = 0; i < pointPartitions.size(); i++) {
            vertexPartitionsTemp.add(new ArrayList<>());
        }
        outer:
        while (true) {
            for (int i = 0; i < pointPartitions.size(); i++) {
                if (vertices.size() == 0) break outer;
                if (vertexPartitionsTemp.get(i).size() < pointPartitions.get(i).length) {
                    int idx = rand.nextInt(vertices.size());
                    vertexPartitionsTemp.get(i).add(vertices.get(idx));
                    vertices.remove(idx);
                }
            }
        }

        ArrayList<int[]> vertexPartitions = new ArrayList<>();
        for (int i = 0; i < vertexPartitionsTemp.size(); i++) {
            vertexPartitions.add(new int[vertexPartitionsTemp.get(i).size()]);
            for (int j = 0; j < vertexPartitionsTemp.get(i).size(); j++) {
                vertexPartitions.get(i)[j] = vertexPartitionsTemp.get(i).get(j);
            }
        }

        return vertexPartitions;
    }

    private ArrayList<Point[]> bisectionPartition(Point[] points, boolean xSorting) {
        if (points.length <= nrPointsPerPartition) return new ArrayList<>(Collections.singleton(points));

        Point[] sortedPoints = points.clone();
        Arrays.sort(sortedPoints, Comparator.comparingInt(xSorting ? Point::x : Point::y).thenComparingInt(xSorting ? Point::y : Point::x));

        Point[] subset1 = new Point[sortedPoints.length/2];
        System.arraycopy(sortedPoints, 0, subset1, 0, subset1.length);
        Point[] subset2 = new Point[sortedPoints.length-subset1.length];
        System.arraycopy(sortedPoints, subset1.length, subset2, 0, subset2.length);

        ArrayList<Point[]> pointsList = bisectionPartition(subset1, !xSorting);
        pointsList.addAll(bisectionPartition(subset2, !xSorting));
        return pointsList;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + "_" + useDistanceMetric;
    }

    @Override
    public Solver newEmptyInstance() { return new UpperboundRandom(this.nrPointsPerPartition, this.useDistanceMetric); }
}
