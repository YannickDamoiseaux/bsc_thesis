import graph.Edge;
import graph.Graph;
import graph.Point;

import java.util.Random;

public class GraphGenerator {
    public static void main(String[] args) {
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph graph = graphGenerator.generateGraph(12, 28, 15, 6, 6);
        graph.saveToFile("test9.json");
    }

    private final Random rand = new Random(0);

    private Graph generateGraph(int nrVertices, int nrEdges, int nrPoints, int width, int height) {
        Edge[] edges = generateEdges(nrVertices, nrEdges);
        Point[] points = generatePoints(nrPoints, width, height);
        return new Graph(nrVertices, edges, points, width, height);
    }

    private Edge[] generateEdges(int nrVertices, int nrEdges) {
        Edge[] edges = new Edge[nrEdges];
        for (int i = 0; i < edges.length; i++) {
            boolean alreadyExists = false;
            do {
                int v1 = rand.nextInt(nrVertices);
                int v2 = rand.nextInt(nrVertices);
                while (v2 == v1) {
                    v2 = rand.nextInt(nrVertices);
                }
                for (Edge edge : edges) {
                    if (edge == null) {
                        alreadyExists = false;
                        edges[i] = new Edge(v1, v2);
                        break;
                    }
                    else if ((edge.v1() == v1 && edge.v2() == v2) || (edge.v1() == v2 && edge.v2() == v1)) {
                        alreadyExists = true;
                        break;
                    }
                }
            } while (alreadyExists);
        }
        return edges;
    }

    private Point[] generatePoints(int nrPoints, int width, int height) {
        Point[] points = new Point[nrPoints];
        for (int i = 0; i < points.length; i++) {
            boolean alreadyExists = false;
            do {
                int x = rand.nextInt(width);
                int y = rand.nextInt(height);
                for (Point point : points) {
                    if (point == null) {
                        alreadyExists = false;
                        points[i] = new Point(x, y);
                        break;
                    }
                    else if (point.x() == x && point.y() == y) {
                        alreadyExists = true;
                        break;
                    }
                }
            } while (alreadyExists);
        }
        return points;
    }
}
