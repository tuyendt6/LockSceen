package com.bk.setting;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.bk.lockscreen.R;
import com.bk.lockscreen.utils.Utils;

public class GuestureFragment extends Fragment implements OnClickListener {

	private Context mContext;
	private Button mSettingGusture;
	private static final int REQUEST_CODE = 1991;
	private String path = "/sdcard/gestures";
	private final static String APP_NAME = "com.davemac327.gesture.tool";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_guesture, container,
				false);
		mSettingGusture = (Button) rootView
				.findViewById(R.id.btngesturesetting);
		mSettingGusture.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getActivity().getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btngesturesetting:
			if (!appInstalledOrNot(APP_NAME)) {
				AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
						.setTitle(
								getResources().getString(
										R.string.Install_confirm))
						.setMessage(
								getResources().getString(
										R.string.Install_question))
						.setPositiveButton(
								getResources().getString(R.string.yes),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										final String appPackageName = APP_NAME;
										try {
											startActivity(new Intent(
													Intent.ACTION_VIEW,
													Uri.parse("market://details?id="
															+ appPackageName)));
										} catch (android.content.ActivityNotFoundException anfe) {
											startActivity(new Intent(
													Intent.ACTION_VIEW,
													Uri.parse("http://play.google.com/store/apps/details?id="
															+ appPackageName)));
										}

									}
								})
						.setNegativeButton(
								getResources().getString(R.string.no),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

									}
								}).create();
				alertDialog.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

				alertDialog.show();

			} else {
				startGustureSetting();
			}
			break;

		default:
			break;
		}

	}

	private void startGustureSetting() {
		String pakageName = APP_NAME;
		PackageManager pm = mContext.getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(pakageName);
		if (launchIntent != null) {
			getActivity().startActivityForResult(launchIntent, REQUEST_CODE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == getActivity().RESULT_OK) {
				SharedPreferences preferences = mContext.getSharedPreferences(
						Utils.BACKGROUNDPREFERENCE, Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString(Utils.CURRENTGUESTURE, path);
				editor.commit();
			}
		}
	}
}
