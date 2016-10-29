package com.bk.setting;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bk.lockscreen.R;

public class NotificationSetting extends PreferenceFragment {

	private Preference mPrefNotiGroup;
	private Preference mPrefNotiAccess;
	private CheckBoxPreference mContentNotification;
	public static final String KEY_CONTENTNOTIFICATION = "show_noti_content";
	public static final String KEY_USE_SHOW_NOTI_BACKGROUND = "pref_show_noti_background";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View rootView = inflater.inflate(R.layout.customer_pref_list,
				container, false);
		addPreferencesFromResource(R.xml.pref_notification_setting);

		mContentNotification = (CheckBoxPreference) findPreference(KEY_CONTENTNOTIFICATION);
		mContentNotification.setChecked(PreferenceManager
				.getDefaultSharedPreferences(getActivity()).getBoolean(
						KEY_CONTENTNOTIFICATION, false));

		mPrefNotiAccess = (Preference) findPreference("pref_noti_access");
		mPrefNotiAccess
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						startActivity(new Intent(
								"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
						return false;
					}
				});

		mPrefNotiGroup = (Preference) findPreference("pref_noti_group");
		mPrefNotiGroup
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getActivity(),
								GroupNotiActivity.class));
						return false;
					}
				});

		return rootView;
	}
}
