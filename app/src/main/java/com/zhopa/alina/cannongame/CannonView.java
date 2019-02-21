package com.zhopa.alina.cannongame;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = "CannonView";

    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;

    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    private CannonThread cannonThread;
    private Activity activity;
    private boolean dialogIsDisplayed = false;

    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    private int screenWidth, screenHeight;

    private boolean gameOver;
    private double timeLeft, totalElapsedTime;
    private int shotsFired;

    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool;
    private SparseIntArray soundMap;

    private Paint textPaint, backgroundPaint;


    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;

        getHolder().addCallback(this);
        AudioAttributes.Builder attrsBuilder = new AudioAttributes.Builder();
        attrsBuilder.setUsage(AudioAttributes.USAGE_GAME);

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrsBuilder.build());
        soundPool = builder.build();
        soundMap = new SparseIntArray(3);
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void playSound(int soundID) {
        soundPool.play(soundMap.get(soundID), 1, 1, 1, 0, 1f);
    }

    public void newGame() {
        cannon = new Cannon(this,
                (int)(CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int)(CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int)(CANNON_BARREL_WIDTH_PERCENT * screenHeight));
        Random random = new Random();
        targets = new ArrayList<>();

        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);
        int targetY = (int) ((0.5 - TARGET_LENGTH_PERCENT/2) * screenHeight);

        for (int n = 0; n < TARGET_PIECES; n++) {
            double velocity = screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT)
                    + TARGET_MIN_SPEED_PERCENT);
            int color = (n%2 == 0) ?
                    getResources().getColor(R.color.dark, getContext().getTheme()) :
                    getResources().getColor(R.color.light, getContext().getTheme());
            velocity *= -1; // TODO: insert into if block?

            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth),
                    (int) (TARGET_LENGTH_PERCENT * screenHeight),
                    (int) (velocity)));

            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                (float) (BLOCKER_SPEED_PERCENT * screenHeight));

        timeLeft = 10;
        shotsFired = 0;
        totalElapsedTime = 0.0;

        if (gameOver) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }

        hideSystemBars();
    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0;

        if (cannon.getCannonBall() != null)
            cannon.getCannonBall().update(interval);

        for (GameElement target : targets)
            target.update(interval);

        timeLeft -= interval;

        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.lose);
        }
        if (targets.isEmpty()) {
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.win);
            gameOver = true;
        }
    }

    public void alignAndFireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        double centerMinusY = (screenHeight / 2 - touchPoint.y);
        double angle = 0;
        angle = Math.atan2(touchPoint.x, centerMinusY);
        cannon.align(angle);

        if (cannon.getCannonBall() == null || !cannon.getCannonBall().isOnScreen()) {
            cannon.fireCannonball();
            ++shotsFired;
        }
    }

    public void showGameOverDialog(final int messageID) {
        final DialogFragment gameResult = new DialogFragment() {
            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(messageID)); // NOT FINISHED
            }
        };
    }
}
