import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;
import jdk.jshell.execution.Util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class ExactILP implements Solver {
    private final Graph graph;

    public ExactILP(String src) throws URISyntaxException, FileNotFoundException {
        this.graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(ExactBIP.class.getClassLoader().getResource(src)).toURI()).toFile()));
    }
    public void solve() {
        try (IloCplex cplex = new IloCplex()) {
            IloIntVar[][][] points = new IloIntVar[graph.getNrOfVertices()][graph.getNrOfPoints()][2];
            IloIntVar[][] vertices = new IloIntVar[graph.getNrOfVertices()][2];
            for (int i = 0; i < graph.getNrOfVertices(); i++) {
                IloLinearIntExpr expr_x = cplex.linearIntExpr();
                IloLinearIntExpr expr_y = cplex.linearIntExpr();
                IloLinearIntExpr expr_x_sum = cplex.linearIntExpr();
                IloLinearIntExpr expr_y_sum = cplex.linearIntExpr();
                for (int j = 0; j < graph.getNrOfPoints(); j++) {
                    points[i][j][0] = cplex.boolVar("px_" + i + "_" + j);
                    points[i][j][1] = cplex.boolVar("py_" + i + "_" + j);
                    expr_x.addTerm(graph.getPoints()[j].x(), points[i][j][0]);
                    expr_y.addTerm(graph.getPoints()[j].y(), points[i][j][1]);
                    expr_x_sum.addTerm(1, points[i][j][0]);
                    expr_y_sum.addTerm(1, points[i][j][1]);

                    cplex.addEq(points[i][j][0], points[i][j][1]);
                }
                vertices[i][0] = cplex.intVar(0, graph.getWidth()-1, "x_"+i);
                vertices[i][1] = cplex.intVar(0, graph.getHeight()-1,"y_"+i);
                cplex.addEq(vertices[i][0], expr_x);
                cplex.addEq(vertices[i][1], expr_y);
                cplex.addEq(1, expr_x_sum);
                cplex.addEq(1, expr_y_sum);
            }
            for (int i = 0; i < graph.getNrOfPoints(); i++) {
                IloLinearIntExpr expr_point_x_sum = cplex.linearIntExpr();
                IloLinearIntExpr expr_point_y_sum = cplex.linearIntExpr();
                for (int j = 0; j < graph.getNrOfVertices(); j++) {
                    expr_point_x_sum.addTerm(1, points[j][i][0]);
                    expr_point_y_sum.addTerm(1, points[j][i][1]);
                }
                cplex.addLe(1, expr_point_x_sum);
                cplex.addLe(1, expr_point_y_sum);
            }



        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
