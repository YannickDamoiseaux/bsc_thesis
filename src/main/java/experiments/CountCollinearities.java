package experiments;

import graph.CrossingData;
import graph.Graph;
import solvers.ExactBIP;
import solvers.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class CountCollinearities {
    public static void main(String[] args) throws URISyntaxException, FileNotFoundException {
        String[] graphs = {"8_0.6_1.25_1", "8_0.6_1.75_1", "8_0.6_1.25_3", "8_0.6_1.75_3"};
        for (String g : graphs) {
            double average_collinearities = 0;
            double average_crossings = 0;
            for (int i = 0; i < 100; i++) {
                System.out.println("Iteration " + i + "/100");
                Graph graph = new Graph(new FileReader(Paths.get(Objects.requireNonNull(CountCollinearities.class.getClassLoader().getResource("experiments/graphs/" + g + "/" + i + ".json")).toURI()).toFile()));
                ArrayList<CrossingData>[] result = Utils.getCrossings(graph);
                average_crossings += result[0].size()/100.0;
                average_collinearities += result[1].size()/100.0;
            }
            average_collinearities /= Integer.parseInt(String.valueOf(g.charAt(11)));
            average_crossings /= Integer.parseInt(String.valueOf(g.charAt(11)));
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/main/resources/experiments/results/counting/"+g+".csv", true));
                bufferedWriter.write("average_crossings," + "average_collinearities");
                bufferedWriter.newLine();
                bufferedWriter.write(average_crossings +"," + average_collinearities);
                bufferedWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
