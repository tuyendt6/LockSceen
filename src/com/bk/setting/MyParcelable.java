package com.bk.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class MyParcelable implements Parcelable {

	private static final String TAG = "MyParcelable";

	public static Bundle mBundle;

	private static MyParcelable sMyParcelable;

	public static MyParcelable getInstance() {
		if (sMyParcelable != null) {
			return sMyParcelable;
		}
		return new MyParcelable();
	}

	public MyParcelable() {
		if (mBundle == null) {
			mBundle = new Bundle();
		}
	}

	public boolean containsKey(String key) {
		return mBundle.containsKey(key);
	}

	public CharSequence getText(String key) {
		return mBundle.getCharSequence(key);
	}

	public String getString(String key) {
		CharSequence text = getText(key);
		if (text != null) {
			return text.toString();
		}
		return null;
	}

	public Bitmap getBitmap(String key) {
		Bitmap bmp = null;
		bmp = mBundle.getParcelable(key);
		return bmp;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeBundle(mBundle);
	}

	public void putBitmap(String key, Bitmap value) {
		if (mBundle != null) {
			mBundle = null;
		}
		mBundle = new Bundle();
		mBundle.putParcelable(key, value);
	}
}
