import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class GraphReader {
    private final JSONObject jsonObject;

    public GraphReader(FileReader fileReader) throws IOException, ParseException {
        jsonObject = (JSONObject) new JSONParser().parse(fileReader);
    }

    public int getNrOfVertices() {
        return ((JSONArray) jsonObject.get("nodes")).size();
    }
    public int getWidth() { return (int)(long) jsonObject.get("width"); }
    public int getHeight() { return (int)(long) jsonObject.get("height"); }

    public Edge[] getEdges() {
        JSONArray edges_read = (JSONArray) jsonObject.get("edges");
        Edge[] edges = new Edge[edges_read.size()];
        for (int i = 0; i < edges.length; i++) {
            JSONObject obj = (JSONObject) edges_read.get(i);
            edges[i] = new Edge((int)(long) obj.get("source"), (int)(long) obj.get("target"));
        }
        return edges;
    }

    public Point[] getPoints() {
        JSONArray points_read = (JSONArray) jsonObject.get("points");
        Point[] points = new Point[points_read.size()];
        for (int i = 0; i < points.length; i++) {
            JSONObject obj = (JSONObject) points_read.get(i);
            points[i] = new Point((int)(long) obj.get("x"), (int)(long) obj.get("y"));
        }
        return points;
    }
}
