package solvers;

import graph.AnchorEdgeNew;
import graph.Edge;
import graph.Graph;
import graph.Point;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactAnchorsNoRounding implements Solver {
    private final Graph graph;

    public ExactAnchorsNoRounding(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    public double solve() {
        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertexPointCombinations[i] = graph.getPoints()[i];
        }

        layers = new LinkedList[graph.getWidth()];

        for (int i = 0; i < layers.length; i++) {
            layers[i] = new LinkedList<>();
        }

        vertexPointCombinationsOld = vertexPointCombinations.clone();
        crossingNumberOld = calculateNumberOfCrossingStatic(vertexPointCombinations);
        int optimalCrossingNumber = Integer.MAX_VALUE;
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

        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < a.length) {
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(a[i] == -1 && a[j] == -1)) {
                    int crossingNumber = calculateNumberOfCrossings(i, j);
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

    LinkedList<AnchorEdgeNew>[] layers;
    Point[] vertexPointCombinations;
    Point[] vertexPointCombinationsOld;
    int crossingNumberOld;

    public int calculateNumberOfCrossingStatic(Point[] vertexPointCombinations) {
        layers = new LinkedList[graph.getWidth()];

        for (int i = 0; i < layers.length; i++) {
            layers[i] = new LinkedList<>();
        }

        int crossingNumber = 0;
        for (Edge edge : graph.getEdges()) {
            List<AnchorEdgePackage> anchorEdges = getAnchorGraphOfEdge(vertexPointCombinations, edge);
            for (AnchorEdgePackage anchorEdgePackage : anchorEdges) {
                layers[anchorEdgePackage.x()].add(anchorEdgePackage.anchorEdge());
            }
        }

        for (int l = 0; l < layers.length; l++) {
            LinkedList<AnchorEdgeNew> layer = layers[l];
            for (int e1Idx = 0; e1Idx < layer.size(); e1Idx++) {
                AnchorEdgeNew e1 = layer.get(e1Idx);
                for (int e2Idx = e1Idx + 1; e2Idx < layer.size(); e2Idx++) {
                    AnchorEdgeNew e2 = layer.get(e2Idx);
                    int doEdgesCross = doEdgesCross(vertexPointCombinations, l, e1, e2);
                    if (doEdgesCross == 1) crossingNumber++;
                    else if (doEdgesCross == -1) return Integer.MAX_VALUE;
                }
            }
        }

        return crossingNumber;
    }

    public int calculateNumberOfCrossings(int swappedVertex1, int swappedVertex2) {
        Point temp = vertexPointCombinations[swappedVertex1];
        vertexPointCombinations[swappedVertex1] = vertexPointCombinations[swappedVertex2];
        vertexPointCombinations[swappedVertex2] = temp;

        ArrayList<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.v1() == swappedVertex1 || edge.v1() == swappedVertex2) edgesToRemove.add(edge);
            else if (edge.v2() == swappedVertex1 || edge.v2() == swappedVertex2) edgesToRemove.add(edge);
        }

        for (Edge edge : edgesToRemove) {
            List<AnchorEdgePackage> anchorEdgesToRemove = getAnchorGraphOfEdge(vertexPointCombinationsOld, edge);
            List<AnchorEdgePackage> anchorEdgesToAdd = getAnchorGraphOfEdge(vertexPointCombinations, edge);

            for (AnchorEdgePackage anchorEdgePackage : anchorEdgesToRemove) {
                for (int i = 0; i < layers[anchorEdgePackage.x()].size(); i++) {
                    AnchorEdgeNew anchorEdge = layers[anchorEdgePackage.x()].get(i);
                    if (anchorEdge.equals(anchorEdgePackage.anchorEdge())) {
                        if (anchorEdge.isEdgeCausingCrossing()) {
                            crossingNumberOld -= anchorEdge.getNrOfCrossingCausing();
                            for (AnchorEdgeNew otherEdge : anchorEdge.getOtherEdgeCausingCrossing()) {
                                otherEdge.removeEdgeCausingCrossing(anchorEdge);
                            }
                        }
                        layers[anchorEdgePackage.x()].remove(i);
                        break;
                    }
                }
            }
            for (AnchorEdgePackage anchorEdgePackage : anchorEdgesToAdd) {
                for (int i = 0; i < layers[anchorEdgePackage.x()].size(); i++) {
                    AnchorEdgeNew anchorEdge = layers[anchorEdgePackage.x()].get(i);
                    int doEdgesCross = doEdgesCross(vertexPointCombinations, anchorEdgePackage.x(), anchorEdgePackage.anchorEdge(), anchorEdge);
                    if (doEdgesCross == 1) crossingNumberOld++;
                    else if (doEdgesCross == -1) return Integer.MAX_VALUE;
                }
                layers[anchorEdgePackage.x()].add(anchorEdgePackage.anchorEdge());
            }
        }

        vertexPointCombinationsOld = vertexPointCombinations.clone();
        return crossingNumberOld;
    }

    private int doEdgesCross(Point[] vertexPointCombinations, int layer, AnchorEdgeNew e1, AnchorEdgeNew e2) {
        if (e1.v1() == e2.v1() && e1.v2() == e2.v2()) return -1;
        if (e2.v1() > e1.v1() && e1.v2() > e2.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            return 1;
        }
        else if (e1.v1() > e2.v1() && e2.v2() > e1.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            return 1;
        }
        else if (e1.v1() < 0) {
            if (e2.v1() < Math.abs(e1.v1()) && e2.v1() > Math.abs(e1.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return 1;
            }
            else if (e2.v1() > Math.abs(e1.v1()) && e2.v1() < Math.abs(e1.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return 1;
            }
        } else if (e2.v1() < 0) {
            if (e1.v1() < Math.abs(e2.v1()) && e1.v1() > Math.abs(e2.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return 1;
            }
            else if (e1.v1() > Math.abs(e2.v1()) && e1.v1() < Math.abs(e2.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return 1;
            }
        }
        else if (e1.v2() == e2.v2()) {
            for (Point point : vertexPointCombinations) {
                if (point.x() == layer + 1 && point.y() == e1.v2()) {
                    return 0;
                }
            }
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            return 1;
        }

        return 0;
    }

    private List<AnchorEdgePackage> getAnchorGraphOfEdge(Point[] vertexPointCombinations, Edge edge) {
        ArrayList<AnchorEdgePackage> anchorEdges = new ArrayList<>();
        Point[] v = {vertexPointCombinations[edge.v1()], vertexPointCombinations[edge.v2()]};
        if (Math.abs(v[0].x()-v[1].x()) > 1) {
            int min = Math.min(v[0].x(), v[1].x());
            int source = v[0].x() == min ? 0 : 1;
            int max = Math.max(v[0].x(), v[1].x());
            double coeff = (v[Math.abs(source-1)].y()-v[source].y())/(double)(max-min);
            double oldY = v[source].y();

            for (int i = 1; i < (max-min); i++) {
                int x = min+i;
                double y = v[source].y() + coeff*i;
                anchorEdges.add(new AnchorEdgePackage(x-1, new AnchorEdgeNew(oldY, y, edge)));
                oldY = y;
            }
            anchorEdges.add(new AnchorEdgePackage(max-1, new AnchorEdgeNew(oldY, v[Math.abs(source-1)].y(), edge)));
        }
        else {
            if (v[0].x() == v[1].x()) { // If the edge is vertical
                if (v[0].y() == 0) anchorEdges.add(new AnchorEdgePackage(v[0].x(), new AnchorEdgeNew( -v[1].y(), v[0].y(), edge)));
                else if (v[1].y() == 0) anchorEdges.add(new AnchorEdgePackage(v[0].x(),new AnchorEdgeNew( -v[0].y(), v[1].y(), edge)));
                else anchorEdges.add(new AnchorEdgePackage(v[0].x(),new AnchorEdgeNew( -v[0].y(), -v[1].y(), edge)));
            }
            else {
                int min = Math.min(v[0].x(), v[1].x());
                int source = v[0].x() == min ? 0 : 1;
                anchorEdges.add(new AnchorEdgePackage(min,new AnchorEdgeNew(v[source].y(), v[Math.abs(source-1)].y(), edge)));
            }
        }
        return anchorEdges;
    }
}

record AnchorEdgePackage(int x, AnchorEdgeNew anchorEdge) {}
