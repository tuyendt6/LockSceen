package com.bk.android.visualeffect.lock.particle;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
/** @hide */
public class Particle {
    
    private float x, y;
    private int rad;
    private float dx, dy;
    private float gravity = 4;
    private Paint paint;
    private float maxSpeed = 7.0f;
    private int smallRadius = 25;
    private int bigRadius = 66;
    private int life;
    private int dotAlpha = 200;
    private int randomTotal = 20;
    private boolean isAlive = false;
    private boolean isUnlocked = false;

    public Particle(float ratio) {
        gravity *= ratio;
        maxSpeed *= ratio;
        smallRadius *= ratio;
        bigRadius *= ratio;
        paint = new Paint();
        paint.setAntiAlias(true);
    }
    
    public void initialize(float x, float y, int color) {
        Random rnd = new Random();
        this.life = 50 + rnd.nextInt(100);
        
        float rndTotal = (float) rnd.nextInt(randomTotal) / randomTotal;
        rad = (rnd.nextInt(10) == 0)? (int)(bigRadius * rndTotal) : (int)(smallRadius * rndTotal);
        dx = (float) (maxSpeed * Math.random() - maxSpeed / 2);
        dy = (float) (maxSpeed * Math.random() - maxSpeed / 2 - gravity);
        
        this.isAlive = true;
        isUnlocked = false;
        this.x = x;
        this.y = y;
        paint.setColor(color);
        
    }
    
    public void move() {
        x += dx;
        y += dy;
    }
    
    public void unlock(float speed) {
        isUnlocked = true;
        dx *= speed;
        dy *= speed;
        life = 19;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public int getTop() {
        return (int)(y - rad);
    }
    
    public int getBottom() {
        return (int)(y + rad);
    }
    
    public int getLeft() {
        return (int)(x - rad);
    }
    
    public int getRight() {
        return (int)(x + rad);
    }
    
    public void draw(Canvas canvas) {
        int alphaReduceStartFrame = (isUnlocked)? 20 :  30;
        int alpha = (life < alphaReduceStartFrame)? dotAlpha * life / alphaReduceStartFrame : dotAlpha;
        paint.setAlpha(alpha);
        canvas.drawCircle(x, y, rad, paint);
        if (life <= 0) {
            isAlive = false;
        } else {
            life --;
        }
    }
}