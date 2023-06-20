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
        //System.out.println(Utils.doEdgesCross(0, 0, 2, 0, 0, 0, 3, 0));
        long startTime = System.nanoTime();
        double bestCrossingNumber = new UpperboundRandom("experiments/graphs/11_0.3_1.75_3/16.json", true).solve();
        //double bestCrossingNumber = new ExactPruning("generated/test6.json").solve();
        /*double bestCrossingNumber = -1;
        for (int i = 0; i < 10000; i++) {
             bestCrossingNumber = new ExactAnchors("generated/test2.json", i).solve();
            if (bestCrossingNumber != 15) {
                System.out.println("THIS ONE " + i);
                break;
            }
        }*/
        //double bestCrossingNumber = new UpperboundMetis("generated/test12.json", true, false).solve();
        //double bestCrossingNumber = new UpperboundRandom("experiments/graphs/8_0.6_1.25_1/11.json", true).solve();
        //double bestCrossingNumber = new ExactPruning("generated/test10.json").solve();
        /*double bestCrossingNumber = Integer.MAX_VALUE;
        for (int i = 0; i < 10000; i++) {
            System.out.println(i);
            double crossingNumber = new UpperboundRandom("experiments/graphs/11_0.3_1.75_3/"+i+".json", true).solve();
            if (crossingNumber < bestCrossingNumber) {
                System.out.println("New best: " + crossingNumber + " with seed " + i);
                bestCrossingNumber = crossingNumber;
            }
        }*/
        /*int[] pSize = {2,2,2,2,2,2,2,2,2,5,3,4};
        for (int i = 1; i < 13; i++) {
            double bestCrossingNumber = Integer.MAX_VALUE;
            for (int p = pSize[i-1]; p < 8; p++) {
                double crossingNumber = new UpperboundMetis("generated/test" + i + ".json", p, 340).solve();
                if (crossingNumber < bestCrossingNumber) bestCrossingNumber = crossingNumber;
            }
            if (bestCrossingNumber == Integer.MAX_VALUE) System.out.println(i + ": INFEASIBLE");
            else System.out.println(i + ": Crossing number = " + bestCrossingNumber);
        }*/
        if (bestCrossingNumber == Integer.MAX_VALUE) System.out.println("INFEASIBLE");
        else System.out.println("Crossing number = " + bestCrossingNumber);

        /*Point[] p1 = {new Point(0,0), new Point(1,2), new Point(0,1)};
        Point[] p2 = {new Point(3,4), new Point(5,2), new Point(3,0)};
        System.out.println(distance(p1, p2));
        System.out.println(average(p1, p2));*/
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

    }
}