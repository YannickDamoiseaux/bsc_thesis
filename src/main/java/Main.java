import graph.Edge;
import solvers.*;
import solvers.upperbound.Upperbound;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        long startTime = System.nanoTime();
        double bestCrossingNumber = new Upperbound("generated/test2.json", 7, 340).solve();
        /*double bestCrossingNumber = Integer.MAX_VALUE;
        for (int i = 0; i < 10000; i++) {
            double crossingNumber = new Upperbound("generated/test2.json", 7, i).solve();
            if (crossingNumber < bestCrossingNumber) {
                System.out.println("New best: " + crossingNumber + " with seed " + i);
                bestCrossingNumber = crossingNumber;
            }
        }*/
        if (bestCrossingNumber == Integer.MAX_VALUE) System.out.println("INFEASIBLE");
        else System.out.println("Crossing number = " + bestCrossingNumber);
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }
}
