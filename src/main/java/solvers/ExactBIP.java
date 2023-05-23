package solvers;

import graph.CrossingData;
import graph.Edge;
import graph.Graph;
import graph.Point;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import jdk.jshell.execution.Util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ExactBIP implements Solver {
    private final Graph graph;

    public ExactBIP(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
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

                            //IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                            //expr_2.addTerm(1, var);
                            cplex.addLe(expr_1, vars[edge.v2()][j]);

                            //IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                            //expr_3.addTerm(1, var);
                            IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                            expr_4.addTerm(1, vars[edge.v1()][i]);
                            expr_4.addTerm(1, vars[edge.v2()][j]);
                            expr_4.setConstant(-1);
                            cplex.addGe(expr_1, expr_4);

                            edgePointCombination[e][i][j] = var;
                        }
                    }
                }
            }

            LinkedList<IloLinearIntExpr> objExpressions = new LinkedList<>();
            List<CrossingData>[] crossingAndColinear = Utils.getCrossings(graph);
            List<CrossingData> crossings = crossingAndColinear[0];
            List<CrossingData> colinear = crossingAndColinear[1];

            for (CrossingData crossing : crossings) {
                IloIntVar var = cplex.intVar(0, 1, "x_" + crossing.e1() + "_" + crossing.p1() + "_" + crossing.p2()
                        + crossing.e2() + "_" + crossing.p3() + "_" + crossing.p4());

                IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                expr_1.addTerm(1, var);
                cplex.addLe(expr_1, edgePointCombination[crossing.e1()][crossing.p1()][crossing.p2()]);

                //IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                //expr_2.addTerm(1, var);
                cplex.addLe(expr_1, edgePointCombination[crossing.e2()][crossing.p3()][crossing.p4()]);

                //IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                //expr_3.addTerm(1, var);
                IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                expr_4.addTerm(1, edgePointCombination[crossing.e1()][crossing.p1()][crossing.p2()]);
                expr_4.addTerm(1, edgePointCombination[crossing.e2()][crossing.p3()][crossing.p4()]);
                expr_4.setConstant(-1);
                cplex.addGe(expr_1, expr_4);

                IloLinearIntExpr obj_expr = cplex.linearIntExpr();
                obj_expr.addTerm(1, var);
                objExpressions.add(obj_expr);
            }

            for (CrossingData colinearity : colinear) {
                IloIntVar var1 = edgePointCombination[colinearity.e1()][colinearity.p1()][colinearity.p2()];
                IloIntVar var2 = edgePointCombination[colinearity.e2()][colinearity.p3()][colinearity.p4()];
                IloIntVar new_var = cplex.intVar(0, 1);

                IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                expr_1.addTerm(1, new_var);
                IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                expr_2.addTerm(1, var1);
                cplex.addLe(expr_1, expr_2);

                IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                expr_3.addTerm(1, var2);
                cplex.addLe(expr_1, expr_3);

                IloLinearIntExpr expr_5 = cplex.linearIntExpr();
                expr_5.addTerm(1, var1);
                expr_5.addTerm(1, var2);
                expr_5.setConstant(-1);
                cplex.addGe(expr_1, expr_5);

                cplex.addEq(0, new_var);
            }

            cplex.addMinimize(cplex.sum(objExpressions.toArray(new IloLinearIntExpr[0])));
            // solve and retrieve optimal solution
            if (cplex.solve()) {
                return cplex.getObjValue();
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }
}
