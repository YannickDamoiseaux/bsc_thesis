package solvers.upperbound;

import algorithms.QuickSort;
import algorithms.SortObject;
import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBIP;
import solvers.ExactPruning;
import solvers.Solver;
import solvers.Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Upperbound implements Solver {
    private final Graph graph;
    private final int nrPointsPerPartition;
    private final Random rand;

    public Upperbound(String src, int nrPointsPerPartition, int seed) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        this.nrPointsPerPartition = nrPointsPerPartition;
        this.rand = new Random(seed);
        //System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    public double solve() {
        BisectionPartitioning bisectionPartitioning = new BisectionPartitioning(graph.getPoints(), nrPointsPerPartition);
        List<Point[]> pointPartitions = bisectionPartitioning.getPartitions();
        for (Point[] points : pointPartitions) {
            //System.out.println(Arrays.deepToString(points));
        }
        List<int[]> vertexPartitions = getVertexPartitions(pointPartitions);

        Point[] finalVertexPointAssignations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < pointPartitions.size(); i++) {
            ArrayList<Edge> edgesTemp = new ArrayList<>();
            for (Edge edge : graph.getEdges()) {
                for (int vertex : vertexPartitions.get(i)) {
                    if (edge.v1() == vertex || edge.v2() == vertex) {
                        edgesTemp.add(edge);
                        break;
                    }
                }
            }
            Edge[] edgeArray = new Edge[edgesTemp.size()];
            for (int j = 0; j < edgeArray.length; j++) {
                edgeArray[j] = edgesTemp.get(j);
            }

            Graph graphTemp = new Graph(graph.getNrOfVertices(), edgeArray, pointPartitions.get(i), graph.getWidth(), graph.getHeight());
            Point[] vertexPointCombinations = new Point[graph.getNrOfVertices()];
            int[] counter = new int[pointPartitions.size()];
            for (int j = 0; j < vertexPartitions.size(); j++) {
                if (i != j) {
                    for (int vertex : vertexPartitions.get(j)) {
                        if (finalVertexPointAssignations[vertex] != null) {
                            vertexPointCombinations[vertex] = finalVertexPointAssignations[vertex];
                        }
                        else vertexPointCombinations[vertex] = pointPartitions.get(j)[counter[j]++];
                    }
                }
            }
            System.out.println("Intial v/p combinations: " + Arrays.toString(vertexPointCombinations) + ", with final " + Arrays.toString(finalVertexPointAssignations));
            //System.out.println("Edges used: " + Arrays.deepToString(graphTemp.getEdges()));
            ExactPruningUB solver = new ExactPruningUB(graphTemp, vertexPointCombinations, vertexPartitions.get(i), pointPartitions.get(i));
            Point[] optimalVertexPointCombinations = solver.solve();
            System.out.println("Optimal " + Arrays.toString(optimalVertexPointCombinations));
            if (optimalVertexPointCombinations != null) {
                for (int j = 0; j < vertexPartitions.get(i).length; j++) {
                    finalVertexPointAssignations[vertexPartitions.get(i)[j]] = optimalVertexPointCombinations[vertexPartitions.get(i)[j]];
                }
            } else return Integer.MAX_VALUE;
        }

        return calculateNrOfCrossings(finalVertexPointAssignations);
    }

    private List<int[]> getVertexPartitions(List<Point[]> pointPartitions) {
        ArrayList<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertices.add(i);
        }
        ArrayList<int[]> vertexPartitions = new ArrayList<>();
        for (Point[] points : pointPartitions) {
            int[] vertexPartition = new int[points.length];
            for (int i = 0; i < points.length; i++) {
                int idx = rand.nextInt(0, vertices.size());
                vertexPartition[i] = vertices.get(idx);
                vertices.remove(idx);
            }
            vertexPartitions.add(vertexPartition);
        }

        return vertexPartitions;
    }

    private int calculateNrOfCrossings(Point[] vertexPointCombinations) {
        int crossingNumber = 0;

        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge1 = graph.getEdges()[i];
            for (int j = i + 1; j < graph.getNrOfEdges(); j++) {
                Edge edge2 = graph.getEdges()[j];
                int crossing = Utils.doEdgesCross(vertexPointCombinations[edge1.v1()].x(), vertexPointCombinations[edge1.v1()].y(), vertexPointCombinations[edge1.v2()].x(), vertexPointCombinations[edge1.v2()].y(),
                        vertexPointCombinations[edge2.v1()].x(), vertexPointCombinations[edge2.v1()].y(), vertexPointCombinations[edge2.v2()].x(), vertexPointCombinations[edge2.v2()].y());
                if (crossing == -1) {
                    return Integer.MAX_VALUE;
                }
                else if (crossing == 1) {
                    crossingNumber++;
                }
            }
        }
        if (crossingNumber == 2) System.out.println(Arrays.toString(vertexPointCombinations) + ", " + crossingNumber);
        return crossingNumber;
    }
}
