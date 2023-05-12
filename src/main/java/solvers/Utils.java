package solvers;

import graph.CrossingData;
import graph.Edge;
import graph.Graph;
import graph.Point;

import java.util.ArrayList;

public class Utils {
    public static boolean doEdgesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1 = determinant(x1, y1, x2, y2, x3, y3);
        double det2 = determinant(x1, y1, x2, y2, x4, y4);
        double det3 = determinant(x3, y3, x4, y4, x1, y1);
        double det4 = determinant(x3, y3, x4, y4, x2, y2);
        return (det1 > 0 && det2 < 0 || det1 < 0 && det2 > 0) && (det3 > 0 && det4 < 0 || det3 < 0 && det4 > 0);
    }

    private static double determinant(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }
}
