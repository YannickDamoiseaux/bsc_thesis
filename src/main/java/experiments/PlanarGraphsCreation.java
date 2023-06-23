package experiments;

import graph.Edge;
import graph.Graph;
import graph.Point;
import solvers.ExactBLP;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class PlanarGraphsCreation {
    public static void main(String[] args) throws URISyntaxException, IOException {
        Scanner scanner = new Scanner(new FileReader(Paths.get(Objects.requireNonNull(ExactBLP.class.getClassLoader().getResource("planar_graphs.txt")).toURI()).toFile()));
        int n = 6;
        Point[] points = new Point[n*n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                points[(i*n)+j] = new Point(i, j);
            }
        }
        int cnt = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == ':') {
                    String subString = line.substring(i+2);
                    String[] split = subString.split("; ");
                    Edge[] edges = new Edge[split.length];
                    for (int j = 0; j < split.length; j++) {
                        edges[j] = new Edge(Integer.parseInt(String.valueOf(split[j].charAt(0)))-1, Integer.parseInt(String.valueOf(split[j].charAt(2)))-1);
                    }
                    Graph graph = new Graph(n, edges, points, n, n);
                    graph.saveToFile("experiments/graphs/planar/planar_"+(cnt++)+".json");
                    break;
                }
            }
        }

    }
}
