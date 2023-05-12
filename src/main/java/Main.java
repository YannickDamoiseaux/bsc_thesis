import solvers.*;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        long startTime = System.nanoTime();
        System.out.println("Crossing number = " + new LowerboundSAT("test9.json").solve());
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }
}
