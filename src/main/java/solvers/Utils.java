package solvers;

public class Utils {
    public static int doEdgesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1 = determinant(x1, y1, x2, y2, x3, y3);
        double det2 = determinant(x1, y1, x2, y2, x4, y4);
        double det3 = determinant(x3, y3, x4, y4, x1, y1);
        double det4 = determinant(x3, y3, x4, y4, x2, y2);

        //System.out.println(det1 + ", " + det2 + ", " + det3 + ", " + det4);
        if ((det1 > 0 && det2 < 0 || det1 < 0 && det2 > 0) && (det3 > 0 && det4 < 0 || det3 < 0 && det4 > 0)) return 1;
        if (det1 == 0 || det2 == 0) {
            if (det1 == 0 && ((x3 < x1 && x3 > x2) || (x3 > x1 && x3 < x2) || (y3 < y1 && y3 > y2) || (y3 > y1 && y3 < y2)))
                return -1;
            if (det2 == 0 && ((x4 < x1 && x4 > x2) || (x4 > x1 && x4 < x2) || (y4 < y1 && y4 > y2) || (y4 > y1 && y4 < y2)))
                return -1;
        }
        else if (det3 == 0 || det4 == 0) {
            if (det3 == 0 && ((x1 < x3 && x1 > x4) || (x1 > x3 && x1 < x4) || (y1 < y3 && y1 > y4) || (y1 > y3 && y1 < y4)))
                return -1;
            if (det4 == 0 && ((x2 < x3 && x2 > x4) || (x2 > x3 && x2 < x4) || (y2 < y3 && y2 > y4) || (y2 > y3 && y2 < y4)))
                return -1;
        }
        return 0;
    }

    private static double determinant(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }
}
