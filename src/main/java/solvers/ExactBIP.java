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
import java.util.*;

public class ExactBIP extends Solver {
    public ExactBIP(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
    }
    public ExactBIP() {}

    //private static IloCplex cplex;

    public double solve() {
        try (IloCplex cplex = new IloCplex()) {
            cplex.setParam(IloCplex.Param.Threads, 1);
            //cplex.setOut(null);
            long startTime = System.nanoTime();
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
                if (Thread.currentThread().isInterrupted()) {
                    return Integer.MAX_VALUE;
                }
                IloIntVar var = cplex.intVar(0, 1, "x_" + crossing.e1() + "_" + crossing.p1() + "_" + crossing.p2()
                        + crossing.e2() + "_" + crossing.p3() + "_" + crossing.p4());

                IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                expr_1.addTerm(1, var);
                cplex.addLe(expr_1, edgePointCombination[crossing.e1()][crossing.p1()][crossing.p2()]);

                cplex.addLe(expr_1, edgePointCombination[crossing.e2()][crossing.p3()][crossing.p4()]);

                IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                expr_4.addTerm(1, edgePointCombination[crossing.e1()][crossing.p1()][crossing.p2()]);
                expr_4.addTerm(1, edgePointCombination[crossing.e2()][crossing.p3()][crossing.p4()]);
                expr_4.setConstant(-1);
                cplex.addGe(expr_1, expr_4);

                IloLinearIntExpr obj_expr = cplex.linearIntExpr();
                obj_expr.addTerm(1, var);
                objExpressions.add(obj_expr);
            }

            for (CrossingData collinearity : colinear) {
                if (Thread.currentThread().isInterrupted()) {
                    return Integer.MAX_VALUE;
                }
                IloIntVar var1;
                if (collinearity.p1() == -1) {
                    var1 = vars[graph.getEdges()[collinearity.e1()].v2()][collinearity.p2()];
                }
                else if (collinearity.p2() == -1) {
                    var1 = vars[graph.getEdges()[collinearity.e1()].v1()][collinearity.p1()];
                }
                else {
                    var1 = edgePointCombination[collinearity.e1()][collinearity.p1()][collinearity.p2()];
                }

                IloIntVar var2;
                if (collinearity.p3() == -1) {
                    var2 = vars[graph.getEdges()[collinearity.e2()].v2()][collinearity.p4()];
                }
                else if (collinearity.p4() == -1) {
                    var2 = vars[graph.getEdges()[collinearity.e2()].v1()][collinearity.p3()];
                }
                else {
                    var2 = edgePointCombination[collinearity.e2()][collinearity.p3()][collinearity.p4()];
                }
                //System.out.println(var1 + ", " + var2);

                //IloIntVar var1 = edgePointCombination[collinearity.e1()][collinearity.p1()][collinearity.p2()];
                //IloIntVar var2 = edgePointCombination[collinearity.e2()][collinearity.p3()][collinearity.p4()];
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
            long stopTime = System.nanoTime();
            int timeLeft = (int) (30000-((stopTime - startTime)/1000000));
            if (timeLeft <= 0) {
                return Integer.MAX_VALUE;
            }
            //cplex.setParam(IloCplex.Param.TimeLimit, timeLeft/1000.0);
            // solve and retrieve optimal solution
            if (cplex.solve()) {
                return cplex.getObjValue();
            }
            else {
                return cplex.getObjValue();
            }
        } catch (IloException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Solver newEmptyInstance() {
        return new ExactBIP();
    }

    @Override
    public double getOptimalCrossingNumber() {
        return Integer.MAX_VALUE;
    }
}
