import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

public class Graph {
    private final int nrVertices;
    private final Edge[] edges;
    private final Point[] points;
    private final int width;
    private final int height;
    public Graph(FileReader fileReader) {
        try {
            GraphReader graphReader = new GraphReader(fileReader);
            this.nrVertices = graphReader.getNrOfVertices();
            this.width = graphReader.getWidth();
            this.height = graphReader.getHeight();
            this.edges = graphReader.getEdges();
            this.points = graphReader.getPoints();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public Graph(int nrVertices, Edge[] edges, Point[] points, int width, int height) {
        this.nrVertices = nrVertices;
        this.edges = edges;
        this.points = points;
        this.width = width;
        this.height = height;
    }

    public int getNrOfVertices() { return nrVertices; }
    public int getNrOfPoints() { return points.length; }
    public int getNrOfEdges() { return edges.length; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Edge[] getEdges() { return edges; }
    public Point[] getPoints() { return points; }

    public void saveToFile(String fileName) {
        JSONArray nodes = new JSONArray();
        for (int i = 0; i < nrVertices; i++) {
            JSONObject node = new JSONObject();
            node.put("id", i);
            nodes.add(i, node);
        }

        JSONArray edges = new JSONArray();
        for (int i = 0; i < getNrOfEdges(); i++) {
            JSONObject edge = new JSONObject();
            edge.put("source", this.edges[i].v1());
            edge.put("target", this.edges[i].v2());
            edges.add(i, edge);
        }

        JSONArray points = new JSONArray();
        for (int i = 0; i < getNrOfPoints(); i++) {
            JSONObject point = new JSONObject();
            point.put("id", i);
            point.put("x", this.points[i].x());
            point.put("y", this.points[i].y());
            points.add(i, point);
        }

        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("nodes", nodes);
        jsonFileObject.put("edges", edges);
        jsonFileObject.put("points", points);
        jsonFileObject.put("width", width);
        jsonFileObject.put("height", height);

        try (FileWriter file = new FileWriter("src/main/resources/"+fileName))
        {
            //We can write any JSONArray or JSONObject instance to the file
            file.write(jsonFileObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
