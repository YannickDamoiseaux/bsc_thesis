import graph.Edge;
import solvers.*;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        long startTime = System.nanoTime();
        double crossingNumber = new LowerboundSAT("generated/test2.json").solve();
        if (crossingNumber == Integer.MAX_VALUE) System.out.println("INFEASIBLE");
        else System.out.println("Crossing number = " + crossingNumber);
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }
}
