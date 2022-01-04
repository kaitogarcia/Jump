package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 *  @author Kaito Garcia
 */

public class BoardTest {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals("bad length", 5, B.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        assertEquals("wrong color1", WHITE, B.get(1, 1).getSide());
        assertEquals("wrong number of spots1", 1, B.get(2, 2).getSpots());

        B.set(2, 2, 2, RED);
        assertEquals("wrong number of spots2", 2, B.get(2, 2).getSpots());
        assertEquals("wrong number of spots3", 1, B.get(1, 2).getSpots());
        assertEquals("wrong color2", WHITE, B.get(1, 1).getSide());
        assertEquals("wrong color3", RED, B.get(2, 2).getSide());
        assertEquals("wrong count1", 1, B.numOfSide(RED));
        assertEquals("wrong count2", 0, B.numOfSide(BLUE));
        assertEquals("wrong count3", 24, B.numOfSide(WHITE));
    }

    @Test
    public void testerCase() {
        Board game = new Board(3);
        game.addSpot(RED, 1, 1);
        System.out.println(game.toString());
        game.addSpot(BLUE, 2, 3);
        System.out.println(game.toString());
        game.addSpot(RED, 2, 2);
        System.out.println(game.toString());
        game.addSpot(BLUE, 3, 3);
        System.out.println(game.toString());
        game.addSpot(RED, 2, 2);
        System.out.println(game.toString());
        game.addSpot(BLUE, 2, 3);
        System.out.println(game.toString());
        game.addSpot(RED, 1, 3);
        System.out.println(game.toString());
        game.addSpot(BLUE, 2, 3);
        System.out.println(game.toString());
        game.addSpot(RED, 2, 1);
        System.out.println(game.toString());


    }

    @Test
    public void testJump() {
        Board B = new Board(3);
        while (B.getWinner() == null) {
            B.addSpot(RED, 1, 1);
            B.addSpot(BLUE, 3, 3);
            System.out.println(B.getWinner());
            System.out.println(B.toString());
        }
        assertEquals("wrong winner", BLUE, B.getWinner());
    }

    @Test
    public void playThrough() {
        Board game = new Board(3);
        game.addSpot(BLUE, 1, 1);
        System.out.println(game.toString());
        game.addSpot(RED, 3, 3);
        System.out.println(game.toString());
        game.addSpot(BLUE, 1, 1);
        System.out.println(game.toString());
        game.addSpot(RED, 3, 3);
        System.out.println(game.toString());
        game.addSpot(BLUE, 3, 1);
        System.out.println(game.toString());
        game.addSpot(RED, 2, 2);
        System.out.println(game.toString());
        game.addSpot(BLUE, 1, 3);
        System.out.println(game.toString());
        game.addSpot(RED, 3, 3);
        System.out.println(game.toString());
        game.addSpot(BLUE, 1, 1);
        System.out.println(game.toString());
        game.addSpot(RED, 2, 2);
        System.out.println(game.toString());
        game.addSpot(BLUE, 1, 1);
        System.out.println(game.toString());
        game.addSpot(RED, 3, 3);
        System.out.println(game.toString());
        game.addSpot(BLUE, 2, 1);
        System.out.println(game.toString());
    }

    @Test
    public void testMove() {
        Board B = new Board(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
        B.undo();
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        checkBoard("#0U", B);
    }

    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                                     contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                       (B.get(i).getSide() != WHITE)
                       || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

}
