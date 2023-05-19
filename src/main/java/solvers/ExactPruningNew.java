package solvers;

import graph.Edge;
import graph.Graph;
import graph.EdgeCrossing;
import graph.Point;

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

    private int[] pointVertexCombinations;
    private Point[] vertexPointCombinations;

    private List<EdgeCrossing>[] edgesPerVertex;
    private EdgeCrossing[] edges;
    private ArrayList<Edge[]> crossings = new ArrayList<>();
    private Edge[] colinearEdge = new Edge[2];

    public double solve() {
        edgesPerVertex = new List[graph.getNrOfVertices()];
        edges = new EdgeCrossing[graph.getNrOfEdges()];

        pointVertexCombinations = new int[graph.getNrOfPoints()];
        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < vertexPointCombinations.length; i++) {
            vertexPointCombinations[i] = graph.getPoints()[i];
            edgesPerVertex[i] = new ArrayList<>();
        }
        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            EdgeCrossing edge = new EdgeCrossing(graph.getEdges()[i]);
            edgesPerVertex[edge.v1()].add(edge);
            edgesPerVertex[edge.v2()].add(edge);
            edges[i] = edge;
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

        int[] result = calculateNrOfCrossingsInitial();
        int lastCrossingNumber = result[0];
        int optimalCrossingNumber = Integer.MAX_VALUE;
        if (result[1] != -1) {
            optimalCrossingNumber = lastCrossingNumber;
            System.out.println("Initial: " + lastCrossingNumber);
        }
        else System.out.println("Initial: " + lastCrossingNumber + " (co-linear)");
        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    result = getNrOfCrossings(i, j, lastCrossingNumber, optimalCrossingNumber);
                    //System.out.println(crossingNumber + ", " + Arrays.toString(vertexPointCombinations));
                    lastCrossingNumber = result[0];
                    if (result[1] != -1 && result[0] < optimalCrossingNumber) {
                        optimalCrossingNumber = result[0];
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

    private int[] getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int lastCrossingNumber, int bestCrossingNumberFound) {
        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx1]] = graph.getPoints()[swappedPointIdx2];
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx2]] = graph.getPoints()[swappedPointIdx1];
        }
        int temp = pointVertexCombinations[swappedPointIdx1];
        pointVertexCombinations[swappedPointIdx1] = pointVertexCombinations[swappedPointIdx2];
        pointVertexCombinations[swappedPointIdx2] = temp;

        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            for (EdgeCrossing edge : edgesPerVertex[pointVertexCombinations[swappedPointIdx1]]) {
                lastCrossingNumber -= edge.getEdgeCausesCrossing();
                edge.emptyEdgeCrossings();
            }
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            for (EdgeCrossing edge : edgesPerVertex[pointVertexCombinations[swappedPointIdx2]]) {
                lastCrossingNumber -= edge.getEdgeCausesCrossing();
                edge.emptyEdgeCrossings();
            }
        }

        crossings.removeIf(edgeArr -> edgeArr[0].v1() == pointVertexCombinations[swappedPointIdx1] || edgeArr[0].v2() == pointVertexCombinations[swappedPointIdx1] || edgeArr[1].v1() == pointVertexCombinations[swappedPointIdx1]
                || edgeArr[1].v2() == pointVertexCombinations[swappedPointIdx1] || edgeArr[0].v1() == pointVertexCombinations[swappedPointIdx2] || edgeArr[0].v2() == pointVertexCombinations[swappedPointIdx2]
                || edgeArr[1].v1() == pointVertexCombinations[swappedPointIdx2] || edgeArr[1].v2() == pointVertexCombinations[swappedPointIdx2]);

        if (colinearEdge[0] != null) {
            if (colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx2] || colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx2]
            || colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx2] || colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            } else return new int[]{lastCrossingNumber, -1};
        }

        //if (crossingNumber == lastCrossingNumber) return crossingNumber;

        return calculateNrOfCrossings(bestCrossingNumberFound, lastCrossingNumber, pointVertexCombinations[swappedPointIdx1], pointVertexCombinations[swappedPointIdx2]);
    }

    private int[] calculateNrOfCrossings(int bestCrossingNumberFound, int lastCrossingNumber, int vertexFirst, int vertexSecond) {
        if (vertexFirst != -1) {
            for (EdgeCrossing edge1 : edgesPerVertex[vertexFirst]) {
                for (EdgeCrossing edge2 : edges) {
                    int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                            vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                    if (crossing == -1) {
                        colinearEdge[0] = edge1;
                        colinearEdge[1] = edge2;
                        return new int[]{lastCrossingNumber, -1};
                    } else if (crossing == 1) {
                        lastCrossingNumber++;
                        edge1.addEdgeCausesCrossing(edge2);
                        edge2.addEdgeCausesCrossing(edge1);
                        crossings.add(new Edge[]{edge1, edge2});
                        if (lastCrossingNumber >= bestCrossingNumberFound) return new int[]{lastCrossingNumber, 1};
                    }
                }
            }
        }
        if (vertexSecond != -1) {
            for (EdgeCrossing edge1 : edgesPerVertex[vertexSecond]) {
                for (EdgeCrossing edge2 : edges) {
                    int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                            vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                    if (crossing == -1) {
                        colinearEdge[0] = edge1;
                        colinearEdge[1] = edge2;
                        return new int[]{lastCrossingNumber, -1};
                    } else if (crossing == 1) {
                        if (!edge1.edgesHavingCrossingWith.contains(edge2)) {
                            lastCrossingNumber++;
                            edge1.addEdgeCausesCrossing(edge2);
                            edge2.addEdgeCausesCrossing(edge1);
                            crossings.add(new Edge[]{edge1, edge2});
                            if (lastCrossingNumber >= bestCrossingNumberFound) return new int[]{lastCrossingNumber, 1};
                        }
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
                        boolean duplicate = false;
                        for (Edge[] edgeArr : crossings) {
                            if ((edgeArr[0].equals(edge1) && edgeArr[1].equals(edge2)) || (edgeArr[0].equals(edge2) && edgeArr[1].equals(edge1))) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                                    vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                            if (crossing == -1) {
                                colinearEdge[0] = edge1;
                                colinearEdge[1] = edge2;
                                return new int[]{lastCrossingNumber, -1};
                            }
                            else if (crossing == 1) {
                                lastCrossingNumber++;
                                edge1.addEdgeCausesCrossing(edge2);
                                edge2.addEdgeCausesCrossing(edge1);
                                crossings.add(new Edge[]{edge1, edge2});
                                if (lastCrossingNumber >= bestCrossingNumberFound)
                                    return new int[]{lastCrossingNumber, 1};
                            }
                        }
                    }
                }
            }
        }

        return new int[]{lastCrossingNumber, 1};
    }

    private int[] calculateNrOfCrossingsInitial() {
        int crossingNumber = 0;
        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            EdgeCrossing edge1 = edges[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                EdgeCrossing edge2 = edges[j];
                int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                if (crossing == -1) { return new int[]{crossingNumber, -1}; }
                else if (crossing == 1) {
                    crossingNumber++;
                    edge1.addEdgeCausesCrossing(edge2);
                    edge2.addEdgeCausesCrossing(edge1);
                }
            }
        }

        return new int[]{crossingNumber, 1};
    }
}
