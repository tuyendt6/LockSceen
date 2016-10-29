package com.bk.lockscreen.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.WindowManager;

import com.bk.lockscreen.LockScreenActivity;
import com.bk.setting.BasicSetting;

public class LockscreenService extends Service implements SensorEventListener {

	private static final String TAG = "LockscreenService";

	private Sensor proxSensor;
	private SensorManager sm;

	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mComponentName;
	private static final String description = "Sample Administrator description";
	private GestureDetector gestureScanner;
	private boolean isScreenOn = true;
	private long lastUpdate = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mComponentName = new ComponentName(this, MyAdminReceiver.class);
	}

	// Register for Lockscreen event intents
	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mBroadcastReceiver, filter);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		proxSensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		sm.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
		return START_STICKY;
	}

	// Unregister receiver
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		sm.unregisterListener(this);
	}

	// http://stackoverflow.com/questions/22382148/0-processes-and-1-service-under-settings-apps-and-runnings
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.e(TAG, "tran.thang onTaskRemoved");
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

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				isScreenOn = false;

				final boolean flag = PreferenceManager
						.getDefaultSharedPreferences(context).getBoolean(
								BasicSetting.KEY_USE_SMART_LOCKSCREEN, true);

				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				int callState = tm.getCallState();
				if (flag && callState != TelephonyManager.CALL_STATE_RINGING
						&& callState != TelephonyManager.CALL_STATE_OFFHOOK) {
					Log.e(TAG, "start acitivity lockscreen");
					start_lockscreen(context);
				}
			} else {
				isScreenOn = true;
			}
		}
	};

	// Display lock screen
	private void start_lockscreen(Context context) {
		Intent mIntent = new Intent(context, LockScreenActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.addFlags(WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW);
		context.startActivity(mIntent);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	private int count = 0;

	@Override
	public void onSensorChanged(SensorEvent e) {
		long curTime = System.currentTimeMillis();
		if (count == 0) {
			lastUpdate = curTime;
		}
		count++;
		if ((curTime - lastUpdate) < 300 && count == 2) {
			if (isScreenOn
					&& PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).getBoolean(
							BasicSetting.KEY_USE_SMART_ON, true)) {
				boolean isAdmin = mDevicePolicyManager
						.isAdminActive(mComponentName);
				// turn off screen :
				if (isAdmin) {
					mDevicePolicyManager.lockNow();
				}
			} else if (!isScreenOn
					&& PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).getBoolean(
							BasicSetting.KEY_USE_SMART_OFF, true)) {
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(
						PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.ACQUIRE_CAUSES_WAKEUP, "wake");
				wl.acquire();
				wl.release();
			}
		}
		if (count == 2) {
			count = 0;
		}
	}
}
