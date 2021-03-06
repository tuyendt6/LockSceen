package com.bk.lockscreen.utils;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bk.lockscreen.R;

public class LockscreenUtils {

	private OverlayDialog mOverlayDialog;

	private static LockscreenUtils sLockScreanUtils;

	public static LockscreenUtils getInstance() {

		if (sLockScreanUtils == null) {
			sLockScreanUtils = new LockscreenUtils();
		}
		return sLockScreanUtils;
	}

	public void lock(Activity activity) {
		mOverlayDialog = OverlayDialog.getInstance(activity);
		if (!mOverlayDialog.isShowing()) {
			mOverlayDialog.show();
		}
	}

	public void unlock() {

		if (mOverlayDialog != null && mOverlayDialog.isShowing()) {
			mOverlayDialog.dismiss();
			mOverlayDialog = null;
		}
	}

	private static class OverlayDialog extends AlertDialog {

		public static OverlayDialog sOverlay;

		public static OverlayDialog getInstance(Activity activity) {
			if (sOverlay == null) {
				sOverlay = new OverlayDialog(activity);
			}
			return sOverlay;
		}

		public OverlayDialog(Activity activity) {

			super(activity, R.style.OverlayDialog);

			WindowManager.LayoutParams params = getWindow().getAttributes();
			params.type = TYPE_SYSTEM_ALERT;
			params.dimAmount = 0.0F;
			params.width = 0;
			params.height = 0;
			params.gravity = Gravity.BOTTOM;
			getWindow().setAttributes(params);
			getWindow().setFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_NOT_TOUCH_MODAL,
					0xffffff);
			setOwnerActivity(activity);
			setCancelable(false);

		}

		public final boolean dispatchTouchEvent(MotionEvent motionevent) {
			return true;
		}

		protected final void onCreate(Bundle bundle) {

			super.onCreate(bundle);

			FrameLayout framelayout = new FrameLayout(getContext());
			framelayout.setBackgroundColor(0);
			setContentView(framelayout);

		}
	}
}
