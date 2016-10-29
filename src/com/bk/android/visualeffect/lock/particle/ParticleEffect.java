package com.bk.android.visualeffect.lock.particle;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bk.android.visualeffect.EffectDataObj;
import com.bk.android.visualeffect.IEffectListener;
import com.bk.android.visualeffect.IEffectView;

/** @hide */
public class ParticleEffect extends View implements IEffectView {
    
    private String TAG = "VisualEffectParticleEffect";
    private ArrayList<Particle> particleTotalList = new ArrayList<Particle>();
    private ArrayList<Particle> particleAliveList = new ArrayList<Particle>();
    private boolean isDrawing;
    private int drawingDelayTime = 2;
    private int initCreatedDotAmount = 250;
    private int dotMaxLimit = 150;
    private int dotUnlockSpeed = 5;
    private float lastAddedX = 0;
    private float lastAddedY = 0;
    private int lastAddedColor = 0;
    private float[] hsvOrigin; 
    private float[] hsvTemp;
    private int drawingLeft = 0;
    private int drawingTop = 0;
    private int drawingRight = 1;
    private int drawingBottom = 1;
    private int drawingMargin = 11; 
    private int nextParticleIndex = -1;
    private boolean isPaused = false;

	/**
	 * Creates ParticleEffect object from View
	 *
	 * @param	context Application context used to access display's resolution
	 */
    public ParticleEffect(Context context) {
        super(context);
        hsvOrigin = new float[3];
        hsvTemp = new float[3];
        
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int smallestWidth = (screenWidth < screenHeight) ? screenWidth : screenHeight;
        
        float ratio = (float) smallestWidth / 1080;
        Log.d(TAG, "ParticleEffect : Constructor, " + screenWidth + " x " + screenHeight);
        Log.d(TAG, "ParticleEffect : ratio = " + ratio);
        
        for (int i = 0; i < initCreatedDotAmount; i++) {
            Particle dot = new Particle(ratio);
            particleTotalList.add(dot);
        }
    }
    
    /**
	 * Add Dots on ParticleEffect view
	 *
	 * @param	amount A number of dots created on screen
	 * @param	x Raw x point for dots creating
	 * @param	y Raw y point for dots creating
	 * @param	color Creating Dots' color value
	 */
    public void addDots(int amount, float x, float y, int color) {
        
        if (particleAliveList.size() + amount > dotMaxLimit) return;
        
        lastAddedX = x;
        lastAddedY = y;
        lastAddedColor = color;
        
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsvOrigin);
        
