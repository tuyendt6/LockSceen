package com.bk.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.bk.lockscreen.LockScreenActivity;
import com.bk.lockscreen.R;
import com.bk.lockscreen.utils.Utils;

public class BasicSetting extends PreferenceFragment {

	private Preference mPreDisplayTimeOut;
	private Preference mPreWallPaper;
	private CheckBoxPreference mUseSmartLockScreen;
	private ColorPickerPreference mColorPickerPreference;
	private CheckBoxPreference mUseSmartON;
	private CheckBoxPreference mUseSmartOFF;
	private CheckBoxPreference mSoundUnLock;
	private Preference mReset2Default;

	private static final int CHANGE_BACK_GROUND = 1404;

	public static final String KEY_USE_SMART_LOCKSCREEN = "pref_use_lockscreen";
	public static final String KEY_USE_SMART_OFF = "pref_turn_off_lockscreen";
	public static final String KEY_USE_SMART_ON = "pref_turn_on_lockscreen";
	private static final String KEY_CHANGE_WALLPAPER = "pref_wallpaper";
	public static final String KEY_SOUND_UNLOCK = "pref_sound_unlock";
	public static final String KEY_RESET_2_DEFAULT = "pref_reset2default";
	private static final String TAG = "BasicSetting";
	public static final String COLOR_DIALOG = "color";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		addPreferencesFromResource(R.xml.pref_basic_setting);
		mPreWallPaper = (Preference) findPreference(KEY_CHANGE_WALLPAPER);
		mUseSmartLockScreen = (CheckBoxPreference) findPreference(KEY_USE_SMART_LOCKSCREEN);

		mUseSmartON = (CheckBoxPreference) findPreference(KEY_USE_SMART_ON);
		mUseSmartOFF = (CheckBoxPreference) findPreference(KEY_USE_SMART_OFF);
		mSoundUnLock = (CheckBoxPreference) findPreference(KEY_SOUND_UNLOCK);
		mColorPickerPreference = (ColorPickerPreference) findPreference(COLOR_DIALOG);
		mReset2Default = (Preference) findPreference(KEY_RESET_2_DEFAULT);
		mReset2Default
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						AlertDialog alertDialog = new AlertDialog.Builder(
								getActivity())
								.setTitle(
										getResources().getString(
												R.string.reset_to_default))
								.setMessage(
										getResources().getString(
												R.string.ques_reset_to_defaut))
								.setPositiveButton(
										getResources().getString(R.string.yes),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												reset2defalt();

											}
										})
								.setNegativeButton(
										getResources().getString(R.string.no),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {

											}
										}).create();
						alertDialog.getWindow().setType(
								WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

						alertDialog.show();
						return false;
					}
				});
		mUseSmartLockScreen.setChecked(PreferenceManager
				.getDefaultSharedPreferences(getActivity()).getBoolean(
						KEY_USE_SMART_LOCKSCREEN, true));

		mUseSmartON.setChecked(PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getBoolean(KEY_USE_SMART_ON, true));
		mUseSmartOFF.setChecked(PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getBoolean(KEY_USE_SMART_OFF, true));

		mSoundUnLock.setChecked(PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getBoolean(KEY_SOUND_UNLOCK, true));
		mPreWallPaper
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Auto-generated method stub
						Intent i = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i, CHANGE_BACK_GROUND);
						return false;
					}
				});
		mPreDisplayTimeOut = (Preference) findPreference("pref_time_out");
		mPreDisplayTimeOut
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(
								Settings.ACTION_DISPLAY_SETTINGS));
						Toast.makeText(getActivity(),
								"press on Screentimeout to change",
								Toast.LENGTH_LONG).show();
						return false;
					}
				});
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private void reset2defalt() {
		SharedPreferences preferences = getActivity().getSharedPreferences(
				Utils.BACKGROUNDPREFERENCE, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(Utils.CURRENTBACKGROUND, "");
		editor.putString(Utils.CURRENTGUESTURE, "");
		editor.commit();
		System.exit(0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHANGE_BACK_GROUND) {
			if (resultCode == getActivity().RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor c = getActivity().getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				c.moveToFirst();
				String imageUri = c.getString(c
						.getColumnIndexOrThrow(filePathColumn[0]));
				c.close();
				SharedPreferences preferences = getActivity()
						.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
								Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(Utils.CURRENTBACKGROUND, imageUri);
				editor.commit();

				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay()
						.getMetrics(dm);
				int width = dm.widthPixels;
				int height = dm.heightPixels;

				// Log.d(TAG, "tran.thang width = " + width + ", height = " +
				// height);
				// switch(dm.densityDpi){
				// case DisplayMetrics.DENSITY_LOW:
				// Log.d(TAG, "tran.thang DENSITY_LOW");
				// break;
				// case DisplayMetrics.DENSITY_MEDIUM:
				// Log.d(TAG, "tran.thang DENSITY_MEDIUM");
				// break;
				// case DisplayMetrics.DENSITY_HIGH:
				// Log.d(TAG, "tran.thang DENSITY_HIGH");
				// break;
				// }
				Bitmap bitmapTemp;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				bitmapTemp = BitmapFactory.decodeFile(imageUri, options);
				options.inSampleSize = calculateInSampleSize(options, width,
						height);
				options.inJustDecodeBounds = false;
				bitmapTemp = BitmapFactory.decodeFile(imageUri, options);
				MyParcelable bitmapBG = MyParcelable.getInstance();
				bitmapBG.putBitmap("data", bitmapTemp);
				Intent i = new Intent(getActivity(), LockScreenActivity.class);
				startActivity(i);
				getActivity().finish();
				if (bitmapTemp != null) {
					bitmapTemp = null;
				}

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public Bitmap getPreferredConfigBitmap(Bitmap srcBitmap,
			Bitmap.Config config) {
		if (srcBitmap == null)
			return null;

		if (srcBitmap.getConfig() == config)
			return srcBitmap;

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		if (width <= 0 || height <= 0)
			return null;

		Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),
				srcBitmap.getHeight(), config);
		Canvas destCanvas = new Canvas(destBitmap);
		destCanvas.drawBitmap(srcBitmap, 0, 0, null);

		return destBitmap;
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
}
