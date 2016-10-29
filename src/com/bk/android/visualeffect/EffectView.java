package com.bk.android.visualeffect;

import java.util.HashMap;

import com.bk.android.visualeffect.lock.particle.ParticleSpaceEffect;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class EffectView extends FrameLayout implements IEffectView {

	private IEffectView mView;
	private Context mContext;
	private final String TAG = "EffectView";
	private int mEffectType;

	public EffectView(Context context) {
		super(context);
		mContext = context;
	}

	public EffectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public EffectView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
	}

	public void setEffect(String effect) {
		if (mView != null) {
			Log.d(TAG, "setEffect : Current mView is " + mView.getClass());
			this.removeAllViews();
		}
	}

	public void setEffect(int effect) {
		if (mView != null) {
			Log.d(TAG, "setEffect : Current mView is " + mView.getClass());
			this.removeAllViews();
		}

		mView = new ParticleSpaceEffect(mContext);
		this.addView((View) mView);
		mEffectType = effect;
	}

	public int getEffect() {
		if (mView == null) {
			Log.d(TAG, "getEffect : Current mView is " + null);
			return -1;
		}

		return mEffectType;
	}

	/** @hide */
	@Override
	public void init(EffectDataObj data) {
		if (mView == null) {
			Log.d(TAG, "setInitValues : mView is null");
			return;
		}
		mView.init(data);
	}

	/** @hide */
	@Override
	public void reInit(EffectDataObj data) {
		if (mView == null) {
			Log.d(TAG, "reInitAndValues : mView is null");
			return;
		}

		if (data == null)
			mView.reInit(new EffectDataObj());
		else
			mView.reInit(data);
	}

	/** @hide */
	@Override
	public void handleTouchEvent(MotionEvent event, View view) {
		// TODO Auto-generated method stub
		if (mView == null) {
			Log.d(TAG, "handleTouchEvent : mView is null");
			return;
		}
		mView.handleTouchEvent(event, view);
	}

	public void onCommand(String cmd, HashMap<?, ?> params) {
		if (cmd.contentEquals("clear")) {
			mView.clearScreen();
		} else {
			handleCustomEvent(EffectCmdType.CUSTOM_CMD, params);
		}
	}

	@Override
	public void handleCustomEvent(int cmd, HashMap<?, ?> params) {
		if (mView == null) {
			Log.d(TAG, "handleCustomEvent : mView is null");
			return;
		}
		if (params == null)
			mView.handleCustomEvent(cmd, new HashMap<Object, Object>());
		else
			mView.handleCustomEvent(cmd, params);
	}

	@Override
	public void clearScreen() {
		// TODO Auto-generated method stub
		if (mView == null) {
			Log.d(TAG, "clearScreen : mView is null");
			return;
		}

		mView.clearScreen();
	}

	@Override
	public void setListener(IEffectListener listener) {
		if (mView == null) {
			Log.d(TAG, "setListener : mView is null");
			return;
		}
		mView.setListener(listener);
	}

	/** @hide */
	@Override
	public void removeListener() {
		if (mView == null) {
			Log.d(TAG, "removeListener : mView is null");
			return;
		}
		mView.removeListener();
	}

	public void removeEffect() {
		this.removeAllViews();
		mView = null;
	}

}
