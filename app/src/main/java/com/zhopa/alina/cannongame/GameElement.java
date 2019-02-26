package com.zhopa.alina.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameElement {
    protected CannonView view;
    protected Paint paint = new Paint();
    protected Rect shape;
    private float velocityY;
    private int soundID;

    public GameElement(CannonView view, int color, int soundID, int x, int y,
                        int width, int length, float velocityY) {
        this.view = view;
        this.velocityY = velocityY;
        this.soundID = soundID;
        shape = new Rect(x, y, x+width, y+length);
        paint.setColor(color);
    }

    public void update(double interval) {
        shape.offset(0, (int) (velocityY * interval));

        if (shape.top <0 && velocityY <0 || shape.bottom >view.getScreenHeight() && velocityY >0)
            velocityY *= -1;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    public void playSound() {
        view.playSound(soundID);
    }
}
