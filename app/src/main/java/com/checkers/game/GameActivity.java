package com.checkers.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends Activity {

    public static final String EXTRA_DIFFICULTY = "difficulty";

    private BoardView boardView;
    private TextView statusText;
    private TextView redScoreText;
    private TextView blackScoreText;

    private GameState gameState;
    private CheckersAI ai;
    private CheckersAI.Difficulty difficulty;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        boardView = (BoardView) findViewById(R.id.boardView);
        statusText = (TextView) findViewById(R.id.statusText);
        redScoreText = (TextView) findViewById(R.id.redScoreText);
        blackScoreText = (TextView) findViewById(R.id.blackScoreText);

        String diffName = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        difficulty = diffName != null
                ? CheckersAI.Difficulty.valueOf(diffName)
                : CheckersAI.Difficulty.MEDIUM;

        if (getActionBar() != null) {
            String name = difficulty.name();
            getActionBar().setTitle(name.charAt(0) + name.substring(1).toLowerCase() + " Mode");
        }

        startNewGame();

        findViewById(R.id.btnNewGame).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startNewGame();
            }
        });
    }

    private void startNewGame() {
        gameState = new GameState();
        ai = new CheckersAI(difficulty, Piece.Color.BLACK);
        boardView.setGameState(gameState);
        boardView.setPlayerTurn(true);
        boardView.setMoveListener(new BoardView.MoveListener() {
            @Override
            public void onMoveMade(Move move) {
                onPlayerMove(move);
            }
        });
        updateStatus();
        updateScore();
    }

    private void onPlayerMove(Move move) {
        if (gameState.getStatus() != GameState.Status.ONGOING) return;
        gameState.applyMove(move);
        boardView.setGameState(gameState);
        updateScore();

        if (gameState.getStatus() != GameState.Status.ONGOING) {
            showEndDialog();
            return;
        }
        updateStatus();
        boardView.setPlayerTurn(false);
        runAI();
    }

    private void runAI() {
        statusText.setText(R.string.ai_thinking);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final Move aiMove = ai.chooseMove(gameState);
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (aiMove != null && gameState.getStatus() == GameState.Status.ONGOING) {
                            gameState.applyMove(aiMove);
                            boardView.setGameState(gameState);
                            updateScore();
                        }
                        boardView.setPlayerTurn(true);
                        if (gameState.getStatus() != GameState.Status.ONGOING) {
                            showEndDialog();
                        } else {
                            updateStatus();
                        }
                    }
                }, 300);
            }
        });
    }

    private void updateStatus() {
        String turn = gameState.getCurrentTurn() == Piece.Color.RED ? "Your turn (Red)" : "AI thinking...";
        statusText.setText(turn);
    }

    private void updateScore() {
        Board board = gameState.getBoard();
        redScoreText.setText("Red: " + board.countPieces(Piece.Color.RED));
        blackScoreText.setText("Black: " + board.countPieces(Piece.Color.BLACK));
    }

    private void showEndDialog() {
        String msg;
        switch (gameState.getStatus()) {
            case RED_WINS:   msg = "You win!"; break;
            case BLACK_WINS: msg = "AI wins!"; break;
            default:         msg = "Draw!"; break;
        }
        statusText.setText(msg);
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(msg)
                .setPositiveButton("Play Again", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int w) {
                        startNewGame();
                    }
                })
                .setNegativeButton("Menu", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int w) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
