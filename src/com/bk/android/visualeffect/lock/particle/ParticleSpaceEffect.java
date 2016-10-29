package com.bk.android.visualeffect.lock.particle;

import java.util.HashMap;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bk.android.visualeffect.EffectCmdType;
import com.bk.android.visualeffect.EffectDataObj;
import com.bk.android.visualeffect.IEffectListener;
import com.bk.android.visualeffect.IEffectView;
import com.bk.setting.MyParcelable;

public class ParticleSpaceEffect extends FrameLayout implements IEffectView {

	private final String TAG = "ParticleSpaceEffect";

	private final int CREATED_DOTS_AMOUNT_MOVE = 3;
	private final int CREATED_DOTS_AMOUNT_DOWN = 15;
	private final int CREATED_DOTS_AMOUNT_AFFORDANCE = 50;

	private float currentX;
	private float currentY;
	private float centerX;
	private float centerY;
	private float stageWidth;
	private float stageHeight;
	private float stageRatio;

	private int affordanceColor;
	private float affordanceX;
	private float affordanceY;
	private boolean isUnlocked = false;

	private Context mContext;
	private Bitmap mBgBitmap;
	private ParticleEffect particleEffect;
	private Runnable affordanceRunnable;
	private ImageView mLockscreenWallpaperImage;

	public ParticleSpaceEffect(Context context) {
		super(context);
		mContext = context;
		particleSpaceInit();
	}

	public ParticleSpaceEffect(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		particleSpaceInit();
	}

	public ParticleSpaceEffect(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		particleSpaceInit();
	}

	private void particleSpaceInit() {
		Log.d(TAG, "tran.thang particleSpaceInit");
		resetOrientation();

		mLockscreenWallpaperImage = new ImageView(mContext);
		mLockscreenWallpaperImage.setScaleType(ScaleType.CENTER_CROP);
		mLockscreenWallpaperImage.setDrawingCacheEnabled(true);
		addView(mLockscreenWallpaperImage, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		particleEffect = new ParticleEffect(mContext);
		addView(particleEffect);
	}

	private void resetOrientation() {
		Log.d(TAG, "tran.thang resetOrientation");
		DisplayMetrics dm = getResources().getDisplayMetrics();
		stageWidth = dm.widthPixels;
		stageHeight = dm.heightPixels;
		Log.d(TAG, "stage : " + stageWidth + " x " + stageHeight);
		centerX = stageWidth / 2;
		centerY = stageHeight / 2;
		stageRatio = stageWidth / stageHeight;
		if (particleEffect != null)
			particleEffect.clearEffect();
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "tran.thang onConfigurationChanged");
		resetOrientation();
	}

	private void clearEffect() {
		Log.d(TAG, "tran.thang clearEffect");
		currentX = centerX;
		currentY = centerY;

		particleEffect.clearEffect();
	}

	private int getColor(float x, float y) {
		// Log.d(TAG, "tran.thang getColor : ("+x+", " +y+")");
		int color = 0xFFFFFF;
		if (x <= 0 || x > stageWidth)
			return color;
		if (y <= 0 || y > stageHeight)
			return color;

		if (mBgBitmap == null) {
			Log.d(TAG, "tran.thang getColor : mBgBitmap = null");
		} else {
			int bitmapWidth = mBgBitmap.getWidth();
			int bitmapHeight = mBgBitmap.getHeight();
			float bitmapRatio = (float) bitmapWidth / (float) bitmapHeight;
			float ratio, resizedStageWidth, resizedStageHeight;
			int offsetX = 0;
			int offsetY = 0;
			int finalX, finalY;
			if (bitmapRatio > stageRatio) {
				// It is horizontally wider than screen, so crop left & right
				// side
				ratio = bitmapHeight / stageHeight;
				resizedStageWidth = stageWidth * ratio;
				resizedStageHeight = bitmapHeight;
				offsetX = (int) ((bitmapWidth - resizedStageWidth) / 2);
			} else {
				// It is vertically wider than screen, so crop top & bottom side
				ratio = bitmapWidth / stageWidth;
				resizedStageWidth = bitmapWidth;
				resizedStageHeight = stageHeight * ratio;
				offsetY = (int) ((bitmapHeight - resizedStageHeight) / 2);
			}
			finalX = offsetX + (int) (x * ratio);
			finalY = offsetY + (int) (y * ratio);
			if (finalX < 0)
				finalX = 0;
			if (finalX >= bitmapWidth)
				finalX = bitmapWidth - 1;
			if (finalY < 0)
				finalY = 0;
			if (finalY >= bitmapHeight)
				finalY = bitmapHeight - 1;

			try {
				color = mBgBitmap.getPixel(finalX, finalY);
			} catch (IllegalArgumentException e) {
				Log.d(TAG, "tran.thang getColor : IllegalArgumentException = "
						+ e.toString());
				Log.d(TAG,
						"tran.thang getColor : bitmap = "
								+ mBgBitmap.getWidth() + " x "
								+ mBgBitmap.getHeight());
				Log.d(TAG, "tran.thang getColor : stageWidth = " + stageWidth
						+ ", stageHeight =  " + stageHeight);
				Log.d(TAG, "tran.thang getColor : x = " + x + ", y =  " + y);
			}
		}
		return color;
	}

