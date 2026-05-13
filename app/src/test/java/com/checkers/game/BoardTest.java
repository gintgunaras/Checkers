package com.checkers.game;

import org.junit.Test;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.*;

public class BoardTest {

    private Board board;

    @Before
    public void setUp() {
        board = Board.initialBoard();
    }

    @Test
    public void testInitialPieceCount() {
        assertEquals(12, board.countPieces(Piece.Color.RED));
        assertEquals(12, board.countPieces(Piece.Color.BLACK));
    }

    @Test
    public void testInitialKingCount() {
        assertEquals(0, board.countKings(Piece.Color.RED));
        assertEquals(0, board.countKings(Piece.Color.BLACK));
    }

    @Test
    public void testInitialBoardLayout() {
        // RED pieces should be on rows 5-7
        for (int row = 5; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    Piece p = board.getPiece(row, col);
                    assertNotNull("Expected RED piece at (" + row + "," + col + ")", p);
                    assertEquals(Piece.Color.RED, p.getColor());
                }
            }
        }
        // BLACK pieces on rows 0-2
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    Piece p = board.getPiece(row, col);
                    assertNotNull("Expected BLACK piece at (" + row + "," + col + ")", p);
                    assertEquals(Piece.Color.BLACK, p.getColor());
                }
            }
        }
    }

    @Test
    public void testEmptyRows() {
        // Rows 3-4 should be empty
        for (int col = 0; col < Board.SIZE; col++) {
            assertNull(board.getPiece(3, col));
            assertNull(board.getPiece(4, col));
        }
    }

    @Test
    public void testInBounds() {
        assertTrue(board.inBounds(0, 0));
        assertTrue(board.inBounds(7, 7));
        assertFalse(board.inBounds(-1, 0));
        assertFalse(board.inBounds(0, 8));
        assertFalse(board.inBounds(8, 8));
    }

    @Test
    public void testCopyIsIndependent() {
        // (5,0): row=5, col=0 -> 5+0=5 (odd) -> dark square, has RED piece
        Board copy = board.copy();
        copy.removePiece(5, 0);
        assertNotNull(board.getPiece(5, 0));
        assertNull(copy.getPiece(5, 0));
    }

    @Test
    public void testApplySimpleMove() {
        // RED piece at (5,0) can move to (4,1)
        Move move = new Move(5, 0, 4, 1);
        board.applyMove(move);
        assertNull(board.getPiece(5, 0));
        assertNotNull(board.getPiece(4, 1));
        assertEquals(Piece.Color.RED, board.getPiece(4, 1).getColor());
    }

    @Test
    public void testKingPromotion_Red() {
        Board b = new Board();
        Piece red = new Piece(Piece.Color.RED);
        b.setPiece(1, 0, red);
        Move move = new Move(1, 0, 0, 1);
        b.applyMove(move);
        Piece promoted = b.getPiece(0, 1);
        assertNotNull(promoted);
        assertTrue(promoted.isKing());
    }

    @Test
    public void testKingPromotion_Black() {
        Board b = new Board();
        Piece black = new Piece(Piece.Color.BLACK);
        b.setPiece(6, 1, black);
        Move move = new Move(6, 1, 7, 0);
        b.applyMove(move);
        Piece promoted = b.getPiece(7, 0);
        assertNotNull(promoted);
        assertTrue(promoted.isKing());
    }

    @Test
    public void testCaptureMoveRemovesPiece() {
        Board b = new Board();
        b.setPiece(4, 3, new Piece(Piece.Color.RED));
        b.setPiece(3, 4, new Piece(Piece.Color.BLACK));
        List<Move> caps = Board.getCaptureMoves(4, 3, b);
        assertFalse(caps.isEmpty());
        Move cap = caps.get(0);
        b.applyMove(cap);
        assertNull(b.getPiece(3, 4)); // captured piece gone
        assertNotNull(b.getPiece(2, 5)); // red moved
    }

    @Test
    public void testRedLegalMovesFromStart() {
        List<Move> moves = board.getLegalMoves(Piece.Color.RED);
        assertFalse(moves.isEmpty());
        // All should be simple moves (no captures at start)
        for (Move m : moves) {
            assertFalse(m.isCapture());
        }
    }

    @Test
    public void testBlackLegalMovesFromStart() {
        List<Move> moves = board.getLegalMoves(Piece.Color.BLACK);
        assertFalse(moves.isEmpty());
        for (Move m : moves) {
            assertFalse(m.isCapture());
        }
    }
}
