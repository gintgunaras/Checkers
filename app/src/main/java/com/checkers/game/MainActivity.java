package com.checkers.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

    private RadioGroup difficultyGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultyGroup = (RadioGroup) findViewById(R.id.difficultyGroup);

        TextView versionText = (TextView) findViewById(R.id.versionText);
        if (versionText != null) versionText.setText("v1.0");

        findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void startGame() {
        int checkedId = difficultyGroup.getCheckedRadioButtonId();
        CheckersAI.Difficulty difficulty;

        if (checkedId == R.id.rbBeginner)      difficulty = CheckersAI.Difficulty.BEGINNER;
        else if (checkedId == R.id.rbEasy)     difficulty = CheckersAI.Difficulty.EASY;
        else if (checkedId == R.id.rbHard)     difficulty = CheckersAI.Difficulty.HARD;
        else if (checkedId == R.id.rbExpert)   difficulty = CheckersAI.Difficulty.EXPERT;
        else                                   difficulty = CheckersAI.Difficulty.MEDIUM;

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name());
        startActivity(intent);
    }
}
