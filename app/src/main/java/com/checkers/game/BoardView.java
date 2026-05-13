package com.checkers.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BoardView extends View {

    public interface MoveListener {
        void onMoveMade(Move move);
    }

    private static final int LIGHT_SQUARE = Color.parseColor("#F0D9B5");
    private static final int DARK_SQUARE  = Color.parseColor("#B58863");
    private static final int RED_PIECE    = Color.parseColor("#CC2200");
    private static final int BLACK_PIECE  = Color.parseColor("#222222");
    private static final int KING_CROWN   = Color.parseColor("#FFD700");
    private static final int HIGHLIGHT    = Color.parseColor("#88FFFF00");
    private static final int VALID_MOVE   = Color.parseColor("#8800CC00");

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private GameState gameState;
    private MoveListener moveListener;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<Move> validMovesForSelected = new ArrayList<>();
    private boolean playerTurn = true;

    public BoardView(Context context) { super(context); }
    public BoardView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setGameState(GameState state) {
        this.gameState = state;
        clearSelection();
        invalidate();
    }

    public void setMoveListener(MoveListener listener) { this.moveListener = listener; }
    public void setPlayerTurn(boolean playerTurn) { this.playerTurn = playerTurn; }

    private float cellSize() { return Math.min(getWidth(), getHeight()) / (float) Board.SIZE; }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int size = Math.min(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (gameState == null) return;
        float cell = cellSize();
        Board board = gameState.getBoard();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                // Square
                paint.setColor((r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                rect.set(c * cell, r * cell, (c + 1) * cell, (r + 1) * cell);
                canvas.drawRect(rect, paint);

                // Highlight selected
                if (r == selectedRow && c == selectedCol) {
                    paint.setColor(HIGHLIGHT);
                    canvas.drawRect(rect, paint);
                }

                // Valid move dots
                for (Move m : validMovesForSelected) {
                    if (m.getToRow() == r && m.getToCol() == c) {
                        paint.setColor(VALID_MOVE);
                        canvas.drawRect(rect, paint);
                    }
                }

                // Piece
                Piece piece = board.getPiece(r, c);
                if (piece != null) drawPiece(canvas, piece, r, c, cell);
            }
        }
    }

    private void drawPiece(Canvas canvas, Piece piece, int row, int col, float cell) {
        float cx = col * cell + cell / 2f;
        float cy = row * cell + cell / 2f;
        float radius = cell * 0.38f;

        // Shadow
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawCircle(cx + cell * 0.04f, cy + cell * 0.04f, radius, paint);

        // Piece body
        paint.setColor(piece.getColor() == Piece.Color.RED ? RED_PIECE : BLACK_PIECE);
        canvas.drawCircle(cx, cy, radius, paint);

        // Highlight shine
        paint.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.3f, radius * 0.4f, paint);

        // King crown indicator
        if (piece.isKing()) {
            paint.setColor(KING_CROWN);
            paint.setTextSize(radius * 1.0f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("♛", cx, cy + radius * 0.35f, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!playerTurn || gameState == null || gameState.getStatus() != GameState.Status.ONGOING) return true;
        if (event.getAction() != MotionEvent.ACTION_UP) return true;

        float cell = cellSize();
        int col = (int) (event.getX() / cell);
        int row = (int) (event.getY() / cell);
        if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) return true;

        Piece tapped = gameState.getBoard().getPiece(row, col);

        if (selectedRow == -1) {
            // Select a piece
            if (tapped != null && tapped.getColor() == gameState.getCurrentTurn()) {
                selectedRow = row;
                selectedCol = col;
                computeValidMoves();
                invalidate();
            }
        } else {
            // Try to move
            Move match = null;
            for (Move m : validMovesForSelected) {
                if (m.getToRow() == row && m.getToCol() == col) { match = m; break; }
            }
            if (match != null) {
                clearSelection();
                if (moveListener != null) moveListener.onMoveMade(match);
            } else if (tapped != null && tapped.getColor() == gameState.getCurrentTurn()) {
                selectedRow = row;
                selectedCol = col;
                computeValidMoves();
                invalidate();
            } else {
                clearSelection();
                invalidate();
            }
        }
        return true;
    }

    private void computeValidMoves() {
        validMovesForSelected.clear();
        if (selectedRow < 0) return;
        List<Move> all = gameState.getLegalMoves();
        for (Move m : all) {
            if (m.getFromRow() == selectedRow && m.getFromCol() == selectedCol)
                validMovesForSelected.add(m);
        }
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        validMovesForSelected.clear();
    }
}
