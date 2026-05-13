package com.checkers.game;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int SIZE = 8;

    private Piece[][] grid;

    public Board() {
        grid = new Piece[SIZE][SIZE];
    }

    public static Board initialBoard() {
        Board b = new Board();
        // BLACK pieces on rows 0-2 (top)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    b.grid[row][col] = new Piece(Piece.Color.BLACK);
                }
            }
        }
        // RED pieces on rows 5-7 (bottom)
        for (int row = 5; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    b.grid[row][col] = new Piece(Piece.Color.RED);
                }
            }
        }
        return b;
    }

    public Piece getPiece(int row, int col) {
        if (!inBounds(row, col)) return null;
        return grid[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (inBounds(row, col)) grid[row][col] = piece;
    }

    public void removePiece(int row, int col) {
        if (inBounds(row, col)) grid[row][col] = null;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public Board copy() {
        Board b = new Board();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                b.grid[r][c] = grid[r][c] == null ? null : grid[r][c].copy();
            }
        }
        return b;
    }

    public void applyMove(Move move) {
        Piece piece = getPiece(move.getFromRow(), move.getFromCol());
        removePiece(move.getFromRow(), move.getFromCol());
        for (int[] cap : move.getCapturedPieces()) {
            removePiece(cap[0], cap[1]);
        }
        setPiece(move.getToRow(), move.getToCol(), piece);
        // King promotion
        if (piece != null) {
            if (piece.getColor() == Piece.Color.RED && move.getToRow() == 0) piece.makeKing();
            if (piece.getColor() == Piece.Color.BLACK && move.getToRow() == SIZE - 1) piece.makeKing();
        }
    }

    public List<Move> getLegalMoves(Piece.Color color) {
        List<Move> captures = new ArrayList<>();
        List<Move> simple = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != color) continue;
                List<Move> caps = getCaptureMoves(r, c, this);
                captures.addAll(caps);
                if (caps.isEmpty()) {
                    simple.addAll(getSimpleMoves(r, c));
                }
            }
        }
        // Mandatory capture rule
        return captures.isEmpty() ? simple : captures;
    }

    private List<Move> getSimpleMoves(int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece p = grid[row][col];
        if (p == null) return moves;
        int[] dirs = moveDirs(p);
        for (int dr : dirs) {
            for (int dc : new int[]{-1, 1}) {
                int nr = row + dr, nc = col + dc;
                if (inBounds(nr, nc) && grid[nr][nc] == null) {
                    moves.add(new Move(row, col, nr, nc));
                }
            }
        }
        return moves;
    }

    // Recursive multi-jump capture generation
    public static List<Move> getCaptureMoves(int row, int col, Board board) {
        List<Move> result = new ArrayList<>();
        Piece p = board.getPiece(row, col);
        if (p == null) return result;
        captureHelper(row, col, p, board, new Move(row, col, row, col), new ArrayList<String>(), result);
        return result;
    }

    private static void captureHelper(int row, int col, Piece piece,
                                       Board board, Move current,
                                       List<String> visited, List<Move> result) {
        int[] dirs = moveDirs(piece);
        boolean foundJump = false;
        for (int dr : dirs) {
            for (int dc : new int[]{-1, 1}) {
                int mr = row + dr, mc = col + dc;   // midpoint (enemy)
                int nr = row + 2 * dr, nc = col + 2 * dc; // landing
                if (!board.inBounds(nr, nc)) continue;
                Piece mid = board.getPiece(mr, mc);
                if (mid == null || mid.getColor() == piece.getColor()) continue;
                if (board.getPiece(nr, nc) != null) continue;
                String key = mr + "," + mc;
                if (visited.contains(key)) continue;

                foundJump = true;
                Board next = board.copy();
                next.removePiece(mr, mc);
                next.removePiece(row, col);
                next.setPiece(nr, nc, piece);

                Move newMove = new Move(current.getFromRow(), current.getFromCol(), nr, nc);
                for (int[] cap : current.getCapturedPieces()) newMove.addCapture(cap[0], cap[1]);
                newMove.addCapture(mr, mc);

                List<String> newVisited = new ArrayList<>(visited);
                newVisited.add(key);

                // Check if piece becomes king mid-jump (stops multi-jump)
                boolean becameKing = !piece.isKing() &&
                        ((piece.getColor() == Piece.Color.RED && nr == 0) ||
                         (piece.getColor() == Piece.Color.BLACK && nr == Board.SIZE - 1));

                if (becameKing) {
                    result.add(newMove);
                } else {
                    List<Move> sub = new ArrayList<>();
                    captureHelper(nr, nc, piece, next, newMove, newVisited, sub);
                    if (sub.isEmpty()) {
                        result.add(newMove);
                    } else {
                        result.addAll(sub);
                    }
                }
            }
        }
    }

    private static int[] moveDirs(Piece p) {
        if (p.isKing()) return new int[]{-1, 1};
        return p.getColor() == Piece.Color.RED ? new int[]{-1} : new int[]{1};
    }

    public int countPieces(Piece.Color color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c] != null && grid[r][c].getColor() == color) count++;
        return count;
    }

    public int countKings(Piece.Color color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c] != null && grid[r][c].getColor() == color && grid[r][c].isKing()) count++;
        return count;
    }
}