        for (int i = 0; i < amount; i++) {
            hsvTemp[0] = hsvOrigin[0];
            hsvTemp[1] = hsvOrigin[1];
            hsvTemp[2] = hsvOrigin[2];
            hsvTemp[1] *= 1f - 0.7 * Math.random();
            hsvTemp[2] += (1 - hsvTemp[2]) * Math.random();
            int resultColor = Color.HSVToColor(hsvTemp); 
            Particle dot = getNextDot();
            dot.initialize(x, y, resultColor);
            particleAliveList.add(dot);
        }
        startDrawing();
    }
    
    private void startDrawing() {
        if(isDrawing) return;
        isDrawing = true;
        mHandler.sendEmptyMessageDelayed(0, drawingDelayTime);
    }
    
    private void stopDrawing() {
        isDrawing = false;
    }
    
    private Particle getNextDot() {
        nextParticleIndex = (nextParticleIndex >= initCreatedDotAmount - 1)? 0 : nextParticleIndex + 1;
        return particleTotalList.get(nextParticleIndex);
    }
    
    public void unlockDots() {
        int totalAdded = dotMaxLimit - particleAliveList.size();
        addDots(totalAdded, lastAddedX, lastAddedY, lastAddedColor);
        
        for (Particle particle : particleAliveList) {
            particle.unlock(dotUnlockSpeed);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (particleAliveList.isEmpty()) {
            stopDrawing();
        } else {
            for (int i = 0; i < particleAliveList.size(); i++) {
                Particle dot = particleAliveList.get(i);
                
                if(dot != null && dot.isAlive()) {
                    dot.move();
                    dot.draw(canvas);
                    
                    int left = dot.getLeft();
                    int right = dot.getRight();
                    int top = dot.getTop();
                    int bottom = dot.getBottom();
                    
                    drawingLeft = i == 0 ? left : Math.min(drawingLeft, left);
                    drawingTop = i == 0 ? top : Math.min(drawingTop, top);
                    drawingRight = i == 0 ? right : Math.max(drawingRight, right);
                    drawingBottom = i == 0 ? bottom : Math.max(drawingBottom, bottom);
                } else {
                    particleAliveList.remove(i);
                    i--;
                }
            }
        }
    }
    
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (isAvailableRect()) {
                int tL = drawingLeft - drawingMargin;
                int tT = drawingTop - drawingMargin;
                int tR = drawingRight + drawingMargin;
                int tB = drawingBottom + drawingMargin;
                invalidate(tL, tT, tR, tB);
            } else {
                invalidate(0, 0, 1, 1);
            }
            if (isDrawing && !isPaused) mHandler.sendEmptyMessageDelayed(0, drawingDelayTime);
        }
    };
    
    private boolean isAvailableRect() {
        if (drawingLeft >= drawingRight){
            return false;
        } else if (drawingTop >= drawingBottom){
            return false;
        } else if (drawingLeft >= this.getWidth()){
            return false;
        } else if (drawingRight <= 0){
            return false;
        } else if (drawingTop >= this.getHeight()){
            return false;
        } else if (drawingBottom <= 0){
            return false;
        } else {
            return true;
        }
    }

    public void clearEffect() {
        stopDrawing();
        invalidate();
        for (int i = particleAliveList.size()-1; i >= 0; i--) {
            particleAliveList.remove(i);
        }
    }

    public void pauseEffect() {
        isPaused = true;
        Log.d(TAG, "ParticleEffect : pauseEffect");
    }
    
    public void resumeEffect() {
        Log.d(TAG, "ParticleEffect : resumeEffect");
        if (isPaused) {
            isPaused = false;
            if (isDrawing) {
                mHandler.sendEmptyMessageDelayed(0, drawingDelayTime);
            } else {
                clearEffect();
            }
        }
    }

    @Override
    public void init(EffectDataObj data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reInit(EffectDataObj data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleTouchEvent(MotionEvent event, View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleCustomEvent(int cmd, HashMap<?, ?> params) {
        // TODO Auto-generated method stub
        addDots((Integer) params.get("Amount"), (Float) params.get("X"), (Float) params.get("Y"),  (Integer) params.get("Color"));
    }

    @Override
    public void clearScreen() {
        // TODO Auto-generated method stub
        clearEffect();
    }

    @Override
    public void setListener(IEffectListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeListener() {
        // TODO Auto-generated method stub
        
    }

//    @Override
//    public void setInitValues(DataForEachLock data) {
//        
//    }
//
//    @Override
//    public void reInitAndValues(DataForEachLock data) {
//    }
//
//    @Override
//    public void onCommand(int cmd, HashMap<?, ?> params) {
//        if(cmd == LockCmdType.TOUCH) {
//            addDots((Integer) params.get("Amount"), (Float) params.get("X"), (Float) params.get("Y"),  (Integer) params.get("Color"));
//        }
//        else if(cmd == LockCmdType.CLEAR) {
//            clearEffect();
//        }
//        else if(cmd == LockCmdType.START) {
//            resumeEffect();
//        }
//        else if(cmd == LockCmdType.STOP) {
//            pauseEffect();
//        }
//        else if(cmd == LockCmdType.UNLOCK) {
//            unlockDots();
//        }
//    }
//
//    @Override
//    public void setListener(ILockEffectListener listener) {
//    }
//
//    @Override
//    public void removeListener() {
//    }
}
