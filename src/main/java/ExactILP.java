import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloQuadIntExpr;
import ilog.cplex.IloCplex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
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

            int M = 10000;
            Edge[] edges = graph.getEdges();
            LinkedList<IloIntVar> cVar = new LinkedList<>();
            area_vars = new IloIntVar[vertices.length][vertices.length];
            for (int i = 0; i < edges.length; i++) {
                Edge edge1 = edges[i];
                for (int j = i+1; j < edges.length; j++) {
                    Edge edge2 = edges[j];
                    if (edge2.v1() != edge1.v1() && edge2.v1() != edge1.v2() && edge2.v2() != edge1.v1() && edge2.v2() != edge1.v2()) {
                        IloIntVar c = cplex.boolVar();
                        IloIntVar t = cplex.boolVar();
                        IloIntVar p = cplex.boolVar();
                        IloLinearIntExpr area2_ijk = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge1.v1()][0], vertices[edge1.v1()][1]}, {vertices[edge1.v2()][0], vertices[edge1.v2()][1]},
                                {vertices[edge2.v1()][0], vertices[edge2.v1()][1]}}, new int[]{edge1.v1(), edge1.v2(), edge2.v1()}, false);

                        IloLinearIntExpr expr_1 = cplex.linearIntExpr();
                        expr_1.addTerms(new int[]{-M, M}, new IloIntVar[]{c, t});
                        expr_1.setConstant(M-1);
                        cplex.addLe(area2_ijk, expr_1);

                        IloLinearIntExpr area2_ijl_n = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge1.v1()][0], vertices[edge1.v1()][1]}, {vertices[edge1.v2()][0], vertices[edge1.v2()][1]},
                                {vertices[edge2.v2()][0], vertices[edge2.v2()][1]}}, new int[]{edge1.v1(), edge1.v2(), edge2.v2()}, true);
                        cplex.addLe(area2_ijl_n, expr_1);

                        IloLinearIntExpr area2_ijk_n = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge1.v1()][0], vertices[edge1.v1()][1]}, {vertices[edge1.v2()][0], vertices[edge1.v2()][1]},
                                {vertices[edge2.v1()][0], vertices[edge2.v1()][1]}}, new int[]{edge1.v1(), edge1.v2(), edge2.v1()}, true);
                        IloLinearIntExpr expr_2 = cplex.linearIntExpr();
                        expr_2.addTerms(new int[]{M, -M}, new IloIntVar[]{c, t});
                        expr_2.setConstant((2*M)-1);
                        cplex.addLe(area2_ijk_n, expr_2);

                        IloLinearIntExpr area2_ijl = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge1.v1()][0], vertices[edge1.v1()][1]}, {vertices[edge1.v2()][0], vertices[edge1.v2()][1]},
                                {vertices[edge2.v2()][0], vertices[edge2.v2()][1]}}, new int[]{edge1.v1(), edge1.v2(), edge2.v2()}, false);
                        cplex.addLe(area2_ijl, expr_2);

                        IloLinearIntExpr area2_kli = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge2.v1()][0], vertices[edge2.v1()][1]}, {vertices[edge2.v2()][0], vertices[edge2.v2()][1]},
                                {vertices[edge1.v1()][0], vertices[edge1.v1()][1]}}, new int[]{edge2.v1(), edge2.v2(), edge1.v1()}, false);
                        IloLinearIntExpr expr_3 = cplex.linearIntExpr();
                        expr_3.addTerms(new int[]{-M, M}, new IloIntVar[]{c, p});
                        expr_3.setConstant(M-1);
                        cplex.addLe(area2_kli, expr_3);

                        IloLinearIntExpr area2_klj_n = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge2.v1()][0], vertices[edge2.v1()][1]}, {vertices[edge2.v2()][0], vertices[edge2.v2()][1]},
                                {vertices[edge1.v2()][0], vertices[edge1.v2()][1]}}, new int[]{edge2.v1(), edge2.v2(), edge1.v2()}, true);
                        cplex.addLe(area2_klj_n, expr_3);

                        IloLinearIntExpr area2_kli_n = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge2.v1()][0], vertices[edge2.v1()][1]}, {vertices[edge2.v2()][0], vertices[edge2.v2()][1]},
                                {vertices[edge1.v1()][0], vertices[edge1.v1()][1]}}, new int[]{edge2.v1(), edge2.v2(), edge1.v1()}, true);
                        IloLinearIntExpr expr_4 = cplex.linearIntExpr();
                        expr_4.addTerms(new int[]{M, -M}, new IloIntVar[]{c, p});
                        expr_4.setConstant((2*M)-1);
                        cplex.addLe(area2_kli_n, expr_4);

                        IloLinearIntExpr area2_klj = getArea2Expr(cplex, new IloIntVar[][]{{vertices[edge2.v1()][0], vertices[edge2.v1()][1]}, {vertices[edge2.v2()][0], vertices[edge2.v2()][1]},
                                {vertices[edge1.v2()][0], vertices[edge1.v2()][1]}}, new int[]{edge2.v1(), edge2.v2(), edge1.v2()}, false);
                        cplex.addLe(area2_klj, expr_4);

                        IloLinearIntExpr expr_5 = cplex.linearIntExpr();
                        expr_5.addTerms(new int[]{M, M, M}, new IloIntVar[]{c, t, p});
                        expr_5.setConstant(-1);
                        cplex.addLe(area2_ijk, expr_5);
                        cplex.addLe(area2_ijl, expr_5);

                        IloLinearIntExpr expr_6 = cplex.linearIntExpr();
                        expr_6.addTerms(new int[]{M, -M, M}, new IloIntVar[]{c, t, p});
                        expr_6.setConstant(M-1);
                        cplex.addLe(area2_ijk_n, expr_6);
                        cplex.addLe(area2_ijl_n, expr_6);

                        IloLinearIntExpr expr_7 = cplex.linearIntExpr();
                        expr_7.addTerms(new int[]{M, M, -M}, new IloIntVar[]{c, t, p});
                        expr_7.setConstant(M-1);
                        cplex.addLe(area2_kli, expr_7);
                        cplex.addLe(area2_klj, expr_7);

                        IloLinearIntExpr expr_8 = cplex.linearIntExpr();
                        expr_8.addTerms(new int[]{M, -M, -M}, new IloIntVar[]{c, t, p});
                        expr_8.setConstant((2*M)-1);
                        cplex.addLe(area2_kli_n, expr_8);
                        cplex.addLe(area2_klj_n, expr_8);

                        cVar.add(c);
                    }
                }
            }

            cplex.addMinimize(cplex.sum(cVar.toArray(new IloIntVar[0])));
            System.out.println(cplex.getObjective());
            if (cplex.solve()) {
                System.out.println("Optimal value = " + cplex.getObjValue());
                System.out.println(Arrays.toString(cplex.getValues(vertices[0])));
                System.out.println(Arrays.toString(cplex.getValues(vertices[1])));
                System.out.println(Arrays.toString(cplex.getValues(vertices[2])));
                System.out.println(Arrays.toString(cplex.getValues(vertices[3])));
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private IloIntVar[][] area_vars;

    private IloLinearIntExpr getArea2Expr(IloCplex cplex, IloIntVar[][] vertices, int[] vertices_int, boolean negativeArea) throws IloException {
        int[] coeffs;
        if (!negativeArea) coeffs = new int[]{1, -1, 1, -1, 1, -1};
        else coeffs = new int[]{-1, 1, -1, 1, -1, 1};

        IloIntVar[] vars = new IloIntVar[6];

        if (area_vars[vertices_int[0]][vertices_int[1]] == null) area_vars[vertices_int[0]][vertices_int[1]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[0] = area_vars[vertices_int[0]][vertices_int[1]];
        IloQuadIntExpr expr_1 = cplex.quadIntExpr();
        expr_1.addTerm(1, vertices[0][0], vertices[1][1]);
        cplex.addEq(area_vars[vertices_int[0]][vertices_int[1]], expr_1);

        if (area_vars[vertices_int[1]][vertices_int[0]] == null) area_vars[vertices_int[1]][vertices_int[0]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[1] = area_vars[vertices_int[1]][vertices_int[0]];
        IloQuadIntExpr expr_2 = cplex.quadIntExpr();
        expr_2.addTerm(1,vertices[1][0], vertices[0][1]);
        cplex.addEq(area_vars[vertices_int[1]][vertices_int[0]], expr_2);

        if (area_vars[vertices_int[2]][vertices_int[0]] == null) area_vars[vertices_int[2]][vertices_int[0]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[2] = area_vars[vertices_int[2]][vertices_int[0]];
        IloQuadIntExpr expr_3 = cplex.quadIntExpr();
        expr_3.addTerm(1, vertices[2][0], vertices[0][1]);
        cplex.addEq(area_vars[vertices_int[2]][vertices_int[0]], expr_3);

        if (area_vars[vertices_int[0]][vertices_int[2]] == null) area_vars[vertices_int[0]][vertices_int[2]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[3] = area_vars[vertices_int[0]][vertices_int[2]];
        IloQuadIntExpr expr_4 = cplex.quadIntExpr();
        expr_4.addTerm(1, vertices[0][0], vertices[2][1]);
        cplex.addEq(area_vars[vertices_int[0]][vertices_int[2]], expr_4);

        if (area_vars[vertices_int[1]][vertices_int[2]] == null) area_vars[vertices_int[1]][vertices_int[2]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[4] = area_vars[vertices_int[1]][vertices_int[2]];
        IloQuadIntExpr expr_5 = cplex.quadIntExpr();
        expr_5.addTerm(1, vertices[1][0], vertices[2][1]);
        cplex.addEq(area_vars[vertices_int[1]][vertices_int[2]], expr_5);

        if (area_vars[vertices_int[2]][vertices_int[1]] == null) area_vars[vertices_int[2]][vertices_int[1]] = cplex.intVar(0, graph.getWidth()*graph.getWidth());
        vars[5] = area_vars[vertices_int[2]][vertices_int[1]];
        IloQuadIntExpr expr_6 = cplex.quadIntExpr();
        expr_6.addTerm(1, vertices[2][0], vertices[1][1]);
        cplex.addEq(area_vars[vertices_int[2]][vertices_int[1]], expr_6);

        IloLinearIntExpr expr_linear = cplex.linearIntExpr();
        expr_linear.addTerms(coeffs, vars);

        return expr_linear;
    }
}
