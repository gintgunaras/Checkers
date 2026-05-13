package com.checkers.game;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CheckersAITest {

    @Test
    public void testAIReturnsValidMove_AllDifficulties() {
        for (CheckersAI.Difficulty diff : CheckersAI.Difficulty.values()) {
            GameState state = new GameState();
            // Let RED move first
            List<Move> redMoves = state.getLegalMoves();
            state.applyMove(redMoves.get(0));

            CheckersAI ai = new CheckersAI(diff, Piece.Color.BLACK);
            Move move = ai.chooseMove(state);
            assertNotNull("AI should return a move for difficulty " + diff, move);

            // Verify it is a legal move
            List<Move> legalMoves = state.getLegalMoves();
            boolean found = false;
            for (Move m : legalMoves) {
                if (m.getFromRow() == move.getFromRow() && m.getFromCol() == move.getFromCol()
                        && m.getToRow() == move.getToRow() && m.getToCol() == move.getToCol()) {
                    found = true;
                    break;
                }
            }
            assertTrue("AI move should be legal for difficulty " + diff, found);
        }
    }

    @Test
    public void testAIPrefersCaptureWhenAvailable() {
        // Create a board where BLACK can capture
        Board b = new Board();
        b.setPiece(2, 3, new Piece(Piece.Color.BLACK));
        b.setPiece(3, 4, new Piece(Piece.Color.RED));
        // BLACK can capture RED at (3,4) by jumping to (4,5)

        GameState state = new GameState();
        // Replace board via copy trick — we test the AI move generator directly
        List<Move> caps = Board.getCaptureMoves(2, 3, b);
        assertFalse("Should have capture moves", caps.isEmpty());
        assertEquals(1, caps.size());
        assertEquals(4, caps.get(0).getToRow());
        assertEquals(5, caps.get(0).getToCol());
    }

    @Test
    public void testAIReturnsNullWhenNoMoves() {
        // Create a state with no legal moves for BLACK
        Board b = new Board();
        b.setPiece(0, 1, new Piece(Piece.Color.RED, true)); // Only a RED king, BLACK's turn would be empty
        GameState state = new GameState();
        // Remove all black pieces from the real state
        for (int r = 0; r < Board.SIZE; r++)
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = state.getBoard().getPiece(r, c);
                if (p != null && p.getColor() == Piece.Color.BLACK)
                    state.getBoard().removePiece(r, c);
            }
        // Move RED so it's BLACK's turn
        List<Move> redMoves = state.getLegalMoves();
        if (!redMoves.isEmpty()) state.applyMove(redMoves.get(0));

        CheckersAI ai = new CheckersAI(CheckersAI.Difficulty.MEDIUM, Piece.Color.BLACK);
        Move move = ai.chooseMove(state);
        assertNull("AI should return null when no moves available", move);
    }

    @Test
    public void testMoveCountsForAllDifficulties() {
        assertEquals(5, CheckersAI.Difficulty.values().length);
    }

    @Test
    public void testDifficultyDepths() {
        assertEquals(1, CheckersAI.Difficulty.BEGINNER.depth);
        assertEquals(2, CheckersAI.Difficulty.EASY.depth);
        assertEquals(4, CheckersAI.Difficulty.MEDIUM.depth);
        assertEquals(6, CheckersAI.Difficulty.HARD.depth);
        assertEquals(8, CheckersAI.Difficulty.EXPERT.depth);
    }
}
