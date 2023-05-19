package solvers;

import graph.Edge;
import graph.Graph;
import graph.Point;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExactPruningRecursive implements Solver {
    private final Graph graph;

    public ExactPruningRecursive(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    private int[] pointVertexCombinations;
    private Point[] vertexPointCombinations;
    //private boolean[] vertexHasAtleastOneCrossing;
    private Edge[] colinearEdge = new Edge[2];
    int count = 0;
    int count_tot = 0;

    public double solve() {
        return -1;
    }

    /*private int solveRecursively(List<Integer> availableVertices, int[] assignedVertices, int idx, int crossingNr) {
        if (availableVertices.size() == 0) return getNrOfCrossings(crossingNr);
        for (Integer v : availableVertices) {
            assignedVertices[idx] = v;
            availableVertices.remove(v);
            int nrCrossings = solveRecursively(availableVertices, assignedVertices, idx++, crossingNr);
            availableVertices.add(v);
            if (nrCrossings > crossingNr) crossingNr = nrCrossings;
        }
    }

    private int getNrOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int bestCrossingNumberFound) {
        //if (colinearEdges.size() > 1) System.out.println("bigger " + colinearEdges.size());
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
            if (colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v1() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            } else if (colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[0].v2() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            } else if (colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v1() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            } else if (colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx1] || colinearEdge[1].v2() == pointVertexCombinations[swappedPointIdx2]) {
                colinearEdge[0] = null;
                colinearEdge[1] = null;
            } else return Integer.MAX_VALUE;
        }
*/
        /*if (!(pointVertexCombinations[swappedPointIdx1] != -1 && vertexHasAtleastOneCrossing[pointVertexCombinations[swappedPointIdx1]])
                && !(pointVertexCombinations[swappedPointIdx2] != -1 && vertexHasAtleastOneCrossing[pointVertexCombinations[swappedPointIdx2]])) {
            return bestCrossingNumberFound;
        }
        vertexHasAtleastOneCrossing[pointVertexCombinations[swappedPointIdx1]] = false;
        vertexHasAtleastOneCrossing[pointVertexCombinations[swappedPointIdx2]] = false;*/
/*
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
                    crossingNumber++;*/
                    /*vertexHasAtleastOneCrossing[edge1.v1()] = true;
                    vertexHasAtleastOneCrossing[edge1.v2()] = true;
                    vertexHasAtleastOneCrossing[edge2.v1()] = true;
                    vertexHasAtleastOneCrossing[edge2.v2()] = true;*//*
                    if (crossingNumber >= bestCrossingNumberFound) return bestCrossingNumberFound;
                }
            }
        }
        return crossingNumber;
    }*/
}
