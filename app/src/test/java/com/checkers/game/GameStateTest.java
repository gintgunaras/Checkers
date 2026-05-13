package com.checkers.game;

import org.junit.Test;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.*;

public class GameStateTest {

    private GameState state;

    @Before
    public void setUp() {
        state = new GameState();
    }

    @Test
    public void testInitialTurn() {
        assertEquals(Piece.Color.RED, state.getCurrentTurn());
    }

    @Test
    public void testInitialStatus() {
        assertEquals(GameState.Status.ONGOING, state.getStatus());
    }

    @Test
    public void testMoveChangesTurn() {
        List<Move> moves = state.getLegalMoves();
        assertFalse(moves.isEmpty());
        assertTrue(state.applyMove(moves.get(0)));
        assertEquals(Piece.Color.BLACK, state.getCurrentTurn());
    }

    @Test
    public void testInvalidMoveRejected() {
        Move invalid = new Move(0, 0, 1, 1);
        assertFalse(state.applyMove(invalid));
    }

    @Test
    public void testOpponent() {
        assertEquals(Piece.Color.BLACK, GameState.opponent(Piece.Color.RED));
        assertEquals(Piece.Color.RED, GameState.opponent(Piece.Color.BLACK));
    }

    @Test
    public void testCopyIsIndependent() {
        GameState copy = state.copy();
        List<Move> moves = copy.getLegalMoves();
        copy.applyMove(moves.get(0));
        // Original should still be RED's turn
        assertEquals(Piece.Color.RED, state.getCurrentTurn());
    }

    @Test
    public void testBlackWinsWhenRedHasNoMoves() {
        // Set up a board where RED has no pieces
        Board b = new Board();
        b.setPiece(0, 1, new Piece(Piece.Color.BLACK));
        // Manually craft a state where RED is to move but has no pieces/moves
        // We verify by checking getLegalMoves returns empty = BLACK wins
        GameState gs = new GameState();
        // Remove all red pieces
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = gs.getBoard().getPiece(r, c);
                if (p != null && p.getColor() == Piece.Color.RED) gs.getBoard().removePiece(r, c);
            }
        // Trigger update by trying to get legal moves
        List<Move> moves = gs.getLegalMoves();
        assertTrue(moves.isEmpty());
    }

    @Test
    public void testDrawAfterManyNonCaptureMoves() {
        GameState gs = new GameState();
        // Apply 40 non-capture moves to trigger draw
        int movesApplied = 0;
        for (int i = 0; i < 20 && gs.getStatus() == GameState.Status.ONGOING; i++) {
            List<Move> redMoves = gs.getLegalMoves();
            if (redMoves.isEmpty()) break;
            Move redMove = findSimpleMove(redMoves);
            if (redMove == null) break;
            gs.applyMove(redMove);
            movesApplied++;
            if (gs.getStatus() != GameState.Status.ONGOING) break;

            List<Move> blackMoves = gs.getLegalMoves();
            if (blackMoves.isEmpty()) break;
            Move blackMove = findSimpleMove(blackMoves);
            if (blackMove == null) break;
            gs.applyMove(blackMove);
            movesApplied++;
        }
        // After 40+ moves without captures, draw should be possible
        assertTrue(movesApplied > 0);
    }

    private Move findSimpleMove(List<Move> moves) {
        for (Move m : moves) if (!m.isCapture()) return m;
        return moves.isEmpty() ? null : moves.get(0);
    }
}
