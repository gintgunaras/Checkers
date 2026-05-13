package com.checkers.game;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private List<int[]> capturedPieces; // [row, col] of each captured piece

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPieces = new ArrayList<>();
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public List<int[]> getCapturedPieces() { return capturedPieces; }

    public void addCapture(int row, int col) {
        capturedPieces.add(new int[]{row, col});
    }

    public boolean isCapture() {
        return !capturedPieces.isEmpty();
    }

    public int captureCount() {
        return capturedPieces.size();
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)->(%d,%d) caps=%d", fromRow, fromCol, toRow, toCol, capturedPieces.size());
    }
}
