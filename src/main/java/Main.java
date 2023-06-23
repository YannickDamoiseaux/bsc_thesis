import graph.CrossingData;
import graph.Point;
import solvers.*;
import solvers.upperbound.UpperboundMetis;
import solvers.upperbound.UpperboundRandom;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        long startTime = System.nanoTime();

        double bestCrossingNumber = new ExactPruning("generated/test2.json", true).solve();

        if (bestCrossingNumber == Integer.MAX_VALUE) System.out.println("INFEASIBLE");
        else System.out.println("Crossing number = " + bestCrossingNumber);
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

    }
}