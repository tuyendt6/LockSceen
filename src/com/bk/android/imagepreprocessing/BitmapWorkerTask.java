package com.bk.android.imagepreprocessing;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import com.bk.android.visualeffect.EffectCmdType;
import com.bk.android.visualeffect.lock.particle.ParticleSpaceEffect;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "BitmapWorkerTask";
	private final WeakReference<ParticleSpaceEffect> particleSpaceEffectReference;
	private String pathName = "";
	int width = 0, height = 0;
	private Context mContext;

	public BitmapWorkerTask(Context mContext, ParticleSpaceEffect frameLayout, int width, int height) {
		// Use a WeakReference to ensure the frameLayout can be garbage
		// collected
		this.mContext = mContext;
		this.width = width;
		this.height = height;
		particleSpaceEffectReference = new WeakReference<ParticleSpaceEffect>(frameLayout);
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		// Log.d(TAG, "tran.thang doInBackground " + params[1]);
		pathName = ("0".equalsIgnoreCase(params[1]))?params[0]:params[1];
		if("0".equalsIgnoreCase(params[1])){
			return ImagePreprocessing.decodeSampledBitmapFromResource(pathName, width/2, height/2);
		}else{
			return ImagePreprocessing.decodeSampledBitmapFromResource(mContext.getResources(), Integer.parseInt(params[0]), width/2, height/2);
		}
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (particleSpaceEffectReference != null && bitmap != null) {
			bitmap = getPreferredConfigBitmap(bitmap,
					Bitmap.Config.ARGB_8888);
			final ParticleSpaceEffect frameLayout = particleSpaceEffectReference.get();
			if (frameLayout != null) {
				HashMap<String, Bitmap> map = new HashMap<String, Bitmap>();
				map.put("BGBitmap", bitmap);
				frameLayout.handleCustomEvent(EffectCmdType.SETBG, map);
			}
		}
	}
	
	public Bitmap getPreferredConfigBitmap(Bitmap srcBitmap, Bitmap.Config config) {
		if (srcBitmap == null)
			return null;

		if (srcBitmap.getConfig() == config)
			return srcBitmap;

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		if (width <= 0 || height <= 0)
			return null;

		Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), config);
		Canvas destCanvas = new Canvas(destBitmap);
		destCanvas.drawBitmap(srcBitmap, 0, 0, null);

		return destBitmap;
	}
}

