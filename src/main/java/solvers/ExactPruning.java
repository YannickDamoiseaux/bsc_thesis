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

    private Point[] vertexPointCombinations;
    private boolean[] vertexHasAtleastOneCrossing;

    public double solve() {
        vertexHasAtleastOneCrossing = new boolean[graph.getNrOfVertices()];

        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertexPointCombinations[i] = graph.getPoints()[i];
        }

        int[] p = new int[graph.getNrOfPoints()];
        int[] a = new int[graph.getNrOfPoints()];

        int i, j, tmp;
        for(i = 0; i < graph.getNrOfVertices(); i++) {
            a[i] = i + 1;
            p[i] = 0;
        }
        for (i = graph.getNrOfVertices(); i < graph.getNrOfPoints(); i++) {
            a[i] = -1;
            p[i] = 0;
        }

        int optimalCrossingNumber = calculateNrOfCrossings(Integer.MAX_VALUE);

        System.out.println("Initial: " + optimalCrossingNumber);
        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < a.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(a[i] == -1 && a[j] == -1)) {
                    int crossingNumber = getNrOfCrossings(i, j, optimalCrossingNumber);
                    if (crossingNumber < optimalCrossingNumber) {
                        optimalCrossingNumber = crossingNumber;
                        System.out.println("New best: " + optimalCrossingNumber);
                        //System.out.println(Arrays.toString(vertexPointCombinations));
                        if (optimalCrossingNumber == 0) break;
                    }
                    tmp = a[j];
                    a[j] = a[i];
                    a[i] = tmp;
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

    private int getNrOfCrossings(int swappedVertex1, int swappedVertex2, int bestCrossingNumberFound) {
        Point temp = vertexPointCombinations[swappedVertex1];
        vertexPointCombinations[swappedVertex1] = vertexPointCombinations[swappedVertex2];
        vertexPointCombinations[swappedVertex2] = temp;

        if (!vertexHasAtleastOneCrossing[swappedVertex1] && !vertexHasAtleastOneCrossing[swappedVertex2]) {
            return bestCrossingNumberFound;
        }
        vertexHasAtleastOneCrossing[swappedVertex1] = false;
        vertexHasAtleastOneCrossing[swappedVertex2] = false;

        return calculateNrOfCrossings(bestCrossingNumberFound);
    }

    private int calculateNrOfCrossings(int bestCrossingNumberFound) {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
                if (Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y())) {
                    crossingNumber++;
                    vertexHasAtleastOneCrossing[edge1.v1()] = true;
                    vertexHasAtleastOneCrossing[edge1.v2()] = true;
                    vertexHasAtleastOneCrossing[edge2.v1()] = true;
                    vertexHasAtleastOneCrossing[edge2.v2()] = true;
                    if (crossingNumber >= bestCrossingNumberFound) return bestCrossingNumberFound;
                }
            }
        }
        return crossingNumber;
    }
}
