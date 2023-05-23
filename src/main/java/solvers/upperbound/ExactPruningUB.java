package solvers.upperbound;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.Utils;

import java.util.Arrays;

public class ExactPruningUB {
    private final Graph graph;
    private int[] pointVertexCombinations;
    private Point[] vertexPointCombinations;
    private final int[] verticesToChange;
    private final Point[] pointPartition;
    private final Edge[] colinearEdge = new Edge[2];

    public ExactPruningUB(Graph graph, Point[] vertexPointCombinations, int[] verticesToChange, Point[] pointPartition) {
        this.graph = graph;
        this.vertexPointCombinations = vertexPointCombinations;
        this.verticesToChange = verticesToChange;
        this.pointPartition = pointPartition;
    }

    public Point[] solve() {
        pointVertexCombinations = new int[pointPartition.length];
        int idx = 0;
        for (int i = 0; i < vertexPointCombinations.length; i++) {
            if (vertexPointCombinations[i] == null) vertexPointCombinations[i] = pointPartition[idx++];
        }

        int[] p = new int[pointPartition.length];
        int i, j;
        for(i = 0; i < verticesToChange.length; i++) {
            pointVertexCombinations[i] = i;
            p[i] = 0;
        }
        for (i = verticesToChange.length; i < pointPartition.length; i++) {
            pointVertexCombinations[i] = -1;
            p[i] = 0;
        }

        System.out.println("Intial v/p combinations filled " + Arrays.toString(vertexPointCombinations));
        int optimalCrossingNumber = calculateNrOfCrossings(Integer.MAX_VALUE);
        Point[] optimalVertexPointCombinations = vertexPointCombinations.clone();

        //System.out.println("Initial: " + optimalCrossingNumber);
        if (optimalCrossingNumber == 0) return vertexPointCombinations;
        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    int crossingNumber = getNrOfCrossings(i, j, optimalCrossingNumber);
                    if (crossingNumber < optimalCrossingNumber) {
                        optimalCrossingNumber = crossingNumber;
                        optimalVertexPointCombinations = vertexPointCombinations.clone();
                        //System.out.println("New best: " + optimalCrossingNumber);
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

        return optimalVertexPointCombinations;
    }

    private int getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int bestCrossingNumberFound) {
        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            vertexPointCombinations[verticesToChange[pointVertexCombinations[swappedPointIdx1]]] = pointPartition[swappedPointIdx2];
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            vertexPointCombinations[verticesToChange[pointVertexCombinations[swappedPointIdx2]]] = pointPartition[swappedPointIdx1];
        }
        int temp = pointVertexCombinations[swappedPointIdx1];
        pointVertexCombinations[swappedPointIdx1] = pointVertexCombinations[swappedPointIdx2];
        pointVertexCombinations[swappedPointIdx2] = temp;

        System.out.println(swappedPointIdx1 + ", " + swappedPointIdx2 + ", " + bestCrossingNumberFound + ", " + Arrays.deepToString(vertexPointCombinations) + ", " + Arrays.toString(pointVertexCombinations));

        if (colinearEdge[0] != null) {
            if (colinearEdge[0].v1() == verticesToChange[pointVertexCombinations[swappedPointIdx1]] || colinearEdge[0].v1() == verticesToChange[pointVertexCombinations[swappedPointIdx2]]
                    || colinearEdge[0].v2() == verticesToChange[pointVertexCombinations[swappedPointIdx1]] || colinearEdge[0].v2() == verticesToChange[pointVertexCombinations[swappedPointIdx2]]
                    || colinearEdge[1].v1() == verticesToChange[pointVertexCombinations[swappedPointIdx1]] || colinearEdge[1].v1() == verticesToChange[pointVertexCombinations[swappedPointIdx2]]
                    || colinearEdge[1].v2() == verticesToChange[pointVertexCombinations[swappedPointIdx1]] || colinearEdge[1].v2() == verticesToChange[pointVertexCombinations[swappedPointIdx2]]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            }
            else return Integer.MAX_VALUE;
        }

        return calculateNrOfCrossings(bestCrossingNumberFound);
    }

    private int calculateNrOfCrossings(int bestCrossingNumberFound) {
        int crossingNumber = 0;

        System.out.println("Calculating nr crossings for " + Arrays.toString(vertexPointCombinations));
        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
                int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                //System.out.println(edge1 + ", " + edge2 + ", " + crossing);
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
