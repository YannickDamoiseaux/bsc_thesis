import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactPruningNew implements Solver {
    private final Graph graph;

    public ExactPruningNew(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    private Point[] vertexPointCombinations;
    private List<EdgeCrossing>[] edgesPerVertex;
    private EdgeCrossing[] edges;

    public double solve() {
        edgesPerVertex = new List[graph.getNrOfVertices()];
        edges = new EdgeCrossing[graph.getNrOfEdges()];

        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertexPointCombinations[i] = graph.getPoints()[i];
            edgesPerVertex[i] = new ArrayList<>();
        }
        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            EdgeCrossing edge = new EdgeCrossing(graph.getEdges()[i]);
            edgesPerVertex[edge.v1()].add(edge);
            edgesPerVertex[edge.v2()].add(edge);
            edges[i] = edge;
        }


        int[] p = new int[graph.getNrOfVertices()];
        int[] a = new int[graph.getNrOfVertices()];

        int i, j, tmp;
        for(i = 0; i < a.length; i++) {
            a[i] = i + 1;
            p[i] = 0;
        }

        int optimalCrossingNumber = calculateNrOfCrossingsInitial();
        System.out.println("Initial: " + optimalCrossingNumber);
        int lastCrossingNumber = optimalCrossingNumber;
        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < a.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                int crossingNumber = getNrOfCrossings(i, j, lastCrossingNumber, optimalCrossingNumber);

                lastCrossingNumber = crossingNumber;
                if (crossingNumber < optimalCrossingNumber) {
                    optimalCrossingNumber = crossingNumber;
                    System.out.println("New best: " + optimalCrossingNumber);
                    //System.out.println(Arrays.toString(vertexPointCombinations));
                    if (optimalCrossingNumber == 0) break;
                }
                tmp = a[j];
                a[j] = a[i];
                a[i] = tmp;

                p[i]++;
                i = 1;
            } else {
                p[i] = 0;
                i++;
            }
        }

        System.out.println(count1 + ", " + count_pruned);
        return optimalCrossingNumber;
    }

    int count1 = 0;
    int count_pruned = 0;

    private int getNrOfCrossings(int swappedVertex1, int swappedVertex2, int lastCrossingNumber, int bestCrossingNumberFound) {
        count1++;
        Point temp = vertexPointCombinations[swappedVertex1];
        vertexPointCombinations[swappedVertex1] = vertexPointCombinations[swappedVertex2];
        vertexPointCombinations[swappedVertex2] = temp;

        int crossingNumber = lastCrossingNumber;
        for (EdgeCrossing edge : edgesPerVertex[swappedVertex1]) {
            crossingNumber -= edge.getEdgeCausesCrossing();
            edge.emptyEdgeCrossings();
        }

        for (EdgeCrossing edge : edgesPerVertex[swappedVertex2]) {
            crossingNumber -= edge.getEdgeCausesCrossing();
            edge.emptyEdgeCrossings();
        }

        if (crossingNumber == lastCrossingNumber) return crossingNumber;

        return calculateNrOfCrossings(bestCrossingNumberFound, crossingNumber, swappedVertex1, swappedVertex2);
    }

    private int calculateNrOfCrossings(int bestCrossingNumberFound, int lastCrossingNumber, int vertexFirst, int vertexSecond) {
        for (EdgeCrossing edge1 : edgesPerVertex[vertexFirst]) {
            for (EdgeCrossing edge2 : edges) {
                if (Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y())) {
                    lastCrossingNumber++;
                    edge1.addEdgeCausesCrossing(edge2);
                    edge2.addEdgeCausesCrossing(edge1);
                    if (lastCrossingNumber >= bestCrossingNumberFound) return lastCrossingNumber;
                }
            }
        }

        for (EdgeCrossing edge1 : edgesPerVertex[vertexSecond]) {
            for (EdgeCrossing edge2 : edges) {
                if (Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y())) {
                    if (!edge1.edgesHavingCrossingWith.contains(edge2)) {
                        lastCrossingNumber++;
                        edge1.addEdgeCausesCrossing(edge2);
                        edge2.addEdgeCausesCrossing(edge1);
                        if (lastCrossingNumber >= bestCrossingNumberFound) return lastCrossingNumber;
                    }
                }
            }
        }

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            EdgeCrossing edge1 = edges[i];
            if (edge1.v1() != vertexFirst && edge1.v2() != vertexFirst && edge1.v1() != vertexSecond && edge1.v2() != vertexSecond) {
                for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                    EdgeCrossing edge2 = edges[j];
                    if (edge2.v1() != vertexFirst && edge2.v2() != vertexFirst && edge2.v1() != vertexSecond && edge2.v2() != vertexSecond) {
                        if (Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                                vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y())) {
                            lastCrossingNumber++;
                            edge1.addEdgeCausesCrossing(edge2);
                            edge2.addEdgeCausesCrossing(edge1);
                            if (lastCrossingNumber >= bestCrossingNumberFound) return lastCrossingNumber;
                        }
                    }
                }
            }
        }

        return lastCrossingNumber;
    }

    private int calculateNrOfCrossingsInitial() {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            EdgeCrossing edge1 = edges[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                EdgeCrossing edge2 = edges[j];
                if (Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y())) {
                    crossingNumber++;
                    edge1.addEdgeCausesCrossing(edge2);
                    edge2.addEdgeCausesCrossing(edge1);
                }
            }
        }

        return crossingNumber;
    }
}
