package solvers.upperbound;

import algorithms.QuickSort;
import algorithms.SortObject;
import graph.Graph;
import graph.Point;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class BisectionPartitioning {
    private final Point[] pointSet;
    private final int nrPointsPerPartition;

    public BisectionPartitioning(Point[] pointSet, int nrPointsPerPartition) {
        this.pointSet = pointSet;
        this.nrPointsPerPartition = nrPointsPerPartition;
    }

    public List<Point[]> getPartitions() {
        return bisectionPartition(pointSet, true);
    }

    private ArrayList<Point[]> bisectionPartition(Point[] points, boolean xSorting) {
        if (points.length <= nrPointsPerPartition) return new ArrayList<>(Collections.singleton(points));
        QuickSort<Point> quickSort = new QuickSort<>();
        SortObject<Point>[] sortObjects = new SortObject[points.length];
        for (int i = 0; i < sortObjects.length; i++) {
            sortObjects[i] = new SortObject<>(points[i], xSorting ? points[i].x() : points[i].y());
        }
        SortObject<Point>[] sortedPoints = quickSort.sort(sortObjects, 0, sortObjects.length-1);

        Point[] subset1 = new Point[sortedPoints.length/2];
        for (int i = 0; i < subset1.length; i++) {
            subset1[i] = sortedPoints[i].object;
        }
        Point[] subset2 = new Point[sortedPoints.length-subset1.length];
        for (int i = 0; i < subset2.length; i++) {
            subset2[i] = sortedPoints[i+subset1.length].object;
        }

        ArrayList<Point[]> pointsList = bisectionPartition(subset1, !xSorting);
        pointsList.addAll(bisectionPartition(subset2, !xSorting));
        return pointsList;
    }
}
