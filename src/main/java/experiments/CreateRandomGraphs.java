package experiments;

import graph.Graph;
import graph.GraphGenerator;

public class CreateRandomGraphs {
    public static void main(String[] args) {
        int nrVertices = 110;
        double density = 0.6;
        double resolutionPower = 1.75;
        int nrPointsMultiplier = 3;
        int size = (int) Math.pow(nrVertices, resolutionPower);
        GraphGenerator graphGenerator = new GraphGenerator();
        for (int i = 0; i < 100; i++) {
            Graph graph = graphGenerator.generateGraph(nrVertices, (int) (density * (((double) nrVertices * (nrVertices - 1)) / 2)), nrPointsMultiplier * nrVertices, size, size);
            graph.saveToFile("experiments/graphs/"+nrVertices+"_"+density+"_"+resolutionPower+"_"+nrPointsMultiplier+"/"+i+".json");
        }
    }
}
