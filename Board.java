package jump61;

import java.util.ArrayDeque;
import java.util.Formatter;

import java.util.Stack;
import java.util.function.Consumer;

import static jump61.Side.*;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Kaito Garcia
 */
class Board {

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        _size = N;
        board = new Square[N * N];
        for (int i = 0; i < N * N; i++) {
            board[i] = Square.INITIAL;
        }

    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this(board0.size());
        _size = board0.size();
        _notifier = NOP;
        clear(board0.size());
        copy(board0);
        _readonlyBoard = new ConstantBoard(this);
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        Square[] clearedBoard = new Square[N * N];
        for (int i = 0; i < N * N; i++) {
            clearedBoard[i] = Square.INITIAL;
        }
        board =  clearedBoard;
        _history.clear();
        _size = N;
        announce();
    }

    /** Copy the contents of BOARD into me.
     * @param boardCopy is copy of board*/
    void copy(Board boardCopy) {
        for (int i = 0; i < board.length; i++) {
            board[i] = boardCopy.get(i);
        }
        _history.clear();
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size.
     *  @param boardCopy is copy of board*/
    private void internalCopy(Board boardCopy) {
        assert size() == boardCopy.size();
        for (int i = 0; i < size() * size(); i++) {
            board[i] = boardCopy.get(i);
        }
    }

    /** Copies board configuration in a 1-D array of squares.
     * @return a copy of arr */
    private Square[] copyArray() {
        Square[] returnArr = new Square[board.length];
        for (int i = 0; i < board.length; i++) {
            returnArr[i] = board[i];
        }
        return returnArr;
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _size;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        if (exists(n)) {
            return board[n];
        }
        throw new GameException("N not in bounds");
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int spots = 0;
        for (int i = 0; i < size() * size(); i++) {
            spots += get(i).getSpots();
        }
        return spots;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        return exists(n) && isLegal(player);
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return whoseMove() == player;
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        int countB = 0;
        int countR = 0;
        for (int i = 0; i < size() * size(); i++) {
            if (board[i].getSide() == BLUE) {
                countB += 1;
            }
            if (board[i].getSide() == RED) {
                countR += 1;
            }
        }
        if (countB == size() * size()) {
            return BLUE;
        }
        if (countR == size() * size()) {
            return RED;
        }
        return null;
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int total = 0;
        for (int i = 0; i < size() * size(); i++) {
            Side compare = get(i).getSide();
            if (compare == side) {
                total += 1;
            }
        }
        return total;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        if (exists(r, c)) {
            markUndo();
            simpleAdd(player, r, c,  1);
            jump(sqNum(r, c));
        }

        if (getWinner() != null) {
            getWinner();
        }
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        board[n] = square(player, num);
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        if (_history.isEmpty()) {
            clear(size());
        } else {
            board = _history.pop();
        }
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        _history.push(copyArray());
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full. */
    private void jump(int S) {

        if (getWinner() == null) {
            Side player = board[S].getSide();
            if (board[S].getSpots() > neighbors(S)) {
                initialHelper(player, S);
                while (!_workQueue.isEmpty() && getWinner() == null) {
                    helper(player);

                }
            }
        }
    }

    /** Initial helper, puts neighbors that need processing into workqueue.
     * @param player is player
     * @param S is square val */
    private void initialHelper(Side player, int S) {
        set(row(S), col(S), board[S].getSpots() - neighbors(S), player);
        int[] neighbors = findNeighbors(S);
        for (int val : neighbors) {
            if (exists(val)) {
                _workQueue.addFirst(val);
                simpleAdd(player, val, 1);
            }
        }
    }

    /** sorts through work queue.
     * @param player is player*/
    private void helper(Side player) {
        int T = _workQueue.removeLast();
        if (board[T].getSpots() > neighbors(T)) {
            set(row(T), col(T), board[T].getSpots() - neighbors(T), player);
            int[] neighbors = findNeighbors(T);
            for (int val : neighbors) {
                if (exists(val)) {
                    _workQueue.addFirst(val);
                    addSpot(player, val);
                }
            }
        }
    }

    /** Returns neighbors of any square.
     * @param T is square number*/
    private int[] findNeighbors(int T) {
        if (T % size() == 0) {
            return new int[]{T - size(), T + size(), T + 1};

        }
        if (T % size() == size() - 1) {
            return new int[]{T - size(), T + size(), T - 1};
        }
        return new int[]{T - size(), T + size(), T - 1, T + 1};
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===");
        for (int i = 0; i < board.length; i++) {
            if (i % size() == 0) {
                out.format("\n    ");
            }
            if (board[i].getSide() == WHITE) {
                out.format("%d- ", board[i].getSpots());
            } else if (board[i].getSide() == BLUE) {
                out.format("%db ", board[i].getSpots());
            } else {
                out.format("%dr ", board[i].getSpots());
            }
        }
        out.format("\n===");
        return out.toString();
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            return this == obj;
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** Size of square board; board is of size _size x _size. */
    private int _size;

    /** 1-D Array that stores size() * size() elements of Square. */
    private Square[] board;

    /** stores history in stack. */
    private Stack<Square[]> _history = new Stack<>();

}
