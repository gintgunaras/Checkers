package com.checkers.game;

import java.util.List;

public class GameState {
    public enum Status { ONGOING, RED_WINS, BLACK_WINS, DRAW }

    private Board board;
    private Piece.Color currentTurn;
    private Status status;
    private int movesSinceCapture;

    public GameState() {
        board = Board.initialBoard();
        currentTurn = Piece.Color.RED;
        status = Status.ONGOING;
        movesSinceCapture = 0;
    }

    private GameState(Board board, Piece.Color turn, Status status, int movesSinceCapture) {
        this.board = board;
        this.currentTurn = turn;
        this.status = status;
        this.movesSinceCapture = movesSinceCapture;
    }

    public Board getBoard() { return board; }
    public Piece.Color getCurrentTurn() { return currentTurn; }
    public Status getStatus() { return status; }

    public List<Move> getLegalMoves() {
        return board.getLegalMoves(currentTurn);
    }

    public boolean applyMove(Move move) {
        if (status != Status.ONGOING) return false;
        List<Move> legal = getLegalMoves();
        boolean valid = false;
        for (Move m : legal) {
            if (m.getFromRow() == move.getFromRow() && m.getFromCol() == move.getFromCol()
                    && m.getToRow() == move.getToRow() && m.getToCol() == move.getToCol()) {
                move = m; // use the one with capture info
                valid = true;
                break;
            }
        }
        if (!valid) return false;

        if (move.isCapture()) movesSinceCapture = 0;
        else movesSinceCapture++;

        board.applyMove(move);
        currentTurn = opponent(currentTurn);
        updateStatus();
        return true;
    }

    private void updateStatus() {
        if (movesSinceCapture >= 40) { status = Status.DRAW; return; }
        List<Move> moves = board.getLegalMoves(currentTurn);
        if (moves.isEmpty()) {
            status = currentTurn == Piece.Color.RED ? Status.BLACK_WINS : Status.RED_WINS;
        }
    }

    public static Piece.Color opponent(Piece.Color c) {
        return c == Piece.Color.RED ? Piece.Color.BLACK : Piece.Color.RED;
    }

    public GameState copy() {
        return new GameState(board.copy(), currentTurn, status, movesSinceCapture);
    }
}
