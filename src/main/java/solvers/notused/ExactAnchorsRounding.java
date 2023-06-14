package solvers.notused;

import graph.AnchorEdge;
import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBIP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class ExactAnchorsRounding {
    private final Graph graph;

    public ExactAnchorsRounding(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    public double solve() {
        LinkedList<Integer> vertices = new LinkedList<>();
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertices.add(i);
        }
       return solveRecursively(new LinkedList<>(Arrays.asList(graph.getPoints())), vertices, new Point[graph.getNrOfVertices()], new LinkedList<>(), Integer.MAX_VALUE);
    }

    LinkedList<AnchorEdge>[][] layers;
    Point[] vertexPointCombinationsOld;
    int crossingNumberOld = 0;

    private int solveRecursively(LinkedList<Point> points, LinkedList<Integer> vertices, Point[] vertexPointCombinations, LinkedList<Integer> verticesThatChanged, int optimalNrCrossings) {
        //System.out.println(points.size() + ", " + vertices.size());
        if (!points.isEmpty() && !vertices.isEmpty()) {
            LinkedList<Point> points_new = new LinkedList<>(points);
            Point point = points_new.remove(0);
            if (vertices.size() == 1 && !verticesThatChanged.isEmpty()) {
                //System.out.println("here " + vertices.get(0) + ", " + Arrays.toString(vertices.toArray()));
                //verticesThatChanged.add(vertices.get(0));
            }
            for (int vertexIdx = 0; vertexIdx < vertices.size(); vertexIdx++) {
                vertexPointCombinations[vertices.get(vertexIdx)] = point;
                LinkedList<Integer> vertices_new = new LinkedList<>(vertices);
                vertices_new.remove(vertexIdx);
                if (vertexIdx > 0) {
                    verticesThatChanged = new LinkedList<>();
                    verticesThatChanged.addAll(vertices);
                }
                //System.out.println(Arrays.toString(points_new.toArray()) + ", " + Arrays.toString(vertices_new.toArray()));
                //System.out.println("temp " + Arrays.toString(verticesThatChanged.toArray()) + ", " + Arrays.toString(vertices_new.toArray()));
                int nrCrossings = solveRecursively(points_new, vertices_new, vertexPointCombinations, verticesThatChanged, optimalNrCrossings);
                if (nrCrossings < optimalNrCrossings) {
                    optimalNrCrossings = nrCrossings;
                    if (nrCrossings == 0) return optimalNrCrossings;
                }
                verticesThatChanged = null;
                //verticesThatChanged = new LinkedList<>();
                //verticesThatChanged.add(vertices.get(vertexIdx));
                //System.out.println("end " + vertices.get(vertexIdx));
            }
            return optimalNrCrossings;
        }
        else {
            crossingNumberOld = calculateNumberOfCrossings(vertexPointCombinations, verticesThatChanged);
            return crossingNumberOld;
        }
    }

    public int calculateNumberOfCrossings(Point[] vertexPointCombinations, LinkedList<Integer> verticesThatChanged) {
        //System.out.println(count++);
        System.out.println("Combinations " + Arrays.toString(vertexPointCombinations));
        //System.out.println("CHANGED " + Arrays.toString(verticesThatChanged.toArray()));
        if (layers == null) {
            layers = new LinkedList[graph.getWidth()][graph.getHeight()+1];

            for (int i = 0; i < layers.length; i++) {
                for (int j = 0; j < layers[i].length; j++) {
                    layers[i][j] = new LinkedList<>();
                }
            }
        }

        int crossingNumber = crossingNumberOld;

        if (verticesThatChanged.isEmpty()) {
            for (Edge edge : graph.getEdges()) {
                AnchorEdge[][] anchorEdges = getAnchorGraphOfEdge(vertexPointCombinations, edge);
                for (int x = 0; x < graph.getWidth(); x++) {
                    for (int y = 0; y < graph.getHeight() + 1; y++) {
                        if (anchorEdges[x][y] != null) {
                            layers[x][y].add(anchorEdges[x][y]);
                            break;
                        }
                    }
                }
            }

            for (int l = 0; l < layers.length; l++) {
                LinkedList<AnchorEdge>[] layer = layers[l];
                for (int i = 0; i < layer.length; i++) {
                    for (int e1Idx = 0; e1Idx < layer[i].size(); e1Idx++) {
                        AnchorEdge e1 = layer[i].get(e1Idx);
                        for (int j = i + 1; j < layer.length; j++) {
                            for (int e2Idx = 0; e2Idx < layer[j].size(); e2Idx++) {
                                AnchorEdge e2 = layer[j].get(e2Idx);
                                if (doEdgesCross(vertexPointCombinations, l, e1, e2)) crossingNumber++;
                            }
                        }
                    }
                }
            }
        }
        else {
            LinkedList<Edge> edgesToRemove = new LinkedList<>();
            for (Integer vertex : verticesThatChanged) {
                for (Edge edge : graph.getEdges()) {
                    if (edge.v1() == vertex) {
                        if (!edgesToRemove.contains(edge)) edgesToRemove.add(edge);
                    }
                    else if (edge.v2() == vertex) {
                        if (!edgesToRemove.contains(edge)) edgesToRemove.add(edge);
                    }
                }
            }

            //System.out.println(Arrays.toString(edgesToRemove.toArray()));
            ArrayList<AnchorEdge[][]> anchorEdgesToAddList = new ArrayList<>();
            //System.out.println("old combinations " + Arrays.deepToString(vertexPointCombinationsOld));
            for (Edge edge : edgesToRemove) {
                AnchorEdge[][] anchorEdgesToRemove = getAnchorGraphOfEdge(vertexPointCombinationsOld, edge);
                //System.out.println("To remove " + edge  + ": " +Arrays.deepToString(anchorEdgesToRemove));
                AnchorEdge[][] anchorEdgesToAdd = getAnchorGraphOfEdge(vertexPointCombinations, edge);
                //System.out.println("To add " + edge  + ": " +Arrays.deepToString(anchorEdgesToAdd));
                anchorEdgesToAddList.add(anchorEdgesToAdd);
                for (int x = 0; x < graph.getWidth(); x++) {
                    for (int y = 0; y < graph.getHeight() + 1; y++) {
                        if (anchorEdgesToRemove[x][y] != null) {
                            int idx = -1;
                            for (int i = 0; i < layers[x][y].size(); i++) {
                                if (layers[x][y].get(i).equals(anchorEdgesToRemove[x][y])) {
                                    idx = i;
                                    break;
                                }
                            }
                            if (layers[x][y].get(idx).isEdgeCausingCrossing()) {
                                crossingNumber -= layers[x][y].get(idx).getNrOfCrossingCausing();
                                for (AnchorEdge otherEdge : layers[x][y].get(idx).getOtherEdgeCausingCrossing()) {
                                    otherEdge.removeEdgeCausingCrossing(layers[x][y].get(idx));
                                }
                                //System.out.println("Removing crossing for edge " + layers[x][y].get(idx) + ". Crossing number becomes " + crossingNumber);
                            }
                            //System.out.println("Removing " + layers[x][y].get(idx) + " in layer " + x);
                            layers[x][y].remove(idx);
                        }
                        if (anchorEdgesToAdd[x][y] != null) {
                            layers[x][y].add(anchorEdgesToAdd[x][y]);

                            for (int i = 0; i < graph.getHeight()+1; i++) {
                                LinkedList<AnchorEdge> anchorEdges = layers[x][i];
                                for (AnchorEdge anchorEdge : anchorEdges) {
                                    if (doEdgesCross(vertexPointCombinations, x, anchorEdgesToAdd[x][y], anchorEdge)) {
                                        crossingNumber++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //System.out.println(Arrays.deepToString(layers));;'
        }

        System.out.println("Anchor graph: " + Arrays.deepToString(layers) + " has " + crossingNumber + " crossing(s).");
        vertexPointCombinationsOld = vertexPointCombinations.clone();
        //return crossingNumber;
        return 0;
    }

    private boolean doEdgesCross(Point[] vertexPointCombinations, int layer, AnchorEdge e1, AnchorEdge e2) {
        if ((e1.v1() == 2 && e1.v2() == 2) || (e2.v1() == 2 && e2.v2() == 2)) System.out.println(e1 + ", " + e2);
        if (e2.v1() > e1.v1() && e1.v2() > e2.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            return true;
        }
        else if (e1.v1() > e2.v1() && e2.v2() > e1.v2()) {
            e1.addEdgeCausingCrossing(e2);
            e2.addEdgeCausingCrossing(e1);
            return true;
        }
        else if (e1.v1() < 0) {
            if (e2.v1() < Math.abs(e1.v1()) && e2.v1() > Math.abs(e1.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return true;
            }
            else if (e2.v1() > Math.abs(e1.v1()) && e2.v1() < Math.abs(e1.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return true;
            }
        } else if (e2.v1() < 0) {
            if (e1.v1() < Math.abs(e2.v1()) && e1.v1() > Math.abs(e2.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return true;
            }
            else if (e1.v1() > Math.abs(e2.v1()) && e1.v1() < Math.abs(e2.v2())) {
                e1.addEdgeCausingCrossing(e2);
                e2.addEdgeCausingCrossing(e1);
                return true;
            }
        }
        else if (e1.v2() == e2.v2()) {
            boolean crossing = true;
            for (Point point : vertexPointCombinations) {
                if (point.x() == layer + 1 && point.y() == e1.v2()) {
                    crossing = false;
                    break;
                }
            }
            if (crossing) {
                if (e1.v1() != e2.v1()) {
                    /*e1.addEdgeCausingCrossing(e2);
                    e2.addEdgeCausingCrossing(e1);
                    return true;*/
                    AnchorEdge e1Next = e1;
                    AnchorEdge e2Next = e2;
                    boolean findNext = true;
                    int tempLayer = layer;
                    while (findNext) {
                        if (tempLayer >= layers.length) {
                            e1.addEdgeCausingCrossing(e2);
                            e2.addEdgeCausingCrossing(e1);
                            return true;
                        }
                        AnchorEdge e1Next_temp = null;
                        AnchorEdge e2Next_temp = null;
                        for (AnchorEdge edge : layers[tempLayer + 1][e1Next.v2()]) {
                            if (edge.getParentEdge().equals(e1Next.getParentEdge())) {
                                e1Next_temp = edge;
                            } else if (edge.getParentEdge().equals(e2Next.getParentEdge())) {
                                e2Next_temp = edge;
                            }
                            if (e1Next_temp != null && e2Next_temp != null) break;
                        }
                        tempLayer++;
                        if (e1Next_temp == null || e2Next_temp == null) findNext = false;
                        else {
                            if (e1Next_temp.v2() != e2Next_temp.v2()) {
                                e1.addEdgeCausingCrossing(e2);
                                e2.addEdgeCausingCrossing(e1);
                                return true;
                            }
                        }
                        e1Next = e1Next_temp;
                        e2Next = e2Next_temp;
                    }
                    if (e1Next == null && e2Next == null) return false;

                    AnchorEdge notNullEdge = e1Next == null ? e2Next : e1Next;
                    while (true) {
                        AnchorEdge notNullEdge_temp = null;
                        for (AnchorEdge edge : layers[tempLayer + 1][notNullEdge.v2()]) {
                            if (edge.getParentEdge().equals(notNullEdge.getParentEdge())) {
                                notNullEdge_temp = edge;
                                System.out.println(edge);
                                if (Math.abs(notNullEdge.v2() - notNullEdge_temp.v2()) > 1) {
                                    e1.addEdgeCausingCrossing(e2);
                                    e2.addEdgeCausingCrossing(e1);
                                    return true;
                                }
                            }
                        }

                        if (notNullEdge_temp == null) return false;
                        tempLayer++;
                    }
                }
            }
        }
        return false;
    }

    private AnchorEdge[][] getAnchorGraphOfEdge(Point[] vertexPointCombinations, Edge edge) {
        AnchorEdge[][] tempAnchorGraph = new AnchorEdge[graph.getWidth()][graph.getHeight()+1];
        Point[] v = {vertexPointCombinations[edge.v1()], vertexPointCombinations[edge.v2()]};
        if (Math.abs(v[0].x()-v[1].x()) > 1) {
            //System.out.println(v[0] + ", " + v[1]);
            int min = Math.min(v[0].x(), v[1].x());
            int source = v[0].x() == min ? 0 : 1;
            int max = Math.max(v[0].x(), v[1].x());
            double coeff = (v[Math.abs(source-1)].y()-v[source].y())/(double)(max-min);
            int oldY = v[source].y();

            for (int i = 1; i < (max-min); i++) {
                int x = min+i;
                int y = v[source].y() + (int) Math.round(coeff*i);
                tempAnchorGraph[x-1][oldY] = new AnchorEdge(oldY, y, edge);
                oldY = y;
            }
            tempAnchorGraph[max-1][oldY] = new AnchorEdge(oldY, v[Math.abs(source-1)].y(), edge);
        }
        else {
            if (v[0].x() == v[1].x()) { // If the edge is vertical
                if (v[0].y() == 0) tempAnchorGraph[v[0].x()][v[0].y()] = new AnchorEdge( -v[1].y(), v[0].y(), edge);
                else if (v[1].y() == 0) tempAnchorGraph[v[0].x()][v[0].y()] = new AnchorEdge( -v[0].y(), v[1].y(), edge);
                else tempAnchorGraph[v[0].x()][v[0].y()] = new AnchorEdge( -v[0].y(), -v[1].y(), edge);
            }
            else {
                int min = Math.min(v[0].x(), v[1].x());
                int source = v[0].x() == min ? 0 : 1;
                tempAnchorGraph[min][v[source].y()] = new AnchorEdge(v[source].y(), v[Math.abs(source-1)].y(), edge);
            }
        }
        return tempAnchorGraph;
    }
}
