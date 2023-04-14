import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class ExactILP {
    private final Graph graph;

    public ExactILP(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactILP.class.getClassLoader().getResource(src)).toURI()).toFile()));
    }

    public void solve() {
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
                cplex.addEq(expr, 1);
            }

            IloIntVar[][][] edgePointCombination = new IloIntVar[graph.getNrOfEdges()][graph.getNrOfPoints()][graph.getNrOfPoints()];
            for (int e = 0; e < graph.getNrOfEdges(); e++) {
                Edge edge = graph.getEdges()[e];
                for (int i = 0; i < graph.getNrOfPoints(); i++) {
                    for (int j = 0; j < graph.getNrOfPoints(); j++) {
                        if (i != j) {
                            IloIntVar var = cplex.intVar(0, 1, "x_" + edge.v1() + "_" + i + "_" + edge.v2() + "_" + j);

                            IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                            expr_1.addTerm(1, var);
                            cplex.addLe(expr_1, vars[edge.v1()][i]);

                            IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                            expr_2.addTerm(1, var);
                            cplex.addLe(expr_2, vars[edge.v2()][j]);

                            IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                            expr_3.addTerm(1, var);
                            IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                            expr_4.addTerm(1, vars[edge.v1()][i]);
                            expr_4.addTerm(1, vars[edge.v2()][j]);
                            expr_4.setConstant(-1);
                            cplex.addGe(expr_3, expr_4);

                            edgePointCombination[e][i][j] = var;
                        }
                    }
                }
            }

            IloLinearIntExpr[] objExpressions = new IloLinearIntExpr[((int)(graph.getNrOfEdges() * graph.getNrOfPoints() * (graph.getNrOfPoints()-1) * (0.5*(graph.getNrOfEdges()-1)))) * (graph.getNrOfPoints()-2) * (graph.getNrOfPoints()-3)];

            boolean[][][][][][] crossings = getCrossings();
            int count = 0;
            for (int e_1 = 0; e_1 < graph.getNrOfEdges(); e_1++) {
                Edge edge_1 = graph.getEdges()[e_1];
                for (int i_1 = 0; i_1 < graph.getNrOfPoints(); i_1++) {
                    for (int j_1 = 0; j_1 < graph.getNrOfPoints(); j_1++) {
                        if (i_1 != j_1) {
                            for (int e_2 = e_1+1; e_2 < graph.getNrOfEdges(); e_2++) {
                                Edge edge_2 = graph.getEdges()[e_2];
                                for (int i_2 = 0; i_2 < graph.getNrOfPoints(); i_2++) {
                                    if (i_2 != i_1 && i_2 != j_1) {
                                        for (int j_2 = 0; j_2 < graph.getNrOfPoints(); j_2++) {
                                            if (j_2 != i_1 && j_2 != j_1 && j_2 != i_2) {
                                                IloIntVar var = cplex.intVar(0, 1, "x_" + edge_1.v1() + "_" + i_1 + "_" + edge_1.v2() + "_" + j_1
                                                        + edge_2.v1() + "_" + i_2 + "_" + edge_2.v2() + "_" + j_2);

                                                IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                                                expr_1.addTerm(1, var);
                                                cplex.addLe(expr_1, edgePointCombination[e_1][i_1][j_1]);

                                                IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                                                expr_2.addTerm(1, var);
                                                cplex.addLe(expr_2, edgePointCombination[e_2][i_2][j_2]);

                                                IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                                                expr_3.addTerm(1, var);
                                                IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                                                expr_4.addTerm(1, edgePointCombination[e_1][i_1][j_1]);
                                                expr_4.addTerm(1, edgePointCombination[e_2][i_2][j_2]);
                                                expr_4.setConstant(-1);
                                                cplex.addGe(expr_3, expr_4);

                                                IloLinearIntExpr obj_expr = cplex.linearIntExpr();
                                                obj_expr.addTerm(crossings[e_1][i_1][j_1][e_2][i_2][j_2] ? 1 : 0, var);
                                                objExpressions[count] = obj_expr;
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
            System.out.println("Nr of objective constraints/expressions: " + count);

            cplex.addMinimize(cplex.sum(objExpressions));
            // solve and retrieve optimal solution
            if (cplex.solve()) {
                System.out.println("Optimal value = " + cplex.getObjValue());
                System.out.println(Arrays.toString(cplex.getValues(vars[0])));
                System.out.println(Arrays.toString(cplex.getValues(vars[1])));
                System.out.println(Arrays.toString(cplex.getValues(vars[2])));
                System.out.println(Arrays.toString(cplex.getValues(vars[3])));
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private boolean[][][][][][] getCrossings() {
        boolean[][][][][][] crossings = new boolean[graph.getNrOfEdges()][graph.getNrOfPoints()][graph.getNrOfPoints()][graph.getNrOfEdges()][graph.getNrOfPoints()][graph.getNrOfPoints()];
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
                                            crossings[e_1][i_1][j_1][e_2][i_2][j_2] = doEdgesCross(points[i_1].x(), points[i_1].y(), points[j_1].x(), points[j_1].y(), points[i_2].x(), points[i_2].y(), points[j_2].x(), points[j_2].y());
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

    public boolean doEdgesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1 = determinant(x1, y1, x2, y2, x3, y3);
        double det2 = determinant(x1, y1, x2, y2, x4, y4);
        double det3 = determinant(x3, y3, x4, y4, x1, y1);
        double det4 = determinant(x3, y3, x4, y4, x2, y2);
        return (det1 > 0 && det2 < 0 || det1 < 0 && det2 > 0) && (det3 > 0 && det4 < 0 || det3 < 0 && det4 > 0);
    }

    private double determinant(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }
}
