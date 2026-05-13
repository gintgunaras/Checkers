package com.checkers.game;

public class Piece {
    public enum Color { RED, BLACK }

    private Color color;
    private boolean king;

    public Piece(Color color) {
        this.color = color;
        this.king = false;
    }

    public Piece(Color color, boolean king) {
        this.color = color;
        this.king = king;
    }

    public Color getColor() { return color; }
    public boolean isKing() { return king; }
    public void makeKing() { king = true; }

    public Piece copy() {
        return new Piece(color, king);
    }

    @Override
    public String toString() {
        return (king ? "K" : "P") + color.name().charAt(0);
    }
}
