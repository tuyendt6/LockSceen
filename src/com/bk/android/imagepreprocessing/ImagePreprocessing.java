package com.bk.android.imagepreprocessing;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImagePreprocessing {
	private static final String TAG = "ImagePreprocessing";

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight; 
		final int width = options.outWidth; 
		Log.d(TAG, "tran.thang check Height = " + height + "width = " + width);
		Log.d(TAG, "tran.thang check "
				+ "reqHeight = " + reqHeight + "reqWidth = " + reqWidth);
		int inSampleSize = 1;   
		if (height > reqHeight || width > reqWidth) {       
			// Calculate ratios of height and width to requested height and width     
			final int heightRatio = Math.round((float) height / (float) reqHeight);     
			final int widthRatio = Math.round((float) width / (float) reqWidth);       
			// Choose the smallest ratio as inSampleSize value, this will guarantee     
			// a final image with both dimensions larger than or equal to the     
			// requested height and width.     
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		// Log.d(TAG, "tran.thang decodeSampledBitmapFromResource resId " + resId);
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		Log.d(TAG, "tran.thang calculateInSampleSize resId inSampleSize " + options.inSampleSize);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		//http://stackoverflow.com/questions/28766465/android-bitmapfactory-decoderesource-takes-too-much-memory
		//options.inScaled = false;
		
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		Log.d(TAG, "tran.thang calculateInSampleSize pathName inSampleSize " + options.inSampleSize);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		//http://stackoverflow.com/questions/28766465/android-bitmapfactory-decoderesource-takes-too-much-memory
		//options.inScaled = false;
		
		return BitmapFactory.decodeFile(pathName, options);
	}
}
