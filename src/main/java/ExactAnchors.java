import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

public class ExactAnchors {
    private final Graph graph;

    public ExactAnchors(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println(graph.getNrOfVertices() + ", " + graph.getNrOfPoints() + ", " + graph.getNrOfEdges());
    }

    public void solve() {
        //System.out.println(calculateNumberOfCrossings(new Point[]{new Point(0, 0), new Point(3, 4), new Point(1, 0), new Point(2, 4)}));
        //System.out.println(calculateNumberOfCrossings(new Point[]{new Point(0, 0), new Point(0, 3), new Point(2, 0), new Point(2, 2), new Point(4, 0), new Point(4, 1), new Point(5, 3)}));
        LinkedList<Integer> vertices = new LinkedList<>();
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            vertices.add(i);
        }
        System.out.println(solveRecursively(new LinkedList<>(Arrays.asList(graph.getPoints())), vertices, new Point[graph.getNrOfVertices()], Integer.MAX_VALUE));
    }

    int count = 0;
    LinkedList<Edge>[][] layers;

    private int solveRecursively(LinkedList<Point> points, LinkedList<Integer> vertices, Point[] vertexPointCombinations, int optimalNrCrossings) {
        //System.out.println(points.size() + ", " + vertices.size());
        if (!points.isEmpty() && !vertices.isEmpty()) {
            LinkedList<Point> points_new = new LinkedList<>(points);
            Point point = points_new.remove(0);
            for (int vertexIdx = 0; vertexIdx < vertices.size(); vertexIdx++) {
                vertexPointCombinations[vertices.get(vertexIdx)] = point;
                LinkedList<Integer> vertices_new = new LinkedList<>(vertices);
                vertices_new.remove(vertexIdx);
                //System.out.println(Arrays.toString(points_new.toArray()) + ", " + Arrays.toString(vertices_new.toArray()));
                int nrCrossings = solveRecursively(points_new, vertices_new, vertexPointCombinations, optimalNrCrossings);
                if (nrCrossings < optimalNrCrossings) {
                    optimalNrCrossings = nrCrossings;
                    if (nrCrossings == 0) return optimalNrCrossings;
                }
            }
            return optimalNrCrossings;
        }
        else {
            return calculateNumberOfCrossings(vertexPointCombinations);
        }
    }

    private int calculateNumberOfCrossings(Point[] vertexPointCombinations) {
        //System.out.println(count++);
        //System.out.println("Combinations " + Arrays.toString(vertexPointCombinations));
        if (layers == null) layers = new LinkedList[graph.getWidth()][graph.getHeight()+1];

        for (int i = 0; i < layers.length; i++) {
            for (int j = 0; j < layers[i].length; j++) {
                layers[i][j] = new LinkedList<>();
            }
        }
        for (Edge edge : graph.getEdges()) {
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
                    layers[x-1][oldY].add(new AnchorEdge(oldY, y));
                    oldY = y;
                }
                layers[max-1][oldY].add(new AnchorEdge(oldY, v[Math.abs(source-1)].y()));
            }
            else {
                if (v[0].x() == v[1].x()) layers[v[0].x()][v[0].y()].add(new AnchorEdge(-v[0].y(), -v[1].y())); // If the edge is vertical
                else {
                    int min = Math.min(v[0].x(), v[1].x());
                    int source = v[0].x() == min ? 0 : 1;
                    layers[min][v[source].y()].add(new AnchorEdge(v[source].y(), v[Math.abs(source-1)].y()));
                }
            }
        }

        int crossingNumber = 0;
        for (int l = 0; l < layers.length; l++) {
            LinkedList<Edge>[] layer = layers[l];
            for (int i = 0; i < layer.length; i++) {
                for (int e1Idx = 0; e1Idx < layer[i].size(); e1Idx++) {
                    Edge e1 = layer[i].get(e1Idx);
                    for (int j = i + 1; j < layer.length; j++) {
                        for (int e2Idx = 0; e2Idx < layer[j].size(); e2Idx++) {
                            Edge e2 = layer[j].get(e2Idx);
                            if (e2.v1() > e1.v1() && e1.v2() > e2.v2()) crossingNumber++;
                            else if (e1.v1() > e2.v1() && e2.v2() > e1.v2()) crossingNumber++;
                            else if (e1.v2() == e2.v2()) {
                                boolean crossing = true;
                                for (Point point : vertexPointCombinations) {
                                    if (point.x() == l + 1 && point.y() == e1.v2()) {
                                        crossing = false;
                                        break;
                                    }
                                }
                                if (crossing) {
                                    if (e1.v1() != e2.v1()) {
                                        crossingNumber++;
                                    }
                                }
                            } else if (e1.v1() < 0) {
                                if (e2.v1() < Math.abs(e1.v1()) && e2.v1() > Math.abs(e1.v1())) crossingNumber++;
                                else if (e2.v1() > Math.abs(e1.v1()) && e2.v1() < Math.abs(e1.v1())) crossingNumber++;
                            } else if (e2.v1() < 0) {
                                if (e1.v1() < Math.abs(e2.v1()) && e1.v1() > Math.abs(e2.v1())) crossingNumber++;
                                else if (e1.v1() > Math.abs(e2.v1()) && e1.v1() < Math.abs(e2.v1())) crossingNumber++;
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("Anchor graph: " + Arrays.deepToString(layers) + " has " + crossingNumber + " crossing(s).");
        return crossingNumber;
    }
}
