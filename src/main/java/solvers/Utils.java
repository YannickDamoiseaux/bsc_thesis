package solvers;

import graph.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Utils {
    public static int doEdgesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1 = determinant(x1, y1, x2, y2, x3, y3);
        double det2 = determinant(x1, y1, x2, y2, x4, y4);
        double det3 = determinant(x3, y3, x4, y4, x1, y1);
        double det4 = determinant(x3, y3, x4, y4, x2, y2);

        if ((det1 > 0 && det2 < 0 || det1 < 0 && det2 > 0) && (det3 > 0 && det4 < 0 || det3 < 0 && det4 > 0)) return 1;
        if (det1 == 0 && ((x3 < x1 && x3 > x2) || (x3 > x1 && x3 < x2) || (y3 < y1 && y3 > y2) || (y3 > y1 && y3 < y2)))
            return -1;
        if (det2 == 0 && ((x4 < x1 && x4 > x2) || (x4 > x1 && x4 < x2) || (y4 < y1 && y4 > y2) || (y4 > y1 && y4 < y2)))
            return -1;
        if (det3 == 0 && ((x1 < x3 && x1 > x4) || (x1 > x3 && x1 < x4) || (y1 < y3 && y1 > y4) || (y1 > y3 && y1 < y4)))
            return -1;
        if (det4 == 0 && ((x2 < x3 && x2 > x4) || (x2 > x3 && x2 < x4) || (y2 < y3 && y2 > y4) || (y2 > y3 && y2 < y4)))
            return -1;
        return 0;
    }

    public static double determinant(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }

    public static ArrayList<CrossingData>[] getCrossings(Graph graph) {
        ArrayList<CrossingData> crossings = new ArrayList<>();
        HashSet<CrossingData> collinear = new HashSet<>(10000);
        Point[] points = graph.getPoints();

        for (int e_1 = 0; e_1 < graph.getNrOfEdges(); e_1++) {
            for (int i_1 = 0; i_1 < graph.getNrOfPoints(); i_1++) {
                for (int j_1 = 0; j_1 < graph.getNrOfPoints(); j_1++) {
                    if (i_1 != j_1) {
                        for (int e_2 = e_1 + 1; e_2 < graph.getNrOfEdges(); e_2++) {
                            for (int i_2 = 0; i_2 < graph.getNrOfPoints(); i_2++) {
                                if (i_2 != i_1 && i_2 != j_1) {
                                    for (int j_2 = 0; j_2 < graph.getNrOfPoints(); j_2++) {
                                        if (j_2 != i_1 && j_2 != j_1 && j_2 != i_2) {
                                            /*int crossing = Utils.doEdgesCross(points[i_1].x(), points[i_1].y(), points[j_1].x(), points[j_1].y(), points[i_2].x(), points[i_2].y(), points[j_2].x(), points[j_2].y());
                                            if (crossing != 0) {
                                                CrossingData crossingData = new CrossingData(e_1, i_1, j_1, e_2,i_2, j_2);
                                                if (crossing == 1) crossings.add(crossingData);
                                                else if (crossing == -1) collinear.add(crossingData);
                                            }*/
                                            List<boolean[]> crossing = Utils.doEdgesCrossPrecompute(points[i_1].x(), points[i_1].y(), points[j_1].x(), points[j_1].y(), points[i_2].x(), points[i_2].y(), points[j_2].x(), points[j_2].y());
                                            if (crossing.size() > 0) {
                                                if (crossing.get(0).length == 1) {
                                                    CrossingData crossingData = new CrossingData(e_1, i_1, j_1, e_2,i_2, j_2);
                                                    crossings.add(crossingData);
                                                }
                                                else {
                                                    for (boolean[] arr : crossing) {
                                                        if (arr.length > 1) {
                                                            CrossingData collinearData = new CrossingData(e_1, !arr[0] ? -1 : i_1, !arr[1] ? -1 : j_1, e_2, !arr[2] ? -1 : i_2, !arr[3] ? -1 : j_2);
                                                            //System.out.println(collinear.contains(collinearData));
                                                            collinear.add(collinearData);
                                                            //System.out.println("Colinear: " + Arrays.toString(arr) + " data = " + collinearData);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("SIZE " + collinear.size());
        //System.out.println(count_cr + " Possible crossings and " + count_co + " possible co-linearities");
        return new ArrayList[]{crossings, new ArrayList(collinear)};
    }

    private static List<boolean[]> doEdgesCrossPrecompute(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1 = determinant(x1, y1, x2, y2, x3, y3);
        double det2 = determinant(x1, y1, x2, y2, x4, y4);
        double det3 = determinant(x3, y3, x4, y4, x1, y1);
        double det4 = determinant(x3, y3, x4, y4, x2, y2);

        //System.out.println(det1 + ", " + det2 + ", " + det3 + ", " + det4);
        if ((det1 > 0 && det2 < 0 || det1 < 0 && det2 > 0) && (det3 > 0 && det4 < 0 || det3 < 0 && det4 > 0)) return List.of(new boolean[]{false});
        ArrayList<boolean[]> collinear = new ArrayList<>();
        if (det1 == 0 && ((x3 < x1 && x3 > x2) || (x3 > x1 && x3 < x2) || (y3 < y1 && y3 > y2) || (y3 > y1 && y3 < y2)))
            collinear.add(new boolean[]{true, true, true, false});
        if (det2 == 0 && ((x4 < x1 && x4 > x2) || (x4 > x1 && x4 < x2) || (y4 < y1 && y4 > y2) || (y4 > y1 && y4 < y2)))
            collinear.add(new boolean[]{true, true, false, true});
        if (det3 == 0 && ((x1 < x3 && x1 > x4) || (x1 > x3 && x1 < x4) || (y1 < y3 && y1 > y4) || (y1 > y3 && y1 < y4)))
            collinear.add(new boolean[]{true, false, true, true});
        if (det4 == 0 && ((x2 < x3 && x2 > x4) || (x2 > x3 && x2 < x4) || (y2 < y3 && y2 > y4) || (y2 > y3 && y2 < y4)))
            collinear.add(new boolean[]{false, true, true, true});
        //System.out.println(x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + x3 + ", " + y3 + ", " + x4 + ", " + y4 + Arrays.deepToString(collinear.toArray()));
        return collinear;
    }
}
