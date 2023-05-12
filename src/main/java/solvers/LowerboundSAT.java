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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class LowerboundSAT implements Solver {
    private final Graph graph;

    public LowerboundSAT(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
        System.out.println("nr of vertices: " + graph.getNrOfVertices() + ", nr of points: " + graph.getNrOfPoints() + ", nr of edges: " + graph.getNrOfEdges());
    }

    public double solve() {
        Literal[][] verticesToPointsAssigned = new Literal[graph.getNrOfVertices()][graph.getNrOfPoints()];
        FormulaFactory f = new FormulaFactory();
        MiniSatConfig config = MiniSatConfig.builder().proofGeneration(true).build();
        MiniSat miniSat = MiniSat.miniCard(f, config);

        // Create literals/variables that keep track of to what point a vertex is assigned
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            for (int j = 0; j < graph.getNrOfPoints(); j++) {
                verticesToPointsAssigned[i][j] = f.literal("vp_"+i+"_"+j, false);
            }
        }

        // Create formulas that make sure that each vertex is assigned to exactly one point
        for (int i = 0; i < graph.getNrOfVertices(); i++) {
            ArrayList<Literal> literals = new ArrayList<>();
            for (int x = 0; x < graph.getNrOfPoints(); x++) {
                literals.add(verticesToPointsAssigned[i][x]);
            }

            ArrayList<Formula> formulas = new ArrayList<>();
            for (int j = 0; j < graph.getNrOfPoints(); j++) {
                Literal temp = literals.get(j);
                literals.set(j, j-1 >= 0 ? literals.get(j-1) : literals.get(j+1));
                formulas.add(f.and(f.and(literals), f.not(verticesToPointsAssigned[i][j])));
                literals.set(j, temp);
            }
            miniSat.add(f.or(formulas));
        }

        // Create formulas to make sure that each point is assigned to at most one vertex, i.e. it can also have no vertex assigned
        for (int j = 0; j < graph.getNrOfPoints(); j++) {
            ArrayList<Literal> literals = new ArrayList<>();
            for (int x = 0; x < graph.getNrOfVertices(); x++) {
                literals.add(verticesToPointsAssigned[x][j]);
            }

            ArrayList<Formula> formulas = new ArrayList<>();
            for (int i = 0; i < graph.getNrOfVertices(); i++) {
                Literal temp = literals.get(i);
                literals.set(i, i-1 >= 0 ? literals.get(i-1) : literals.get(i+1));
                formulas.add(f.and(f.and(literals), f.not(verticesToPointsAssigned[i][j])));
                literals.set(i, temp);
            }

            miniSat.add(f.or(f.or(formulas), f.and(literals)));
        }

        SolverState initialState = miniSat.saveState();
        ArrayList<CrossingData>[] crossings = getCrossings();

        int blockSize = 10000;
        //int nrOfDivisions = crossings.size()/blockSize;
        //System.out.println(crossings.size() + ", " + blockSize + ", " + nrOfDivisions);
        int[] blockSizePerVertex = new int[graph.getNrOfVertices()];
        int sum = 0;
        for (int i = 0; i < blockSizePerVertex.length; i++) {
            sum += crossings[i].size();
        }
        for (int i = 0; i < blockSizePerVertex.length; i++) {
            blockSizePerVertex[i] = (int)(((double)crossings[i].size()/sum)*blockSize);
        }
        int nrOfDivisions = sum/blockSize;

        int lastIndex = 0;
        int crossingNumber;
        boolean possible = true;
        for (crossingNumber = 0; crossingNumber < 10; crossingNumber++) {
            System.out.println("***** CROSSING NUMBER = " + crossingNumber + " ****");
            miniSat.loadState(initialState);
            possible = true;

            int currentLiteralIdx = 0;
            Literal[][] crossingLiterals = new Literal[sum][crossingNumber];

            for (int iteration = 0; iteration < lastIndex; iteration++) {
                System.out.println("Addding " + iteration + "/"+lastIndex);
                ArrayList<CrossingData> crossingsDivision = new ArrayList<>();
                for (int i = 0; i < blockSizePerVertex.length; i++) {
                    crossingsDivision.addAll(crossings[i].subList(blockSizePerVertex[i] * iteration, blockSizePerVertex[i] * (iteration + 1)));
                }
                //List<Solvers.CrossingData> crossingsDivision = crossings.subList(blockSize * iteration, blockSize * (iteration + 1));

                currentLiteralIdx = addCrossingClauses(miniSat, f, verticesToPointsAssigned, crossingLiterals, crossingsDivision, crossingNumber, currentLiteralIdx);
            }

            SolverState tempState = miniSat.saveState();
            for (int iteration = lastIndex; iteration < nrOfDivisions; iteration++) {
                System.out.println("Iteration: " + iteration + "/" + nrOfDivisions);
                miniSat.loadState(tempState);
                ArrayList<CrossingData> crossingsDivision = new ArrayList<>();
                for (int i = 0; i < blockSizePerVertex.length; i++) {
                    crossingsDivision.addAll(crossings[i].subList(blockSizePerVertex[i] * iteration, blockSizePerVertex[i] * (iteration + 1)));
                }
                //List<Solvers.CrossingData> crossingsDivision = crossings.subList(blockSize * iteration, blockSize * (iteration + 1));

                currentLiteralIdx = addCrossingClauses(miniSat, f, verticesToPointsAssigned, crossingLiterals, crossingsDivision, crossingNumber, currentLiteralIdx);
                tempState = miniSat.saveState();

                addOneCrossingAtTimeClauses(miniSat, f, crossingLiterals, crossingNumber);

                if (miniSat.sat() == Tristate.FALSE) {
                    System.out.println("Not possible to do it with " + crossingNumber + " crossing(s)");
                    possible = false;
                    lastIndex = iteration;
                    break;
                }
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

    private int addCrossingClauses(MiniSat miniSat, FormulaFactory f, Literal[][] verticesToPointsAssigned, Literal[][] crossingLiterals, List<CrossingData> crossingsDivision, int crossingNumber, int currentLiteralIdx) {
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
    }

    private ArrayList<CrossingData>[] getCrossings() {
        ArrayList<CrossingData>[] crossings = new ArrayList[graph.getNrOfVertices()];
        for (int i = 0; i < crossings.length; i++) {
            crossings[i] = new ArrayList<>();
        }
        Point[] points = graph.getPoints();

        for (int e_1 = 0; e_1 < graph.getNrOfEdges(); e_1++) {
            Edge edge_1 = graph.getEdges()[e_1];
            for (int i_1 = 0; i_1 < graph.getNrOfPoints(); i_1++) {
                for (int j_1 = 0; j_1 < graph.getNrOfPoints(); j_1++) {
                    if (i_1 != j_1) {
                        for (int e_2 = e_1 + 1; e_2 < graph.getNrOfEdges(); e_2++) {
                            Edge edge_2 = graph.getEdges()[e_2];
                            if (edge_2.v1() != edge_1.v1() && edge_2.v1() != edge_1.v2() && edge_2.v2() != edge_1.v1() && edge_2.v2() != edge_1.v2()) {
                                for (int i_2 = 0; i_2 < graph.getNrOfPoints(); i_2++) {
                                    if (i_2 != i_1 && i_2 != j_1) {
                                        for (int j_2 = 0; j_2 < graph.getNrOfPoints(); j_2++) {
                                            if (j_2 != i_1 && j_2 != j_1 && j_2 != i_2) {
                                                if (Utils.doEdgesCross(points[i_1].x(), points[i_1].y(), points[j_1].x(), points[j_1].y(), points[i_2].x(), points[i_2].y(), points[j_2].x(), points[j_2].y())) {
                                                    CrossingData crossing = new CrossingData(e_1, i_1, j_1, e_2, i_2, j_2);
                                                    crossings[edge_1.v1()].add(crossing);
                                                    crossings[edge_1.v2()].add(crossing);
                                                    crossings[edge_2.v1()].add(crossing);
                                                    crossings[edge_2.v2()].add(crossing);
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

        return crossings;
    }
}
