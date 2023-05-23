package solvers;

import graph.Graph;
import graph.Edge;
import graph.Point;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactPruning implements Solver {
    private final Graph graph;

    public ExactPruning(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public ExactPruning(Graph graph) {
        this.graph = graph;
    }

    private int[] pointVertexCombinations;
    private Point[] vertexPointCombinations;
    private final Edge[] colinearEdge = new Edge[2];
    int count = 0;
    int count_tot = 0;

    public double solve() {
        pointVertexCombinations = new int[graph.getNrOfPoints()];
        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < vertexPointCombinations.length; i++) {
            vertexPointCombinations[i] = graph.getPoints()[i];
        }

        int[] p = new int[graph.getNrOfPoints()];
        int i, j;
        for(i = 0; i < graph.getNrOfVertices(); i++) {
            pointVertexCombinations[i] = i;
            p[i] = 0;
        }
        for (i = graph.getNrOfVertices(); i < graph.getNrOfPoints(); i++) {
            pointVertexCombinations[i] = -1;
            p[i] = 0;
        }

        int optimalCrossingNumber = calculateNrOfCrossings(Integer.MAX_VALUE);

        System.out.println("Initial: " + optimalCrossingNumber);
        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    int crossingNumber = getNrOfCrossings(i, j, optimalCrossingNumber);
                    if (crossingNumber < optimalCrossingNumber) {
                        optimalCrossingNumber = crossingNumber;
                        System.out.println("New best: " + optimalCrossingNumber);
                        //System.out.println(Arrays.toString(pointVertexCombinations));
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
        System.out.println((double)count/count_tot);
        return optimalCrossingNumber;
    }

    private int getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int bestCrossingNumberFound) {
        count_tot++;
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
                    count++;
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
