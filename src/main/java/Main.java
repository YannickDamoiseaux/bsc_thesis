import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("Crossing number = " + new ExactAnchorsNoRounding("test2.json").solve());
    }
}
