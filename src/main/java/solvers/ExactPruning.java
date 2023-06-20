package solvers;

import graph.Graph;
import graph.Edge;
import graph.Point;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactPruning extends ExactSolver {
    public ExactPruning(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public ExactPruning(Graph graph) {
        this.graph = graph;
    }
    public ExactPruning() {}

    private Integer[] pointVertexCombinations;
    private Point[] vertexPointCombinations;
    private final Edge[] colinearEdge = new Edge[2];
    private Set<List<Integer>> set;
    private boolean useHashSet;
    private int optimalCrossingNumber;
    private Random rand = new Random(0);

    public double solve() {
        return solve(Integer.MAX_VALUE);
    }

    @Override
    public Solver newEmptyInstance() {
        return new ExactPruning();
    }

    public double solve(int upperbound) {
        useHashSet = graph.getNrOfPoints()-graph.getNrOfVertices() > graph.getNrOfVertices()/2;
        if (useHashSet) set = new HashSet<>();
        pointVertexCombinations = new Integer[graph.getNrOfPoints()];
        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        ArrayList<Integer> indicesToChooseFrom = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfPoints(); i++) {
            indicesToChooseFrom.add(i);
        }
        for (int i = 0; i < vertexPointCombinations.length; i++) {
            int idx = rand.nextInt(indicesToChooseFrom.size());
            vertexPointCombinations[i] = graph.getPoints()[indicesToChooseFrom.get(idx)];
            indicesToChooseFrom.remove(idx);
        }
        Arrays.fill(pointVertexCombinations, -1);
        //System.out.println(Arrays.toString(vertexPointCombinations));
        int[] p = new int[graph.getNrOfPoints()];
        int i, j;
        for (i = 0; i < vertexPointCombinations.length; i++) {
            for (int b = 0; b < graph.getNrOfPoints(); b++) {
                if (graph.getPoints()[b].equals(vertexPointCombinations[i])) {
                    pointVertexCombinations[b] = i;
                    break;
                }
            }
        }
        for (i = 0; i < p.length; i++) {
            p[i] = 0;
        }

        int inital = calculateNrOfCrossings(upperbound);
        optimalCrossingNumber = (inital == Integer.MAX_VALUE ? upperbound : inital);
        if (optimalCrossingNumber == 0) return optimalCrossingNumber;

        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (Thread.currentThread().isInterrupted()) return optimalCrossingNumber;
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    int crossingNumber = getNrOfCrossings(i, j, optimalCrossingNumber);
                    //if (Arrays.equals(vertexPointCombinations, new Point[]{new Point(0,0), new Point(0,2), new Point(2,3), new Point(2,2), new Point(4,3), new Point(3,0), new Point(1,3), new Point(3, 4), new Point(1,1), new Point(2,4)})) System.out.println(crossingNumber);
                    if (crossingNumber < optimalCrossingNumber) {
                        optimalCrossingNumber = crossingNumber;
                        System.out.println("New best: " + optimalCrossingNumber);
                        //System.out.println(Arrays.toString(vertexPointCombinations));
                        if (optimalCrossingNumber == 0) break;
                    }
                }
                p[i]++;
                i = 1;
            } else {
                p[i] = 0;
                i++;
            }
        }

        return optimalCrossingNumber;
    }

    private int getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int bestCrossingNumberFound) {
        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx1]] = graph.getPoints()[swappedPointIdx2];
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx2]] = graph.getPoints()[swappedPointIdx1];
        }
        int temp = pointVertexCombinations[swappedPointIdx1];
        pointVertexCombinations[swappedPointIdx1] = pointVertexCombinations[swappedPointIdx2];
        pointVertexCombinations[swappedPointIdx2] = temp;

        if (colinearEdge[0] != null) {
            if (colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx2]
            || colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx2]
            || colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx2]
            || colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            }
            else return Integer.MAX_VALUE;
        }

        if (useHashSet) {
            if (set.contains(List.of(pointVertexCombinations))) return Integer.MAX_VALUE;
            else set.add(List.of(pointVertexCombinations));
        }

        return calculateNrOfCrossings(bestCrossingNumberFound);
    }

    private int calculateNrOfCrossings(int bestCrossingNumberFound) {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
                //System.out.println(edge1 + ", " + edge2);
                int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                if (crossing == -1) {
                    colinearEdge[0] = edge1;
                    colinearEdge[1] = edge2;
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

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public double getOptimalCrossingNumber() { return optimalCrossingNumber; }

}
