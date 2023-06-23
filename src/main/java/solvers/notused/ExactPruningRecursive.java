package solvers.notused;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBLP;
import solvers.ExactSolver;
import solvers.Solver;
import solvers.Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactPruningRecursive extends ExactSolver {
    public ExactPruningRecursive(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBLP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public ExactPruningRecursive(Graph graph) {
        this.graph = graph;
    }
    public ExactPruningRecursive() {}

    public double solve() {
        return solve(Integer.MAX_VALUE);
    }

    @Override
    public Solver newEmptyInstance() {
        return new ExactPruningRecursive();
    }

    private int goBackToIdx = Integer.MAX_VALUE;
    private Random rand = new Random(0);
    private int crossingNumber = Integer.MAX_VALUE;

    public double solve(int upperbound) {
        Point[] vertexPointCombinations = new Point[graph.getNrOfVertices()];
        ArrayList<Integer> indicesToChooseFrom = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfPoints(); i++) {
            indicesToChooseFrom.add(i);
        }
        Point[] points = new Point[graph.getNrOfPoints()];
        for (int i = 0; i < graph.getNrOfPoints(); i++) {
            int idx = rand.nextInt(indicesToChooseFrom.size());
            points[i] = graph.getPoints()[indicesToChooseFrom.get(idx)];
            indicesToChooseFrom.remove(idx);
        }
        //System.out.println(Arrays.toString(points));
        return recursive(vertexPointCombinations, points, 0);
    }


    private int recursive(Point[] vertexPointCombinations, Point[] points, int idx) {
        if (idx == vertexPointCombinations.length) return calculateNrOfCrossings(vertexPointCombinations, crossingNumber);
        else {
            outer:
            for (int i = 0; i < points.length; i++) {
                if (Thread.currentThread().isInterrupted()) return crossingNumber;
                Point point = points[i];
                if (point != null) {
                    vertexPointCombinations[idx] = point;
                    points[i] = null;
                    int result = recursive(vertexPointCombinations, points, idx + 1);
                    points[i] = point;
                    if (goBackToIdx < idx) return Integer.MAX_VALUE;
                    else if (goBackToIdx == idx) goBackToIdx = Integer.MAX_VALUE;
                    if (result < crossingNumber) {
                        crossingNumber = result;
                        System.out.println("New best: " + crossingNumber);
                        if (crossingNumber == 0) return crossingNumber;
                    }
                }
            }
        }
        return crossingNumber;
    }

    private int calculateNrOfCrossings(Point[] vertexPointCombinations, int bestCrossingNumberFound) {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
                int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                if (crossing == -1) {
                    goBackToIdx = Collections.max(List.of(edge1.v1(), edge1.v2(), edge2.v1(), edge2.v2()));
                    return Integer.MAX_VALUE;
                }
                else if (crossing == 1) {
                    crossingNumber++;
                    if (crossingNumber >= bestCrossingNumberFound) return bestCrossingNumberFound;
                }
            }
        }
        return crossingNumber;
    }

    public double getOptimalCrossingNumber() { return crossingNumber; }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
