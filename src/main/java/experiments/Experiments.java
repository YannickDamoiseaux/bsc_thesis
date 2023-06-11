package experiments;

import graph.Graph;
import jep.JepConfig;
import jep.MainInterpreter;
import jep.Run;
import jep.SharedInterpreter;
import solvers.*;
import solvers.upperbound.Upperbound;
import solvers.upperbound.UpperboundMetis;
import solvers.upperbound.UpperboundRandom;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Experiments {
    public static void main(String[] args) {
        exact_1_1();
        exact_1_2();
        exact_2_1();
        exact_2_2();
    }

    private static void test() {
        ExecutorService threadpool = Executors.newFixedThreadPool(2);
        Solver[] solvers = {new ExactPruning(), new ExactPruningRecursive()};
        int[] vertices = {8};
        double[] densities = {0.6};
        ArrayList<Callable<Object>> runnables = new ArrayList<>();
        for (int v : vertices) {
            for (double d : densities) {
                for (Solver s : solvers) {
                    runnables.add(() -> {
                        try {
                            runConfiguration(s, v, d, 3, 1.75, "TEST", Executors.newSingleThreadExecutor());
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

    private static void exact_1_1() {
        ExecutorService threadpool = Executors.newFixedThreadPool(3);
        Solver[] solvers = {new ExactPruning(), new ExactPruningNew()};
        int[] vertices = {5, 8, 11};
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

    private static void exact_1_2() {
    Solver[] solvers = {new ExactBIP()};
    //int[] vertices = {5, 8, 11};
    int[] vertices = {5};
    double[] densities = {0.6};//{0.3, 0.6};
    for (int v : vertices) {
        for (double d : densities) {
            for (Solver s : solvers) {
                    try {
                        runConfiguration(s, v, d, 3, 1.75, "", Executors.newSingleThreadExecutor());
                    } catch (URISyntaxException | FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void exact_2_1() {
        ExecutorService threadpool = Executors.newFixedThreadPool(3);
        Solver[] solvers = {new ExactPruning(), new ExactPruningNew()};
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

    private static void exact_2_2() {
        Solver[] solvers = {new ExactBIP()};
        double[] resolutionPowers = {1.25, 1.75};
        int[] nrPointsMultipliers = {1, 3};
        for (double r : resolutionPowers) {
            for (int p : nrPointsMultipliers) {
                for (Solver s : solvers) {
                    try {
                        runConfiguration(s, 8, 0.6, p, r, "", Executors.newSingleThreadExecutor());
                    } catch (URISyntaxException | FileNotFoundException e) {
                        throw new RuntimeException(e);

                    }
                }
            }
        }
    }

    private static void upperbound_1() {
        UpperBoundSolver[] solvers = {new UpperboundMetis(true, true), new UpperboundMetis(true, false)};
        int[] vertices = {20, 29};// 47, 65, 83, 101};
        int[] nrVerticesPerPartition = {4, 6, 8, 10};
        ArrayList<Callable<Object>> runnables = new ArrayList<>();
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



    private static void runConfiguration(Solver solver, int nrVertices, double density, int nrPointsMultiplier, double resolutionPower, String extraIdentifier, ExecutorService executor) throws URISyntaxException, FileNotFoundException {
        int[] result = new int[100];
        double[] time = new double[100];

        for (int i = 0; i < result.length; i++ ) {
            System.out.println("Iteration " + (i+1) + "/100");
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
            solverTemp.setNrVerticesPerPartition(nrVerticesPerPartition);

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

        writeToFile(result, time, solver, nrVertices, density, resolutionPower, nrPointsMultiplier, String.valueOf(nrVerticesPerPartition));
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
