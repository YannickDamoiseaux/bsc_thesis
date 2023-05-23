package solvers;

import graph.CrossingData;
import graph.Edge;
import graph.Graph;
import graph.Point;
import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SolverState;
import org.logicng.solvers.sat.MiniSatConfig;
import org.logicng.transformations.cnf.PlaistedGreenbaumTransformation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class LowerboundSAT implements Solver {
    private final Graph graph;
    private final Random rand = new Random(0);

    public LowerboundSAT(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    public double solve() {
        Literal[][] verticesToPointsAssigned = new Literal[graph.getNrOfVertices()][graph.getNrOfPoints()];
        FormulaFactory f = new FormulaFactory();
        MiniSatConfig config = MiniSatConfig.builder().proofGeneration(true).cnfMethod(MiniSatConfig.CNFMethod.PG_ON_SOLVER).build();
        MiniSat miniSat = MiniSat.miniSat(f, config);

        // Create literals/variables that keep track of to what point a vertex is assigned
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            for (int j = 0; j < graph.getNrOfPoints(); j++) {
                verticesToPointsAssigned[i][j] = f.literal("vp_"+i+"_"+j, true);
            }
        }

        // Create formulas that make sure that each vertex is assigned to exactly one point
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            ArrayList<Literal> literals = new ArrayList<>();
            for (int x = 0; x < graph.getNrOfPoints(); x++) {
                literals.add(verticesToPointsAssigned[i][x].negate());
            }

            ArrayList<Formula> formulas = new ArrayList<>();
            for (int j = 0; j < graph.getNrOfPoints(); j++) {
                Literal temp = literals.get(j);
                literals.set(j, j-1 >= 0 ? literals.get(j-1) : literals.get(j+1));
                formulas.add(f.and(f.and(literals),verticesToPointsAssigned[i][j]));
                literals.set(j, temp);
            }
            miniSat.add(f.or(formulas));
        }

        // Create formulas to make sure that each point is assigned to at most one vertex, i.e. it can also have no vertex assigned
        for (int j = 0; j < graph.getNrOfPoints(); j++) {
            ArrayList<Literal> literals = new ArrayList<>();
            for (int x = 0; x < graph.getNrOfVertices(); x++) {
                literals.add(verticesToPointsAssigned[x][j].negate());
            }

            ArrayList<Formula> formulas = new ArrayList<>();
            for (int i = 0; i < graph.getNrOfVertices(); i++) {
                Literal temp = literals.get(i);
                literals.set(i, i-1 >= 0 ? literals.get(i-1) : literals.get(i+1));
                formulas.add(f.and(f.and(literals), verticesToPointsAssigned[i][j]));
                literals.set(i, temp);
            }

            miniSat.add(f.or(f.or(formulas), f.and(literals)));
        }

        List<CrossingData>[] crossingAndColinear = Utils.getCrossings(graph);
        List<CrossingData> crossings = crossingAndColinear[0];
        List<CrossingData> colinear = crossingAndColinear[1];

        for (CrossingData colinearity : colinear) {
            miniSat.add(f.equivalence(f.and(verticesToPointsAssigned[graph.getEdges()[colinearity.e1()].v1()][colinearity.p1()], verticesToPointsAssigned[graph.getEdges()[colinearity.e1()].v2()][colinearity.p2()],
                    verticesToPointsAssigned[graph.getEdges()[colinearity.e2()].v1()][colinearity.p3()], verticesToPointsAssigned[graph.getEdges()[colinearity.e2()].v2()][colinearity.p4()]), f.constant(false)));
        }

        SolverState initialState = miniSat.saveState();

        int blockSize = 4000;
        int nrOfDivisions = crossings.size()/blockSize;

        int lastIndex = 0;
        int crossingNumber;
        boolean possible = true;
        for (crossingNumber = 0; crossingNumber < 30; crossingNumber++) {
            System.out.println("***** CROSSING NUMBER = " + crossingNumber + " ****");
            List<CrossingData> crossingsTemp = new ArrayList<>(crossings);
            //miniSat.loadState(initialState);
            //possible = true;

            /*int currentLiteralIdx = 0;
            Literal[][] crossingLiterals = new Literal[crossings.size()][crossingNumber];

            for (int iteration = 0; iteration < lastIndex; iteration++) {
                System.out.println("Addding " + iteration + "/"+lastIndex);
                ArrayList<CrossingData> crossingsDivision = new ArrayList<>();
                crossingsDivision.addAll(crossings.subList(blockSize * iteration, blockSize * (iteration + 1)));

                currentLiteralIdx = addCrossingClauses(miniSat, f, verticesToPointsAssigned, crossingLiterals, crossingsDivision, crossingNumber, currentLiteralIdx);
            }*/

            //SolverState tempState = miniSat.saveState();
            for (int iteration = lastIndex; iteration < nrOfDivisions; iteration++) {
                System.out.println("Iteration: " + iteration + "/" + nrOfDivisions);
                //miniSat.loadState(tempState);
                miniSat.loadState(initialState);

                //ArrayList<CrossingData> crossingsDivision = new ArrayList<>();
                //crossingsDivision.addAll(crossings.subList(blockSize * iteration, blockSize * (iteration + 1)));

                //currentLiteralIdx = addCrossingClauses(miniSat, f, verticesToPointsAssigned, crossingLiterals, crossingsDivision, crossingNumber, currentLiteralIdx);
                //tempState = miniSat.saveState();

                //addOneCrossingAtTimeClauses(miniSat, f, crossingLiterals, crossingNumber);

                //List<CrossingData> crossingDataSubList = crossings.subList(blockSize * iteration, blockSize * (iteration + 1));
                CrossingData[] crossingDataSubset = new CrossingData[blockSize];
                for (int i = 0; i < blockSize; i++) {
                    System.out.println(i + ", " + crossingsTemp.size());
                    int idx = rand.nextInt(0,  crossingsTemp.size());
                    crossingDataSubset[i] = crossingsTemp.get(idx);
                    crossingsTemp.remove(idx);
                }
                Literal[][] crossingLiterals = new Literal[crossingDataSubset.length][crossingNumber];
                for (int i = 0; i < crossingDataSubset.length; i++) {
                    CrossingData crossing = crossingDataSubset[i];
                    for (int j = 0; j < crossingNumber; j++) {
                        crossingLiterals[i][j] = f.literal("c_" + i + "_" + j, true);
                    }
                    miniSat.add(f.equivalence(f.and(verticesToPointsAssigned[graph.getEdges()[crossing.e1()].v1()][crossing.p1()], verticesToPointsAssigned[graph.getEdges()[crossing.e1()].v2()][crossing.p2()],
                            verticesToPointsAssigned[graph.getEdges()[crossing.e2()].v1()][crossing.p3()], verticesToPointsAssigned[graph.getEdges()[crossing.e2()].v2()][crossing.p4()]), f.or(crossingLiterals[i])));
                }

                for (int i = 0; i < crossingNumber; i++) {
                    ArrayList<Literal> literals = new ArrayList<>();

                    for (Literal[] crossingLiteral : crossingLiterals) {
                        literals.add(crossingLiteral[i].negate());
                    }

                    ArrayList<Formula> formulas = new ArrayList<>();
                    for (int j = 0; j < literals.size(); j++) {
                        System.out.println("Literal " + j + " / " + literals.size());
                        Literal temp = literals.get(j);
                        literals.set(j, j-1 >= 0 ? literals.get(j-1) : literals.get(j+1));
                        formulas.add(f.and(f.and(literals), f.not(temp)));
                        literals.set(j, temp);
                    }

                    miniSat.add(f.or(f.or(formulas), f.and(literals)));
                }

                System.out.println("Solving...");
                if (miniSat.sat() == Tristate.FALSE) {
                    System.out.println("Not possible to do it with " + crossingNumber + " crossing(s)");
                    possible = false;
                    lastIndex = iteration;
                    break;
                }
                //else System.out.println("SIZE " + miniSat.model().toString());
            }
            if (possible) break;
        }
        if (possible) {
            System.out.println("At least " + crossingNumber + " crossing(s) are needed.");
            return crossingNumber;
        }
        else {
            System.out.println("At least " + (crossingNumber+1) + " crossing(s) are needed.");
            return crossingNumber+1;
        }
    }

    /*private int addCrossingClauses(MiniSat miniSat, FormulaFactory f, Literal[][] verticesToPointsAssigned, Literal[][] crossingLiterals, List<CrossingData> crossingsDivision, int crossingNumber, int currentLiteralIdx) {
        // Make sure that if the positions of some points cause a crossing, one of the crossing variables is set to true.
        for (int i = 0; i < crossingsDivision.size(); i++) {
            CrossingData crossing = crossingsDivision.get(i);
            for (int j = 0; j < crossingNumber; j++) {
                crossingLiterals[currentLiteralIdx][j] = f.literal("c_" + currentLiteralIdx + "_" + j, true);
            }
            miniSat.add(f.equivalence(f.and(f.not(verticesToPointsAssigned[graph.getEdges()[crossing.e1()].v1()][crossing.p1()]), f.not(verticesToPointsAssigned[graph.getEdges()[crossing.e1()].v2()][crossing.p2()]),
                    f.not(verticesToPointsAssigned[graph.getEdges()[crossing.e2()].v1()][crossing.p3()]), f.not(verticesToPointsAssigned[graph.getEdges()[crossing.e2()].v2()][crossing.p4()])), f.or(crossingLiterals[i])));

            currentLiteralIdx++;
        }
        return currentLiteralIdx;
    }

    private void addOneCrossingAtTimeClauses(MiniSat miniSat, FormulaFactory f, Literal[][] crossingLiterals, int crossingNumber) {
        // Make sure that a crossing variable can only be set to true once. When testing if a graph can be drawn with 3 crossings, there are 3 crossing variables for each edge pair that can cause a crossing. Among all clauses defining a crossing only one of these
        // can be true at the same time.
        for (int i = 0; i < crossingNumber; i++) {
            ArrayList<Literal> literals = new ArrayList<>();

            for (Literal[] crossingLiteral : crossingLiterals) {
                if (crossingLiteral[i] == null) break;
                literals.add(crossingLiteral[i].negate());
            }

            ArrayList<Formula> formulas = new ArrayList<>();
            for (int j = 0; j < crossingLiterals.length; j++) {
                Literal temp = literals.get(j);
                literals.set(j, j - 1 >= 0 ? literals.get(j - 1) : literals.get(j + 1));
                formulas.add(f.and(f.and(literals), f.not(crossingLiterals[j][i])));
                literals.set(j, temp);
            }

            miniSat.add(f.or(f.or(formulas), f.and(literals)));
        }
    }*/
}
