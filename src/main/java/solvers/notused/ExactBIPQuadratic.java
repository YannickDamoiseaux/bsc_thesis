package solvers.notused;

import graph.Edge;
import graph.Graph;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import solvers.ExactBLP;
import solvers.Solver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;

public class ExactBIPQuadratic extends Solver {
    private final Graph graph;

    public ExactBIPQuadratic(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBLP.class.getClassLoader().getResource(src)).toURI()).toFile()));
    }

    public double solve() {
        try(IloCplex cplex = new IloCplex()) {
            IloIntVar[][] vars = new IloIntVar[graph.getNrOfVertices()][graph.getNrOfPoints()];
            for (int i = 0; i < graph.getNrOfVertices(); i++) {
                IloLinearIntExpr expr = cplex.linearIntExpr();
                for (int j = 0; j < graph.getNrOfPoints(); j++) {
                    vars[i][j] = cplex.intVar(0, 1, "x_" + i + "_" + j);
                    expr.addTerm(1, vars[i][j]);
                }
                cplex.addEq(expr, 1);
            }
            for (int i = 0; i < graph.getNrOfPoints(); i++) {
                IloLinearIntExpr expr = cplex.linearIntExpr();
                for (int j = 0; j < graph.getNrOfVertices(); j++) {
                    expr.addTerm(1, vars[j][i]);
                }
                cplex.addLe(expr, 1);
            }


            //IloLinearIntExpr[] objExpressions = new IloLinearIntExpr[((int)(graph.getNrOfEdges() * graph.getNrOfPoints() * (graph.getNrOfPoints()-1) * (0.5*(graph.getNrOfEdges()-1)))) * (graph.getNrOfPoints()-2) * (graph.getNrOfPoints()-3)];
            LinkedList<IloQuadIntExpr> objExpressions = new LinkedList<>();
            boolean[][][][][][] crossings = getCrossings();
            IloIntVar[][][][] edgePointCombination = new IloIntVar[graph.getNrOfVertices()][graph.getNrOfPoints()][graph.getNrOfVertices()][graph.getNrOfPoints()];
            int count = 0;
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
                                                    if (crossings[e_1][i_1][j_1][e_2][i_2][j_2]) { // Only add objective expressions that have a crossing as result, otherwise no point in adding it
                                                        IloIntVar var_1 = edgePointCombination[edge_1.v1()][i_1][edge_1.v2()][j_1];
                                                        if (var_1 == null) {
                                                            IloQuadIntExpr expr_1 = cplex.quadIntExpr();
                                                            expr_1.addTerm(1, vars[edge_1.v1()][i_1], vars[edge_1.v2()][j_1]);
                                                            var_1 = cplex.boolVar();
                                                            cplex.addEq(var_1, expr_1);
                                                        }
                                                        IloIntVar var_2 = edgePointCombination[edge_2.v1()][i_2][edge_2.v2()][j_2];
                                                        if (var_2 == null) {
                                                            IloQuadIntExpr expr_2 = cplex.quadIntExpr();
                                                            expr_2.addTerm(1, vars[edge_2.v1()][i_2], vars[edge_2.v2()][j_2]);
                                                            var_2 = cplex.boolVar();
                                                            cplex.addEq(var_2, expr_2);
                                                        }

                                                        IloQuadIntExpr expr_3 = cplex.quadIntExpr();
                                                        expr_3.addTerm(1, var_1, var_2);

                                                        objExpressions.add(expr_3);
                                                        count++;
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

            System.out.println("Nr of objective constraints/expressions: " + count);

            //cplex.addMinimize(cplex.sum(objExpressions));
            cplex.addMinimize(cplex.sum(objExpressions.toArray(new IloQuadIntExpr[0])));
            // solve and retrieve optimal solution
            if (cplex.solve()) {
                return cplex.getObjValue();
            }

        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Solver newEmptyInstance() {
        return null;
    }

    @Override
    public double getOptimalCrossingNumber() {
        return 0;
    }

    private boolean[][][][][][] getCrossings() {
        /*
        boolean[][][][][][] crossings = new boolean[graph.getNrOfEdges()][graph.getNrOfPoints()][graph.getNrOfPoints()][graph.getNrOfEdges()][graph.getNrOfPoints()][graph.getNrOfPoints()];
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
                                                crossings[e_1][i_1][j_1][e_2][i_2][j_2] = Utils.doEdgesCross(points[i_1].x(), points[i_1].y(), points[j_1].x(), points[j_1].y(), points[i_2].x(), points[i_2].y(), points[j_2].x(), points[j_2].y());
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
         */
        return null;
    }
}
