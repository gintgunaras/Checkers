package com.checkers.game;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoveTest {

    @Test
    public void testSimpleMoveNotCapture() {
        Move m = new Move(5, 0, 4, 1);
        assertFalse(m.isCapture());
        assertEquals(0, m.captureCount());
    }

    @Test
    public void testCaptureMoveDetected() {
        Move m = new Move(5, 0, 3, 2);
        m.addCapture(4, 1);
        assertTrue(m.isCapture());
        assertEquals(1, m.captureCount());
    }

    @Test
    public void testMultiCapture() {
        Move m = new Move(5, 0, 1, 4);
        m.addCapture(4, 1);
        m.addCapture(2, 3);
        assertEquals(2, m.captureCount());
    }

    @Test
    public void testMoveCoordinates() {
        Move m = new Move(2, 3, 4, 5);
        assertEquals(2, m.getFromRow());
        assertEquals(3, m.getFromCol());
        assertEquals(4, m.getToRow());
        assertEquals(5, m.getToCol());
    }
}
