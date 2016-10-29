package com.bk.lockscreen.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.model.MyNotification;
import com.bk.setting.NotificationSetting;
import com.bk.table.tblAplicationInGroup;
import com.bk.table.tblNotification;

public class NotificationService extends NotificationListenerService {
	private Context context;
	public static String NOTIFI_POSTED = "tuyenpx.notification.posted";
	private static final String TAG = "NotificationService";

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	// http://stackoverflow.com/questions/22382148/0-processes-and-1-service-under-settings-apps-and-runnings
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Intent restartServiceIntent = new Intent(getApplicationContext(),
				this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(
				getApplicationContext(), 1, restartServiceIntent,
				PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 1000,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		String pack = sbn.getPackageName();
		Log.e(TAG, "received package name : " + pack);
		if (ignoreNotification(pack)) {
			Bundle extras = sbn.getNotification().extras;
			String title = extras.getString("android.title");
			String text = extras.getCharSequence("android.text") == null ? ""
					: extras.getCharSequence("android.text").toString();
			if (text.trim().equals("")) {
				CharSequence[] lines = extras
						.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);

				for (CharSequence msg : lines) {
					if (!TextUtils.isEmpty(msg)) {
						Log.e(TAG, "tuyenpx : msg = " + msg);

						if (isShowContent(pack)) {
							text = "New Messages";
							break;
						} else {
							if (text.trim().equals("")) {
								text = text + msg.toString() + "\n";
							} else {
								text = text + msg.toString() + "\n";
							}
						}
					}
				}
				text = text.trim();
			} else {
				if (isShowContent(pack)) {
					text = "New Messages";
				} else {
					text = text.trim();
				}
			}
			int android_icon = extras.getInt("android.icon");
			Utils.hashMap.put(sbn.getId(), sbn.getNotification().contentIntent);
			MyNotification myNotification = new MyNotification(1, sbn.getId(),
					pack, "", title, text, getGroupId(pack), sbn.getPostTime()
							+ "", android_icon);
			Log.e(TAG, "create my notification : " + myNotification.toString());
			insertNotification2Database(myNotification);
		}
	}

	private boolean isShowContent(String pack) {
		boolean flagship = false;

		boolean flag = PreferenceManager.getDefaultSharedPreferences(
				getBaseContext()).getBoolean(
				NotificationSetting.KEY_CONTENTNOTIFICATION, false);

		if (!flag
				&& (pack.equals("com.android.mms")
						|| pack.equals("com.skype.raider")
						|| pack.equals("com.zing.zalo")
						|| pack.equals("com.whatsapp")
						|| pack.equals("com.viber.voip")
						|| pack.equals("com.sec.chaton") || pack
							.equals("com.facebook.orca"))) {
			flagship = true;
		}
		return flagship;
	}

	private void insertNotification2Database(MyNotification myNotification) {
		Cursor c = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION, null,
				tblNotification.NOTIFICATION_ID + "=?",
				new String[] { myNotification.getNotificationID() + "" }, null);
		int n = c.getCount();
		c.close();
		if (n > 0
				&& (!myNotification.getPakageNam()
						.equals("com.facebook.katana"))) {
			getContentResolver().delete(
					SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
					tblNotification.NOTIFICATION_ID + " =?",
					new String[] { myNotification.getNotificationID() + "" });
			addNewNotification(myNotification);
			Log.e(TAG, "update notification : " + myNotification.toString());
		} else {
			addNewNotification(myNotification);
		}
	}

	private void addNewNotification(MyNotification myNotification) {
		ContentValues contentValues = new ContentValues();
		contentValues
				.put(tblNotification.GROUP_ID, myNotification.getGroupID());
		contentValues.put(tblNotification.NOTIFICATION_ID,
				myNotification.getNotificationID());
		contentValues.put(tblNotification.TIME_NOTIFICATION,
				myNotification.getTime());
		contentValues.put(tblNotification.TITLE_NOTIFICATION,
				myNotification.getTitle());
		contentValues.put(tblNotification.CONTENT_NOTIFICATION,
				myNotification.getText());
		contentValues.put(tblNotification.PACKAGE_NAME,
				myNotification.getPakageNam());
		contentValues.put(tblNotification.ICON_NOTIFICATION,
				myNotification.getIcon());
		contentValues.put(tblNotification.COUNT_NOTIFICATION, 1);
		getContentResolver().insert(
				SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
				contentValues);
		Log.e(TAG, "insert notification : " + myNotification.toString());
	}

	private int getGroupId(String packagename) {
		Cursor c = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
				null, tblAplicationInGroup.PACKAGE + " =?",
				new String[] { packagename }, null);
		int n = -1;
		if (c.getCount() > 0) {
			c.moveToFirst();
			n = c.getInt(c.getColumnIndex(tblAplicationInGroup.GROUP_ID));
		}
		c.close();
		if (n != -1) {
			return n;
		}

		return 10;
	}
	
	private boolean ignoreNotification(String packageName) {
		if (packageName.equals("com.android.systemui")
				|| packageName.equals("android")|| packageName.equals("com.samsung.android.securitylogagent")) {
			return false;
		}
		return true;
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {

		int n = getGroupId(sbn.getPackageName());
		// should not delete notification in priority and
		if (n != 1 && n != 2) {
			getContentResolver().delete(
					SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
					tblNotification.NOTIFICATION_ID + " =?",
					new String[] { sbn.getId() + "" });
		}
	}

}
