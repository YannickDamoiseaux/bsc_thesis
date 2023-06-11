package solvers.upperbound;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBIP;
import solvers.Solver;
import solvers.UpperBoundSolver;
import solvers.Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Upperbound extends UpperBoundSolver {
    public Upperbound(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
    }
    public Upperbound() {};

    public double solve(List<Point[]> pointPartitions, List<int[]> vertexPartitions, boolean useDistanceMetric, Long startTime) {
        int[] vertexIsInPartition = new int[graph.getNrOfVertices()];
        for (int i = 0; i < vertexPartitions.size(); i++) {
            int[] vertexPartition = vertexPartitions.get(i);
            for (int vertex : vertexPartition) {
                vertexIsInPartition[vertex] = i;
            }
        }

        int[] pointVertexPartitionsAssignation =  vertexPartitionAssignedToPointPartition(vertexPartitions, pointPartitions, vertexIsInPartition, useDistanceMetric);
        int[] vertexPointPartitionsAssignation = new int[pointVertexPartitionsAssignation.length];
        for (int i = 0; i < pointVertexPartitionsAssignation.length; i++) {
            vertexPointPartitionsAssignation[pointVertexPartitionsAssignation[i]] = i;
        }

        Point[] finalVertexPointAssignations = new Point[graph.getNrOfVertices()];
        ArrayList<Edge> edgesInFixedPartitions = new ArrayList<>();
        for (int i = 0; i < pointPartitions.size(); i++) {
            //System.out.println("Partition " + (i+1) +" of " + pointPartitions.size());
            ArrayList<Edge> edgesTemp = new ArrayList<>();
            for (Edge edge : graph.getEdges()) {
                for (int vertex : vertexPartitions.get(pointVertexPartitionsAssignation[i])) {
                    if ((edge.v1() == vertex || edge.v2() == vertex) && (vertexPointPartitionsAssignation[vertexIsInPartition[edge.v1()]] >= i && vertexPointPartitionsAssignation[vertexIsInPartition[edge.v2()]] >= i)) {
                        edgesTemp.add(edge);
                        break;
                    }
                }
            }
            Edge[] edgeArray = new Edge[edgesTemp.size()+edgesInFixedPartitions.size()];
            //Edge[] edgeArray = new Edge[edgesTemp.size()];
            for (int j = 0; j < edgesInFixedPartitions.size(); j++) {
                edgeArray[j] = edgesInFixedPartitions.get(j);
            }
            int tempSize = edgesInFixedPartitions.size();
            for (int j = 0; j < edgesTemp.size(); j++) {
                edgeArray[j+tempSize] = edgesTemp.get(j);
                //edgeArray[j] = edgesTemp.get(j);
                edgesInFixedPartitions.add(edgesTemp.get(j));
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

            double timePerPartition = (29000-((System.nanoTime()-startTime)/1000000.0))/pointPartitions.size();
            ExactPruningUB solver = new ExactPruningUB(graphTemp, vertexPointCombinations, vertexPartitions.get(i), pointPartitions.get(i), timePerPartition);
            Point[] optimalVertexPointCombinations = solver.solve();

            if (optimalVertexPointCombinations != null) {
                for (int j = 0; j < vertexPartitions.get(i).length; j++) {
                    finalVertexPointAssignations[vertexPartitions.get(i)[j]] = optimalVertexPointCombinations[vertexPartitions.get(i)[j]];
                }
            } else {
                return Integer.MAX_VALUE;
            }
        }

        return calculateNrOfCrossings(finalVertexPointAssignations);
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
                    System.out.println(edge1 + ", " + edge2 + ", " + Arrays.toString(vertexPointCombinations));
                    System.out.println("***** ERROR: Co-linearity cannot be detected at the end. Error in calculating nr. of crossings in partitions. *****");
                    return Integer.MAX_VALUE;
                }
                else if (crossing == 1) {
                    crossingNumber++;
                }
            }
        }

        return crossingNumber;
    }

    private int[] vertexPartitionAssignedToPointPartition(List<int[]> vertexPartitions, List<Point[]> pointPartitions, int[] vertexIsInPartition, boolean useDistanceMetric) {
        if (useDistanceMetric) {
            int[][] edgesFromPartitionToPartition = new int[vertexPartitions.size()][vertexPartitions.size()];
            for (Edge edge : graph.getEdges()) {
                edgesFromPartitionToPartition[vertexIsInPartition[edge.v1()]][vertexIsInPartition[edge.v2()]]++;
                edgesFromPartitionToPartition[vertexIsInPartition[edge.v2()]][vertexIsInPartition[edge.v1()]]++;
            }

            int[][] distanceBetweenPointPartitions = new int[pointPartitions.size()][pointPartitions.size()];
            for (int i = 0; i < pointPartitions.size(); i++) {
                for (int j = i + 1; j < pointPartitions.size(); j++) {
                    double dist = distanceBetweenPointPartitions(pointPartitions.get(i), pointPartitions.get(j));
                    int distInt = (int) Math.round(dist);
                    distanceBetweenPointPartitions[i][j] = distInt;
                    distanceBetweenPointPartitions[j][i] = distInt;
                }
            }

            int[] vertexPartitionSizes = new int[vertexPartitions.size()];
            for (int i = 0; i < vertexPartitionSizes.length; i++) {
                vertexPartitionSizes[i] = vertexPartitions.get(i).length;
            }

            int[] p = new int[pointPartitions.size()];
            int[] a = new int[pointPartitions.size()];
            int i, j, tmp;
            for (i = 0; i < a.length; i++) {
                a[i] = i;
            }

            int optimalScore = getDistanceScore(a, edgesFromPartitionToPartition, distanceBetweenPointPartitions, Integer.MAX_VALUE);
            int[] optimalAssignation = a.clone();

            //System.out.println("INITIAL " + optimalScore);
            i = 1;
            // Source: https://www.quickperm.org/quickperm.php
            while (i < a.length) {
                if (p[i] < i) {
                    j = i % 2 * p[i];
                    tmp = a[j];
                    a[j] = a[i];
                    a[i] = tmp;

                    boolean possible = true;
                    for (int k = 0; k < vertexPartitionSizes.length; k++) {
                        if (vertexPartitionSizes[a[k]] != pointPartitions.get(k).length) {
                            possible = false;
                            break;
                        }
                    }

                    if (possible) {
                        int score = getDistanceScore(a, edgesFromPartitionToPartition, distanceBetweenPointPartitions,optimalScore);
                        if (score < optimalScore) {
                            optimalScore = score;
                            optimalAssignation = a.clone();
                        }
                    }

                    p[i]++;
                    i = 1;
                } else {
                    p[i] = 0;
                    i++;
                }
            }

            return optimalAssignation;
        }
        else {
            int[] assignation = new int[pointPartitions.size()];
            for (int i = 0; i < assignation.length; i++) {
                assignation[i] = i;
            }
            return assignation;
        }
    }

    private int getDistanceScore(int[] assignation, int[][] edgesFromPartitionToPartition, int[][] distanceBetweenPointPartitions, int bestScore) {
        int sum = 0;
        for (int i = 0; i < assignation.length; i++) {
            for (int j = i + 1; j < assignation.length; j++) {
                sum += edgesFromPartitionToPartition[assignation[i]][assignation[j]]*distanceBetweenPointPartitions[i][j];
                if (sum >= bestScore) return Integer.MAX_VALUE;
            }
        }
        return sum;
    }

    private double distanceBetweenPointPartitions(Point[] p1, Point[] p2) {
        double sum = 0;
        int cnt = 0;
        for (Point point1 : p1) {
            for (Point point2 : p2) {
                sum += Math.sqrt(Math.pow(point2.x()-point1.x(), 2) + Math.pow(point2.y()-point1.y(), 2));
                cnt++;
            }
        }
        return sum/cnt;
    }

    @Override
    public double solve() {
        return -1000;
    }

    @Override
    public Solver newEmptyInstance() {
        return null;
    }

    @Override
    public double getOptimalCrossingNumber() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
