package jump61;

import java.util.ArrayList;
import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 *  @author Kaito Garcia
 */
class AI extends Player {

    /**
     * A new player of GAME initially COLOR that chooses moves automatically.
     * SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();

        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /**
     * Return a move after searching the game tree to DEPTH>0 moves
     * from the current position. Assumes the game is not over.
     */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        assert work.getWinner() == null;
        _foundMove = -1;
        if (getSide() == RED) {
            minMax(work, 5, true, 1, negInf, posInf);
        } else {
            minMax(work, 5, true, -1, negInf, posInf);
        }
        return _foundMove;
    }


    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.
     * Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticEval(board, sense);
        }
        if (sense == 1) {
            int best = negInf;
            ArrayList<Integer> moves = possibleMoves(board, RED);
            for (int val : moves) {
                Board next = makeMove(board, RED, val);
                int response = minMax(next, depth - 1, false, -1, alpha, beta);
                if (response > best) {
                    best = response;
                    if (saveMove) {
                        _foundMove = val;
                    }
                    alpha = Math.max(alpha, best);
                    if (alpha >= beta) {
                        return best;
                    }
                }
            }
            return best;

        } else {
            int best = posInf;
            ArrayList<Integer> moves = possibleMoves(board, BLUE);
            for (int val : moves) {
                Board next = makeMove(board, BLUE, val);
                int response = minMax(next, depth - 1, false, 1, alpha, beta);
                if (response < best) {
                    best = response;
                    if (saveMove) {
                        _foundMove = val;
                    }
                    beta = Math.min(beta, best);
                    if (alpha >= beta) {
                        return best;
                    }
                }
            }
            return best;
        }

    }


    /** simulates a move.
     * @param b b
     * @param player player
     * @param square square
     * @return a copy of board w simulated move */
    private Board makeMove(Board b, Side player, int square) {
        Board copyBoard = new Board(b.size());
        copyBoard.copy(b);
        copyBoard.addSpot(player, square);
        return copyBoard;
    }


    /**
     * returns ArrayList of integers of all squares that are possible to
     * move for either player.
     *
     * @param board  is board
     * @param player is player
     */
    private ArrayList<Integer> possibleMoves(Board board, Side player) {
        ArrayList<Integer> moves = new ArrayList<>();
        for (int i = 0; i < board.size() * board.size(); i++) {
            if (board.get(i).getSide() == player && board.exists(i)) {
                moves.add(i);
            }
        }
        return moves;
    }


    /**
     * Return a heuristic estimate of the value of board position B.
     * Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     * indicate a win for Blue.
     * @param b            is board
     * @param winningValue is winning val
     */
    private int staticEval(Board b, int winningValue) {
        return b.numOfSide(RED) - b.numOfSide(BLUE);
    }

    /**
     * A random-number generator used for move selection.
     */
    private Random _random;

    /**
     * Used to convey moves discovered by minMax.
     */
    private int _foundMove;

    /**
     * Negative infinity.
     */
    private final int negInf = Integer.MIN_VALUE;

    /**
     * Positive infinity.
     */
    private final int posInf = Integer.MAX_VALUE;
}
