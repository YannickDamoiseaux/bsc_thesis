package experiments;

import graph.Graph;
import solvers.*;
import solvers.notused.ExactPruningRecursive;
import solvers.upperbound.UpperboundMetis;
import solvers.upperbound.UpperboundRandom;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class Experiments {
    public static void main(String[] args) {
        exact_1();
        exact_2();
    }

    private static void exact_1() {
        ExecutorService threadpool = Executors.newFixedThreadPool(2);
        Solver[] solvers = {new ExactAnchors(), new ExactBIP()};
        int[] vertices = {5, 8, 11, 20};
        double[] densities = {0.3, 0.6};
        ArrayList<Callable<Object>> runnables = new ArrayList<>();
        for (int v : vertices) {
            for (double d : densities) {
                for (Solver s : solvers) {
                    runnables.add(() -> {
                        try {
                            runConfiguration(s, v, d, 3, 1.75, "", Executors.newSingleThreadExecutor());
                            return null;
                        } catch (URISyntaxException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }

        try {
            threadpool.invokeAll(runnables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        threadpool.shutdown();
    }

    private static void exact_2() {
        ExecutorService threadpool = Executors.newFixedThreadPool(2);
        Solver[] solvers = {new ExactAnchors(), new ExactBIP()};
        double[] resolutionPowers = {1.25, 1.75};
        int[] nrPointsMultipliers = {1, 3};
        ArrayList<Callable<Object>> runnables = new ArrayList<>();
        for (double r : resolutionPowers) {
            for (int p : nrPointsMultipliers) {
                for (Solver s : solvers) {
                    runnables.add(() -> {
                        try {
                            runConfiguration(s, 8, 0.6, p, r, "", Executors.newSingleThreadExecutor());
                            return null;
                        } catch (URISyntaxException | FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
        try {
            threadpool.invokeAll(runnables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        threadpool.shutdown();
    }

    private static void upperbound_1() {
        UpperBoundSolver[] solvers = {new UpperboundMetis(true, true), new UpperboundMetis(true, false)};
        int[] vertices = {20, 29}; //{20, 29, 56, 83, 110};
        int[] nrVerticesPerPartition = {4, 7, 10};
        for (int v : vertices) {
            for (int nV : nrVerticesPerPartition) {
                for (UpperBoundSolver s : solvers) {
                    try {
                        runConfigurationUpperBound(s, v, 0.6, 3, 1.75, nV, "", Executors.newSingleThreadExecutor());
                    } catch (URISyntaxException | FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void upperbound_2() {
        UpperBoundSolver[] solvers = {new UpperboundMetis(true, true)};//, new UpperboundMetis(true, false)};//, new UpperboundRandom(true)};
        int[] vertices = {5, 8, 11};
        double[] densities = {0.3, 0.6};
        for (int v : vertices) {
            for (double d : densities) {
                for (UpperBoundSolver s : solvers) {
                    try {
                        runConfigurationUpperBound(s, v, d, 3, 1.75, -1, "", Executors.newSingleThreadExecutor());
                    } catch (URISyntaxException | FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void upperbound_3() {
        UpperBoundSolver[] solvers = {new UpperboundMetis(true, true), new UpperboundRandom(true)};//, new UpperboundMetis(true, false)};
        double[] resolutionPowers = {1.25, 1.75};
        int[] nrPointsMultiplier = {1, 3};
        for (double r : resolutionPowers) {
            for (int p : nrPointsMultiplier) {
                for (UpperBoundSolver s : solvers) {
                    try {
                        runConfigurationUpperBound(s, 8, 0.6, p, r, -1, "", Executors.newSingleThreadExecutor());
                    } catch (URISyntaxException | FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void planar_exact() {
        ExecutorService threadpool = Executors.newFixedThreadPool(3);
        Solver[] solvers = {new ExactAnchors()};
        ArrayList<Callable<Object>> runnables = new ArrayList<>();
        for (Solver s : solvers) {
            runnables.add(() -> {
                try {
                    runConfigurationPlanar(s, Executors.newSingleThreadExecutor());
                    return null;
                } catch (URISyntaxException | FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        try {
            threadpool.invokeAll(runnables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        threadpool.shutdown();
    }

    private static void planar_upperbound() {
        Solver[] solvers = {new UpperboundMetis(true, true), new UpperboundRandom(true), new UpperboundMetis(true, false)};
        for (Solver s : solvers) {
            try {
                runConfigurationPlanar(s, Executors.newSingleThreadExecutor());
            } catch (URISyntaxException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void runConfiguration(Solver solver, int nrVertices, double density, int nrPointsMultiplier, double resolutionPower, String extraIdentifier, ExecutorService executor) throws URISyntaxException, FileNotFoundException {
        int[] result = new int[100];
        double[] time = new double[100];

        for (int i = 0; i < result.length; i++) {
            System.out.println("Iteration " + (i + 1) + "/100");
            Graph graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(Experiments.class.getClassLoader().getResource("experiments/graphs/"+nrVertices+"_"+density+"_"+resolutionPower+"_"+nrPointsMultiplier+"/"+i+".json")).toURI()).toFile()));

            Solver solverTemp = solver.newEmptyInstance();
            solverTemp.setGraph(graph);
            Future<Double> future = executor.submit(solverTemp::solve);
            try {
                // Wait for the result with a timeout
                result[i] = future.get(30, TimeUnit.SECONDS).intValue();
                time[i] = solverTemp.getExecutionTime();
            } catch (TimeoutException e) {
                // Execution took longer than the specified timeout
                result[i] = (int) solverTemp.getOptimalCrossingNumber();
                time[i] = solverTemp.getExecutionTime();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                future.cancel(true);
            }
        }
        executor.shutdown();

        writeToFile(result, time, solver, nrVertices, density, resolutionPower, nrPointsMultiplier, extraIdentifier);
    }

    private static void runConfigurationUpperBound(UpperBoundSolver solver, int nrVertices, double density, int nrPointsMultiplier, double resolutionPower, int nrVerticesPerPartition, String extraIdentifier, ExecutorService executor) throws URISyntaxException, FileNotFoundException {
        int[] result = new int[100];
        double[] time = new double[100];

        for (int i = 0; i < result.length; i++ ) {
            System.out.println("Iteration " + (i+1) + "/100");
            Graph graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(Experiments.class.getClassLoader().getResource("experiments/graphs/"+nrVertices+"_"+density+"_"+resolutionPower+"_"+nrPointsMultiplier+"/"+i+".json")).toURI()).toFile()));

            UpperBoundSolver solverTemp = (UpperBoundSolver) solver.newEmptyInstance();
            solverTemp.setGraph(graph);
            if (nrVerticesPerPartition != -1) solverTemp.setNrVerticesPerPartition(nrVerticesPerPartition);

            Future<Double> future = executor.submit(solverTemp::solve);
            try {
                // Wait for the result with a timeout
                result[i] = future.get(30, TimeUnit.SECONDS).intValue();
                time[i] = solverTemp.getExecutionTime();
            } catch (TimeoutException e) {
                // Execution took longer than the specified timeout
                result[i] = (int) solverTemp.getOptimalCrossingNumber();
                time[i] = solverTemp.getExecutionTime();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                future.cancel(true);
            }
        }
        executor.shutdown();

        writeToFile(result, time, solver, nrVertices, density, resolutionPower, nrPointsMultiplier, nrVerticesPerPartition != -1 ? String.valueOf(nrVerticesPerPartition) : extraIdentifier);
    }

    private static void runConfigurationPlanar(Solver solver, ExecutorService executor) throws URISyntaxException, FileNotFoundException {
        int[] result = new int[5];
        double[] time = new double[5];

        for (int i = 0; i < result.length; i++ ) {
            System.out.println("Iteration " + (i+1) + "/5");
            Graph graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(Experiments.class.getClassLoader().getResource("experiments/graphs/planar/planar_"+i+".json")).toURI()).toFile()));

            Solver solverTemp = solver.newEmptyInstance();
            solverTemp.setGraph(graph);

            Future<Double> future = executor.submit(solverTemp::solve);
            try {
                // Wait for the result with a timeout
                result[i] = future.get(30, TimeUnit.SECONDS).intValue();
                time[i] = solverTemp.getExecutionTime();
            } catch (TimeoutException e) {
                // Execution took longer than the specified timeout
                result[i] = (int) solverTemp.getOptimalCrossingNumber();
                time[i] = solverTemp.getExecutionTime();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                future.cancel(true);
            }
        }
        executor.shutdown();

        writeToFile(result, time, solver);
    }
    private static void writeToFile(int[] result, double[] time, Solver solver) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/main/resources/experiments/results/planar_"+solver.getName()+".csv", true));
            bufferedWriter.write("result, " + "time");
            bufferedWriter.newLine();
            for (int i = 0; i < result.length; i++) {
                bufferedWriter.write(result[i] + "," + time[i]);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeToFile(int[] result, double[] time, Solver solver, int nrVertices, double density, double resolutionPower, int nrPointsMultiplier, String extraIdentifier) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/main/resources/experiments/results/"+nrVertices+"_"+density+"_"+resolutionPower+"_"+nrPointsMultiplier+"_"+extraIdentifier+"_"+solver.getName()+".csv", true));
            bufferedWriter.write("result, " + "time");
            bufferedWriter.newLine();
            for (int i = 0; i < result.length; i++) {
                bufferedWriter.write(result[i] + "," + time[i]);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