	private void unlock() {
		Log.d(TAG, "tran.thang unlock");
		isUnlocked = true;
		particleEffect.unlockDots();
		long unlockDuration = 300 + 50;

		this.animate().setDuration(unlockDuration)
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						unlockFinished();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						Log.d(TAG, "unlock : onAnimationCancel");
					}
				});
	}

	private void unlockFinished() {
		Log.d(TAG, "tran.thang unlockFinished");
		clearEffect();
	}

	private void setBGBitmap() {
		MyParcelable myParcelable = MyParcelable.getInstance();
		mBgBitmap = myParcelable.getBitmap("data");
		if (mLockscreenWallpaperImage != null) {
			mLockscreenWallpaperImage.setImageBitmap(mBgBitmap);
		}
	}

	private void showAffordanceEffect(long startDelay, Rect rect) {
		Log.d(TAG, "showUnlockAffordance : " + rect.left + ", " + rect.right
				+ ", " + rect.top + ", " + rect.bottom + ", startDelay : "
				+ startDelay);

		affordanceX = rect.left + (rect.right - rect.left) / 2;
		affordanceY = rect.top + (rect.bottom - rect.top) / 2;
		affordanceColor = getColor(affordanceX, affordanceY);
		affordanceRunnable = new Runnable() {
			public void run() {
				particleEffect.addDots(CREATED_DOTS_AMOUNT_AFFORDANCE,
						affordanceX, affordanceY, affordanceColor);
				affordanceRunnable = null;
			}
		};
		this.postDelayed(affordanceRunnable, startDelay);
	}

	@Override
	public void init(EffectDataObj data) {
		Log.d(TAG, "tran.thang init");
		clearEffect();
	}

	@Override
	public void reInit(EffectDataObj data) {
	}

	@Override
	public void handleCustomEvent(int cmd, HashMap<?, ?> params) {
		Log.d(TAG, "tran.thang handleCustomEvent" + cmd);
		if (cmd == EffectCmdType.SETBG) {
			setBGBitmap();
		} else if (cmd == EffectCmdType.LOCKAFFORDANCE) {
			showAffordanceEffect((Long) params.get("StartDelay"),
					(Rect) params.get("Rect"));
		} else if (cmd == EffectCmdType.UNLOCK) {
			unlock();
		}
	}

	@Override
	public void setListener(IEffectListener listener) {
	}

	@Override
	public void removeListener() {
	}

	@Override
	public void handleTouchEvent(MotionEvent event, View view) {
		// Log.d(TAG, "tran.thang handleTouchEvent");
		currentX = event.getRawX();
		currentY = event.getRawY();
		// Log.d(TAG, "tran.thang handleTouchEvent: (" + currentX + ", " +
		// currentY +")");
		int color = getColor(currentX, currentY);

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			// Log.d(TAG, "tran.thang handleTouchEvent : ACTION_DOWN");
			isUnlocked = false;

			particleEffect.addDots(CREATED_DOTS_AMOUNT_DOWN, currentX,
					currentY, color);

		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
				&& event.getActionIndex() == 0) {
			// Log.d(TAG, "tran.thang handleTouchEvent : ACTION_MOVE");
			if (!isUnlocked) {
				particleEffect.addDots(CREATED_DOTS_AMOUNT_MOVE, currentX,
						currentY, color);
			}
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP
				|| event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
			// Log.d(TAG,
			// "tran.thang handleTouchEvent : ACTION_UP || ACTION_CANCEL");

			currentX = centerX;
			currentY = centerY;
		}
	}

	@Override
	public void clearScreen() {
		clearEffect();
	}
}
