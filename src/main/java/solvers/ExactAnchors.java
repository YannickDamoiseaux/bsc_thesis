package solvers;

import graph.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactAnchors extends ExactSolver {
    public ExactAnchors(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public ExactAnchors() {}

    private LinkedList<AnchorEdgeNew>[] layers;
    private Point[] vertexPointCombinations;
    private Point[] vertexPointCombinationsOld;
    private Integer[] pointVertexCombinations;
    private int optimalCrossingNumber = Integer.MAX_VALUE;

    private final Random rand = new Random(0);

    public double solve() {
        pointVertexCombinations = new Integer[graph.getNrOfPoints()];
        vertexPointCombinations = new Point[graph.getNrOfVertices()];
        ArrayList<Integer> indicesToChooseFrom = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfPoints(); i++) {
            indicesToChooseFrom.add(i);
        }
        for (int i = 0; i < vertexPointCombinations.length; i++) {
            int idx = rand.nextInt(indicesToChooseFrom.size());
            vertexPointCombinations[i] = graph.getPoints()[indicesToChooseFrom.get(idx)];
            indicesToChooseFrom.remove(idx);
        }
        Arrays.fill(pointVertexCombinations, -1);

        layers = new LinkedList[graph.getWidth()];

        for (int i = 0; i < layers.length; i++) {
            layers[i] = new LinkedList<>();
        }

        vertexPointCombinationsOld = vertexPointCombinations.clone();
        //crossingNumberOld = calculateNumberOfCrossingStatic(vertexPointCombinations);
        int[] result = calculateNumberOfCrossingStatic(vertexPointCombinations);
        int lastCrossingNumber = result[0];
        if (result[1] != -1) {
            optimalCrossingNumber = lastCrossingNumber;
        }

        int[] p = new int[graph.getNrOfPoints()];

        int i, j;
        for (i = 0; i < vertexPointCombinations.length; i++) {
            for (int b = 0; b < graph.getNrOfPoints(); b++) {
                if (graph.getPoints()[b].equals(vertexPointCombinations[i])) {
                    pointVertexCombinations[b] = i;
                    break;
                }
            }
        }
        for (i = 0; i < p.length; i++) {
            p[i] = 0;
        }

        i = 1;
        // Source: https://www.quickperm.org/quickperm.php
        while(i < pointVertexCombinations.length) {
            if (Thread.currentThread().isInterrupted()) return optimalCrossingNumber;
            if (p[i] < i) {
                j = i % 2 * p[i];

                if (!(pointVertexCombinations[i] == -1 && pointVertexCombinations[j] == -1)) {
                    result = calculateNumberOfCrossings(i, j, lastCrossingNumber);
                    lastCrossingNumber = result[0];
                    if (result[1] != -1 && result[0] < optimalCrossingNumber) {
                        optimalCrossingNumber = result[0];
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

        return optimalCrossingNumber;
    }

    @Override
    public Solver newEmptyInstance() {
        return new ExactAnchors();
    }

    public int[] calculateNumberOfCrossingStatic(Point[] vertexPointCombinations) {
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
                    int doEdgesCross = doEdgesCross(vertexPointCombinations, l, e1, e2, false);
                    if (doEdgesCross == 1) crossingNumber++;
                    else if (doEdgesCross == -1) return new int[]{crossingNumber, -1};
                }
            }
        }

        return new int[]{crossingNumber, 1};
    }

    public int[] calculateNumberOfCrossings(int swappedPointIdx1, int swappedPointIdx2, int lastCrossingNumber) {
        if (pointVertexCombinations[swappedPointIdx1] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx1]] = graph.getPoints()[swappedPointIdx2];
        }
        if (pointVertexCombinations[swappedPointIdx2] != -1) {
            vertexPointCombinations[pointVertexCombinations[swappedPointIdx2]] = graph.getPoints()[swappedPointIdx1];
        }
        int temp = pointVertexCombinations[swappedPointIdx1];
        pointVertexCombinations[swappedPointIdx1] = pointVertexCombinations[swappedPointIdx2];
        pointVertexCombinations[swappedPointIdx2] = temp;

        ArrayList<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.v1() == pointVertexCombinations[swappedPointIdx1] || edge.v1() == pointVertexCombinations[swappedPointIdx2]) edgesToRemove.add(edge);
            else if (edge.v2() == pointVertexCombinations[swappedPointIdx1] || edge.v2() == pointVertexCombinations[swappedPointIdx2]) edgesToRemove.add(edge);
        }

        //boolean print = Arrays.equals(vertexPointCombinations, new Point[]{new Point(0,1), new Point(2, 1), new Point(2, 0), new Point(2, 2), new Point(1, 2), new Point(1, 0), new Point(1, 1)});
        boolean print = false;

        if (print) System.out.println(Arrays.toString(edgesToRemove.toArray()));
        if (print) System.out.println("OLD " + Arrays.toString(vertexPointCombinationsOld));
        if (print) System.out.println("NEW " + Arrays.toString(vertexPointCombinations));
        if (print) System.out.println("Current layer initial " + Arrays.toString(layers[2].toArray()));
        for (Edge edge : edgesToRemove) {
            List<AnchorEdgePackage> anchorEdgesToRemove = getAnchorGraphOfEdge(vertexPointCombinationsOld, edge);

            for (AnchorEdgePackage anchorEdgePackage : anchorEdgesToRemove) {
                for (int i = 0; i < layers[anchorEdgePackage.x()].size(); i++) {
                    AnchorEdgeNew anchorEdge = layers[anchorEdgePackage.x()].get(i);
                    if (anchorEdge.equals(anchorEdgePackage.anchorEdge())) {
                        if (anchorEdge.isEdgeCausingCrossing()) {
                            lastCrossingNumber -= anchorEdge.getNrOfCrossingCausing();
                            for (AnchorEdgeNew otherEdge : anchorEdge.getOtherEdgeCausingCrossing()) {
                                otherEdge.removeEdgeCausingCrossing(anchorEdge);
                            }
                        }
                        layers[anchorEdgePackage.x()].remove(i);
                        break;
                    }
                }
            }
        }

        int feasible = 1;
        for (Edge edge : edgesToRemove) {
            List<AnchorEdgePackage> anchorEdgesToAdd = getAnchorGraphOfEdge(vertexPointCombinations, edge);

            for (AnchorEdgePackage anchorEdgePackage : anchorEdgesToAdd) {
                for (int i = 0; i < layers[anchorEdgePackage.x()].size(); i++) {
                    AnchorEdgeNew anchorEdge = layers[anchorEdgePackage.x()].get(i);
                    int doEdgesCross = doEdgesCross(vertexPointCombinations, anchorEdgePackage.x(), anchorEdgePackage.anchorEdge(), anchorEdge, false);
                    if (doEdgesCross == 1) lastCrossingNumber++;
                    else if (doEdgesCross == -1) {
                        if (print) System.out.println(anchorEdgePackage.anchorEdge() + ", " + anchorEdge + " INFEASIBLE");
                        feasible = -1;
                        break;
                    }
                }
                layers[anchorEdgePackage.x()].add(anchorEdgePackage.anchorEdge());
            }
        }

        if (print) System.out.println("TEMP " + lastCrossingNumber);
        if (print ) System.out.println(Arrays.toString(layers[1].toArray()));

        if (feasible == 1) {
            lastCrossingNumber = 0;
            outer:
            for (int i = 0; i < layers.length; i++) {
                for (int a = 0; a < layers[i].size(); a++) {
                    for (int b = a + 1; b < layers[i].size(); b++) {
                        int doEdgesCross = doEdgesCross(vertexPointCombinations, i, layers[i].get(a), layers[i].get(b), print);
                        if (doEdgesCross == 1) lastCrossingNumber++;
                        else if (doEdgesCross == -1) {
                            feasible = -1;
                            break outer;
                        }
                    }
                }
            }
        }

        //System.out.println(lastCrossingNumber);
        vertexPointCombinationsOld = vertexPointCombinations.clone();
        return new int[]{lastCrossingNumber, feasible};
    }

    private int doEdgesCross(Point[] vertexPointCombinations, int layer, AnchorEdgeNew e1, AnchorEdgeNew e2, boolean print) {
        if (print) System.out.println(e1 + ", " + e2);
        if (e1.v1() == e2.v1() && e1.v2() == e2.v2()) {
            if (print) System.out.println("-1-0");
            return -1;
        }
        if (e1.v1() < 0) {
            if (e2.v1() < 0) {
                if (e1.v1() == e2.v1()) {
                    if ((e1.v2() < e1.v1() && e2.v2() < e1.v1()) || (e1.v2() > e1.v1() && e2.v2() > e1.v1())) {
                        if (print) System.out.println(-1 - 10);
                        return -1;
                    }
                }
                else if (e1.v1() == e2.v2()) {
                    if ((e1.v2() < e1.v1() && e2.v1() < e1.v1()) || (e1.v2() > e1.v1() && e2.v1() > e1.v1())) {
                        if (print) System.out.println(-1 - 10);
                        return -1;
                    }
                }
                else if (e1.v2() == e2.v1()) {
                    if ((e1.v1() < e1.v2() && e2.v2() < e1.v2()) || (e1.v1() > e1.v2() && e2.v2() > e1.v2())) {
                        if (print) System.out.println(-1 - 10);
                        return -1;
                    }
                }
                else if (e1.v2() == e2.v2()) {
                    if ((e1.v1() < e1.v2() && e2.v1() < e1.v2()) || (e1.v1() > e1.v2() && e2.v1() > e1.v2())) {
                        if (print) System.out.println(-1 - 10);
                        return -1;
                    }
                }
                else if (Math.abs(e2.v1()) < Math.abs(e1.v1()) && (Math.abs(e1.v2()) > Math.abs(e2.v1()) && Math.abs(e1.v2()) < Math.abs(e2.v2()))) {
                    if (print) System.out.println(-1-11);
                    return -1;
                } else if (Math.abs(e2.v1()) > Math.abs(e1.v1()) && (Math.abs(e2.v2()) > Math.abs(e1.v2()) && Math.abs(e2.v2()) < Math.abs(e1.v1()))) {
                    if (print) System.out.println(-1-12);
                    return -1;
                }
            }
            else {
                if (e2.v1() < Math.abs(e1.v1()) && e2.v1() > Math.abs(e1.v2())) {
                    for (Point point : vertexPointCombinations) {
                        if (point.x() == layer && point.y() == e2.v1()) {
                            if (print) System.out.println("-1-1");
                            return -1;
                        }
                    }
                    e1.addEdgeCausingCrossing(e2);
                    e2.addEdgeCausingCrossing(e1);
                    if (print) System.out.println("CROSS 1-3");
                    return 1;
                } else if (e2.v1() > Math.abs(e1.v1()) && e2.v1() < Math.abs(e1.v2())) {
                    for (Point point : vertexPointCombinations) {
                        if (point.x() == layer && point.y() == e2.v1()) {
                            if (print) System.out.println("-1-2");
                            return -1;
                        }
                    }
                    e1.addEdgeCausingCrossing(e2);
                    e2.addEdgeCausingCrossing(e1);
                    if (print) System.out.println("CROSS 1-4");
                    return 1;
                }
            }
        } else if (e2.v1() < 0) {
            if (e1.v1() < Math.abs(e2.v1()) && e1.v1() > Math.abs(e2.v2())) {
                for (Point point : vertexPointCombinations) {
                    if (point.x() == layer && point.y() == e1.v1()) {
                        if (print) System.out.println("-1-3");
                        return -1;
                    }
                }
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                if (print) System.out.println("CROSS 1-5");
                return 1;
            }
            else if (e1.v1() > Math.abs(e2.v1()) && e1.v1() < Math.abs(e2.v2())) {
                for (Point point : vertexPointCombinations) {
                    if (point.x() == layer && point.y() == e2.v1()) {
                        if (print) System.out.println("-1-4");
                        return -1;
                    }
                }
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                if (print) System.out.println("CROSS 1-6");
                return 1;
            }
        }
        /*else if (e1.v1() == e2.v1()) {
            boolean isEndpoint = false;
            for (Point point : vertexPointCombinations) {
                if (point.x() == layer && point.y() == e1.v1()) {
                    isEndpoint = true;
                }
            }
            if (layer - 1 >= 0) {
                if (isEndpoint) {
                    for (AnchorEdgeNew edge : layers[layer - 1]) {
                        if (edge.v2() == e1.v1() && edge.getParentEdge().equals(e1.getParentEdge())) {
                            return -1;
                        } else if (edge.v2() == e2.v1() && edge.getParentEdge().equals(e2.getParentEdge())) {
                            return -1;
                        }
                    }

                } else {
                    for (AnchorEdgeNew edge : layers[layer - 1]) {
                        if (edge.v2() == e1.v1() && edge.getParentEdge().equals(e1.getParentEdge())) {
                            if (print) System.out.println("CROSS 1-20");
                            return 1;
                        } else if (edge.v2() == e2.v1() && edge.getParentEdge().equals(e2.getParentEdge())) {
                            if (print) System.out.println("CROSS 1-21");
                            return 1;
                        }
                    }

                }
            }
        }*/
        else if (e1.v2() == e2.v2()) {
            boolean isEndpoint = false;
            for (Point point : vertexPointCombinations) {
                if (point.x() == layer && point.y() == e1.v2()) {
                    isEndpoint = true;
                }
            }
            if (layer + 1 < layers.length) {
                if (isEndpoint) {
                    for (AnchorEdgeNew edge : layers[layer + 1]) {
                        if (edge.v1() == e1.v2() && edge.getParentEdge().equals(e1.getParentEdge())) {
                            if (print) System.out.println(edge.getParentEdge() + ", with " + e1.getParentEdge());
                            return -1;
                        } else if (edge.v1() == e2.v2() && edge.getParentEdge().equals(e2.getParentEdge())) {
                            if (print) System.out.println(edge.getParentEdge() + ", with " + e2.getParentEdge());
                            return -1;
                        }
                    }

                } else {
                    for (AnchorEdgeNew edge : layers[layer + 1]) {
                        if (edge.v1() == e1.v2() && edge.getParentEdge().equals(e1.getParentEdge())) {
                            if (print) System.out.println("CROSS 1-22");
                            return 1;
                        } else if (edge.v1() == e2.v2() && edge.getParentEdge().equals(e2.getParentEdge())) {
                            if (print) System.out.println("CROSS 1-23");
                            return 1;
                        }
                    }

                }
            }
        }
        else if (e2.v1() > e1.v1() && e1.v2() > e2.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            if (print) System.out.println("CROSS 1-1");
            return 1;
        }
        else if (e1.v1() > e2.v1() && e2.v2() > e1.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            if (print) System.out.println("CROSS 1-2");
            return 1;
        }

        if (print) System.out.println("0-2");
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

    @Override
    public double getOptimalCrossingNumber() {
        return optimalCrossingNumber;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    protected double solve(int upperbound) {
        return -1;
    }
}

record AnchorEdgePackage(int x, AnchorEdgeNew anchorEdge) {}
