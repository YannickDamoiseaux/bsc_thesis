package solvers.upperbound;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.Utils;

import java.util.*;

public class ExactPruningUB {
    private final Graph graph;
    private Integer[] pointVertexCombinations;
    private Point[] vertexPointCombinations;
    private final int[] verticesToChange;
    private final Point[] pointPartition;
    private final Edge[] colinearEdge = new Edge[2];
    private boolean[] isVertexInPartition;
    private Set<String> set;
    private boolean useHashSet;
    private StringBuilder stringBuilder = new StringBuilder();
    private Random rand = new Random(0);
    private long startTime;
    private final double timeAllowed;

    public ExactPruningUB(Graph graph, Point[] vertexPointCombinations, int[] verticesToChange, Point[] pointPartition, double timeInMillis) {
        this.graph = graph;
        this.vertexPointCombinations = vertexPointCombinations;
        this.verticesToChange = verticesToChange;
        this.pointPartition = pointPartition;
        this.timeAllowed = timeInMillis;
    }

    public Point[] solve() {
        startTime = System.nanoTime();
        useHashSet = graph.getNrOfPoints()-verticesToChange.length > verticesToChange.length/2;
        if (useHashSet) set = new HashSet<>();

        isVertexInPartition = new boolean[graph.getNrOfVertices()];
        for (int i : verticesToChange) {
            isVertexInPartition[i] = true;
        }
        pointVertexCombinations = new Integer[pointPartition.length];
        ArrayList<Point> availablePoints = new ArrayList<>(Arrays.asList(pointPartition));

        for (int i = 0; i < vertexPointCombinations.length; i++) {
            if (vertexPointCombinations[i] == null) {
                int idx = rand.nextInt(availablePoints.size());
                vertexPointCombinations[i] = availablePoints.get(idx);
                availablePoints.remove(idx);
            }
        }

        Arrays.fill(pointVertexCombinations, -1);

        int[] p = new int[pointPartition.length];
        int i, j;
        for(i = 0; i < verticesToChange.length; i++) {
            for (int b = 0; b < pointPartition.length; b++) {
                if (pointPartition[b].equals(vertexPointCombinations[verticesToChange[i]])) {
                    pointVertexCombinations[b] = verticesToChange[i];
                    break;
                }
            }
        }
        for (i = 0; i < pointPartition.length; i++) {
            p[i] = 0;
        }

        int optimalCrossingNumber = calculateNrOfCrossings(Integer.MAX_VALUE);
        Point[] optimalVertexPointCombinations = (optimalCrossingNumber == Integer.MAX_VALUE ? null : vertexPointCombinations.clone());

        if (optimalCrossingNumber == 0) return vertexPointCombinations;

        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (Thread.currentThread().isInterrupted()) {
                return optimalVertexPointCombinations;
            }
            if (p[i] < i) {
                if ((System.nanoTime()-startTime)/1000000.0 >= timeAllowed) return optimalVertexPointCombinations;
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    int crossingNumber = getNrOfCrossings(i, j, optimalCrossingNumber);
                    if (crossingNumber < optimalCrossingNumber) {
                        optimalCrossingNumber = crossingNumber;
                        optimalVertexPointCombinations = vertexPointCombinations.clone();
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

        return optimalVertexPointCombinations;
    }

    private int getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int bestCrossingNumberFound) {
        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx1]] = pointPartition[swappedPointIdx2];
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx2]] = pointPartition[swappedPointIdx1];
        }

        int temp = pointVertexCombinations[swappedPointIdx1];
        pointVertexCombinations[swappedPointIdx1] = pointVertexCombinations[swappedPointIdx2];
        pointVertexCombinations[swappedPointIdx2] = temp;

        if (useHashSet) {
            stringBuilder.setLength(0);
            for (Integer pointVertexCombination : pointVertexCombinations) {
                stringBuilder.append(pointVertexCombination);
            }
            if (set.contains(stringBuilder.toString())) return Integer.MAX_VALUE;
            else {
                set.add(stringBuilder.toString());
            }
        }

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

        return calculateNrOfCrossings(bestCrossingNumberFound);
    }

    private int calculateNrOfCrossings(int bestCrossingNumberFound) {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
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
}
