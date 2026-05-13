package com.checkers.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Minimax AI with alpha-beta pruning.
 * 5 difficulty levels control search depth and randomness.
 */
public class CheckersAI {

    public enum Difficulty {
        BEGINNER(1, 0.7),
        EASY(2, 0.4),
        MEDIUM(4, 0.1),
        HARD(6, 0.0),
        EXPERT(8, 0.0);

        final int depth;
        final double randomness; // probability of picking a random (non-best) move

        Difficulty(int depth, double randomness) {
            this.depth = depth;
            this.randomness = randomness;
        }
    }

    private final Difficulty difficulty;
    private final Piece.Color aiColor;
    private final Random random;

    public CheckersAI(Difficulty difficulty, Piece.Color aiColor) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        this.random = new Random();
    }

    public Move chooseMove(GameState state) {
        List<Move> moves = state.getLegalMoves();
        if (moves.isEmpty()) return null;
        if (moves.size() == 1) return moves.get(0);

        // Random move for beginner/easy
        if (random.nextDouble() < difficulty.randomness) {
            return moves.get(random.nextInt(moves.size()));
        }

        Move best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            GameState next = state.copy();
            next.applyMove(move);
            int score = minimax(next, difficulty.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }

    private int minimax(GameState state, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || state.getStatus() != GameState.Status.ONGOING) {
            return evaluate(state);
        }

        List<Move> moves = state.getLegalMoves();
        if (moves.isEmpty()) return evaluate(state);

        // Move ordering: captures first
        orderMoves(moves);

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (Move move : moves) {
                GameState next = state.copy();
                next.applyMove(move);
                value = Math.max(value, minimax(next, depth - 1, alpha, beta, false));
                alpha = Math.max(alpha, value);
                if (beta <= alpha) break;
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (Move move : moves) {
                GameState next = state.copy();
                next.applyMove(move);
                value = Math.min(value, minimax(next, depth - 1, alpha, beta, true));
                beta = Math.min(beta, value);
                if (beta <= alpha) break;
            }
            return value;
        }
    }

    private void orderMoves(List<Move> moves) {
        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move a, Move b) {
                return b.captureCount() - a.captureCount();
            }
        });
    }

    private int evaluate(GameState state) {
        if (state.getStatus() == GameState.Status.RED_WINS)
            return aiColor == Piece.Color.RED ? 10000 : -10000;
        if (state.getStatus() == GameState.Status.BLACK_WINS)
            return aiColor == Piece.Color.BLACK ? 10000 : -10000;
        if (state.getStatus() == GameState.Status.DRAW)
            return 0;

        Board board = state.getBoard();
        int score = 0;

        // Piece counts
        int aiPieces = board.countPieces(aiColor);
        int oppPieces = board.countPieces(GameState.opponent(aiColor));
        int aiKings = board.countKings(aiColor);
        int oppKings = board.countKings(GameState.opponent(aiColor));

        score += (aiPieces - oppPieces) * 100;
        score += (aiKings - oppKings) * 150;

        // Positional bonuses
        score += positionalBonus(board, aiColor);
        score -= positionalBonus(board, GameState.opponent(aiColor));

        return score;
    }

    private int positionalBonus(Board board, Piece.Color color) {
        int bonus = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece p = board.getPiece(r, c);
                if (p == null || p.getColor() != color) continue;
                // Advancement bonus
                if (!p.isKing()) {
                    int advance = (color == Piece.Color.RED) ? (Board.SIZE - 1 - r) : r;
                    bonus += advance * 5;
                }
                // Center control
                int centerDist = Math.abs(c - 3) + Math.abs(c - 4);
                bonus += (6 - centerDist) * 2;
                // Back row defense
                if ((color == Piece.Color.RED && r == Board.SIZE - 1) ||
                        (color == Piece.Color.BLACK && r == 0)) {
                    bonus += 10;
                }
            }
        }
        return bonus;
    }
}
