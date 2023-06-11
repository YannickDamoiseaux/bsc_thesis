package solvers.upperbound;

import graph.Edge;
import graph.Point;
import jep.*;
import solvers.Solver;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpperboundMetis extends Upperbound {
    private final boolean useDistanceMetric;
    private final boolean convexSolver;
    private static JepConfig jepConfig;

    private double upperbound;

    public UpperboundMetis(String src, int nrPartitions, boolean useDistanceMetric, boolean convexSolver) throws URISyntaxException, FileNotFoundException {
        super(src);
        this.nrPartitions = nrPartitions;
        this.useDistanceMetric = useDistanceMetric;
        this.convexSolver = convexSolver;
        this.nrVerticesPerPartition = -1;
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }
    public UpperboundMetis(int nrPartitions, boolean useDistanceMetric, boolean convexSolver) {
        this.nrPartitions = nrPartitions;
        this.useDistanceMetric = useDistanceMetric;
        this.convexSolver = convexSolver;
        this.nrVerticesPerPartition = -1;
    }
    public UpperboundMetis(boolean useDistanceMetric, boolean convexSolver, int nrVerticesPerPartition) {
        this.useDistanceMetric = useDistanceMetric;
        this.convexSolver = convexSolver;
        this.nrVerticesPerPartition = nrVerticesPerPartition;
        this.nrPartitions = -1;
    }
    public UpperboundMetis(boolean useDistanceMetric, boolean convexSolver) {
        this.useDistanceMetric = useDistanceMetric;
        this.convexSolver = convexSolver;
        this.nrPartitions = -1;
        this.nrVerticesPerPartition = -1;
    }

    @Override
    public double solve() {
        Long startTime = System.nanoTime();
        if (nrPartitions == -1) {
            nrPartitions = graph.getNrOfVertices()/nrVerticesPerPartition;
        }
        ArrayList<ArrayList<Integer>> edgesPerVertex = new ArrayList<>();
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            edgesPerVertex.add(new ArrayList<>());
        }
        for (int i = 0; i < graph.getNrOfEdges(); i++) {
            Edge edge = graph.getEdges()[i];
            edgesPerVertex.get(edge.v1()).add(edge.v2());
            edgesPerVertex.get(edge.v2()).add(edge.v1());
        }
        if (jepConfig == null) {
            String pythonFolder = System.getenv("DYLD_LIBRARY_PATH");
            //define the JEP library path
            String jepPath = pythonFolder + "/jep/libjep.jnilib";
            //initialize the MainInterpreter
            MainInterpreter.setJepLibraryPath(jepPath);

            // set path for python docs with python script to run
            jepConfig = new JepConfig();
            jepConfig.addIncludePaths("/Users/yannick/IdeaProjects/bsc_thesis/src/main/java");
        }
        //create the interpreter for python executing
        try (SubInterpreter subInterp = jepConfig.createSubInterpreter()) {
            subInterp.eval("import importlib.util");
            subInterp.eval("import sys");
            subInterp.eval("spec = importlib.util.spec_from_file_location('metis', '/Users/yannick/IdeaProjects/bsc_thesis/src/main/java/solvers/upperbound/metis.py')");
            subInterp.eval("m = importlib.util.module_from_spec(spec)");
            subInterp.eval("sys.modules['metis'] = m");
            subInterp.eval("spec.loader.exec_module(m)");

            subInterp.eval("result = m.test("+edgesPerVertex+","+nrPartitions+")");
            List<Long> result = (List<Long>) subInterp.getValue("result");
            int[] count = new int[nrPartitions];
            for (Long aLong : result) {
                count[aLong.intValue()]++;
            }
            ArrayList<int[]> vertexPartitions = new ArrayList<>();
            for (int j : count) {
                vertexPartitions.add(new int[j]);
            }
            int[] idx = new int[vertexPartitions.size()];
            for (int i = 0; i < result.size(); i++) {
                vertexPartitions.get(result.get(i).intValue())[idx[result.get(i).intValue()]++] = i;
            }

            int[] pointsTooMuch = new int[vertexPartitions.size()];
            int diff = graph.getNrOfPoints()-graph.getNrOfVertices();
            double pointsPerPartion = (double) diff/pointsTooMuch.length;
            int diffLeft = diff;
            for (int i = 0; i < pointsTooMuch.length; i++) {
                if (diffLeft == 0) break;
                else if (diffLeft > pointsPerPartion) {
                    pointsTooMuch[i] = (int) Math.ceil(pointsPerPartion);
                    diffLeft -= (int) Math.ceil(pointsPerPartion);
                }
                else {
                    pointsTooMuch[i] = diffLeft;
                    break;
                }
            }

            upperbound = Integer.MAX_VALUE;
            boolean[] boolArray = {true, false};
            for (Boolean bool1 : boolArray) {
                for (Boolean bool2 : boolArray) {
                    if (Thread.currentThread().isInterrupted()) return upperbound;
                    if (convexSolver) {
                        List<Point[]> pointPartitions = bisectionPartitionConvex(vertexPartitions, pointsTooMuch, bool1, bool2);
                        double crossingNumber = super.solve(pointPartitions, vertexPartitions, useDistanceMetric, startTime);
                        if (crossingNumber < upperbound) upperbound = crossingNumber;
                    }
                    else {
                        List<Point[]> pointPartitions = bisectionPartitionNonConvex(vertexPartitions, pointsTooMuch, bool1, bool2);
                    /*System.out.println("Solving..");
                    System.out.print("Point partition size: ");
                    for (int i = 0; i < pointPartitions.size(); i++) {
                        System.out.print(pointPartitions.get(i).length + ", ");
                    }
                    System.out.println();
                    System.out.print("Vertex partition size: ");
                    for (int i = 0; i < vertexPartitions.size(); i++) {
                        System.out.print(vertexPartitions.get(i).length + ", ");
                    }
                    System.out.println();*/
                        double crossingNumber = super.solve(pointPartitions, vertexPartitions, useDistanceMetric, startTime);
                        if (crossingNumber < upperbound) {
                            upperbound = crossingNumber;
                            if (upperbound == 0) return upperbound;
                        }
                    }
                    System.out.println("Current upper bound = " + upperbound);
                }
            }

            subInterp.close();
            return upperbound;
        }
        //}
    }

    private ArrayList<Point[]> bisectionPartitionNonConvex(ArrayList<int[]> vertexPartitions, int[] pointsTooMuch, boolean ascendingSorting, boolean firstX) {
        ArrayList<Point[]> pointPartitions = new ArrayList<>();
        Point[] sortedPoints = graph.getPoints().clone();
        for (int i = 0; i < vertexPartitions.size(); i++) {
            Point[] pointPartition = new Point[vertexPartitions.get(i).length+pointsTooMuch[i]];

            int firstSize = pointPartition.length/2;
            if (i%2 == (firstX ? 0 : 1)) {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::x).thenComparingInt(Point::y));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::x).thenComparingInt(Point::y)));
            }
            else {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::y).thenComparingInt(Point::x));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::y).thenComparingInt(Point::x)));
            }
            System.arraycopy(sortedPoints, 0, pointPartition, 0, firstSize);

            Point[] sortedPointsNew = new Point[sortedPoints.length-firstSize];
            System.arraycopy(sortedPoints, firstSize, sortedPointsNew, 0, sortedPointsNew.length);
            sortedPoints = sortedPointsNew.clone();

            int secondSize = pointPartition.length-firstSize;
            if (i%2 == (firstX ? 1 : 0)) {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::x).thenComparingInt(Point::y));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::x).thenComparingInt(Point::y)));
            }
            else {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::y).thenComparingInt(Point::x));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::y).thenComparingInt(Point::x)));
            }
            System.arraycopy(sortedPoints, 0, pointPartition, firstSize, secondSize);

            sortedPointsNew = new Point[sortedPointsNew.length-secondSize];
            System.arraycopy(sortedPoints, secondSize, sortedPointsNew, 0, sortedPointsNew.length);
            sortedPoints = sortedPointsNew;

            pointPartitions.add(pointPartition);
        }

        return pointPartitions;
    }

    private ArrayList<Point[]> bisectionPartitionConvex(ArrayList<int[]> vertexPartitions, int[] pointsTooMuch, boolean ascendingSorting, boolean firstX) {
        ArrayList<Point[]> pointPartitions = new ArrayList<>();
        Point[] sortedPoints = graph.getPoints().clone();
        for (int i = 0; i < vertexPartitions.size(); i++) {
            int size = vertexPartitions.get(i).length;
            if (i%2 == (firstX ? 0 : 1)) {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::x).thenComparingInt(Point::y));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::x).thenComparingInt(Point::y)));
            }
            else {
                if (ascendingSorting) Arrays.sort(sortedPoints, Comparator.comparingInt(Point::y).thenComparingInt(Point::x));
                else Arrays.sort(sortedPoints, Collections.reverseOrder(Comparator.comparingInt(Point::y).thenComparingInt(Point::x)));
            }

            Point[] pointPartition = new Point[size+pointsTooMuch[i]];
            System.arraycopy(sortedPoints, 0, pointPartition, 0, pointPartition.length);
            pointPartitions.add(pointPartition);

            Point[] sortedPointsNew = new Point[sortedPoints.length-pointPartition.length];
            System.arraycopy(sortedPoints, pointPartition.length, sortedPointsNew, 0, sortedPointsNew.length);
            sortedPoints = sortedPointsNew;
        }

        return pointPartitions;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + "_" + useDistanceMetric + "_" + convexSolver;
    }

    @Override
    public Solver newEmptyInstance() {
        if (nrVerticesPerPartition == -1 && nrPartitions == -1) return new UpperboundMetis(this.useDistanceMetric, this.convexSolver);
        else if (nrVerticesPerPartition == -1) return new UpperboundMetis(this.nrPartitions, this.useDistanceMetric, this.convexSolver);
        else return new UpperboundMetis(this.useDistanceMetric, this.convexSolver, this.nrVerticesPerPartition);
    }

    @Override
    public double getOptimalCrossingNumber() {
        return upperbound;
    }
}
