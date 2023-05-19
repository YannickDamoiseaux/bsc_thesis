import graph.Point;
import solvers.ExactAnchors;
import solvers.ExactAnchorsNoRounding;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class ExactAnchorsTest {
    @Test
    public void test_notes_1() throws FileNotFoundException, URISyntaxException {
        assertEquals(1, new ExactAnchorsNoRounding("specialcases/test_notes_1.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 0), new Point(3, 4), new Point(1, 0), new Point(2, 4)}));
    }

    @Test
    public void test_notes_2() throws FileNotFoundException, URISyntaxException {
        assertEquals(4, new ExactAnchorsNoRounding("specialcases/test_notes_2.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 0), new Point(0, 3), new Point(2, 0), new Point(2, 2), new Point(4, 0), new Point(4, 1), new Point(5, 3)}));
    }

    @Test
    public void test_notes_3() throws FileNotFoundException, URISyntaxException {
        assertEquals(2, new ExactAnchorsNoRounding("specialcases/test_notes_3.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 0), new Point(0, 2), new Point(1, 3), new Point(2, 0), new Point(2, 2)}));
    }

    @Test
    public void test_notes_4_1() throws FileNotFoundException, URISyntaxException {
        assertEquals(2, new ExactAnchorsNoRounding("specialcases/test_notes_4.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 4), new Point(2, 3)}));
    }

    @Test
    public void test_notes_4_2() throws FileNotFoundException, URISyntaxException {
        assertEquals(0, new ExactAnchorsNoRounding("specialcases/test_notes_4.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 0), new Point(0, 1), new Point(2, 3), new Point(1, 0), new Point(1, 4)}));
    }

    @Test
    public void test_notes_5() throws FileNotFoundException, URISyntaxException {
        assertEquals(1, new ExactAnchorsNoRounding("specialcases/test_notes_5.json").calculateNumberOfCrossingStatic(new Point[]{new Point(0, 1), new Point(6, 6), new Point(0, 0), new Point(16, 17)}));
    }

    @Test
    public void test_graph() throws FileNotFoundException, URISyntaxException {
        assertEquals(0, new ExactAnchors("contest/graph.json").solve(), 0.0001);
    }
}
