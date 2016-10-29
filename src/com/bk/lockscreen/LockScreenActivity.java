package com.bk.lockscreen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bk.android.visualeffect.EffectCmdType;
import com.bk.android.visualeffect.lock.particle.ParticleSpaceEffect;
import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.customadapter.MyExpandAdapter;
import com.bk.customview.AnimatedExpandableListView;
import com.bk.customview.Shimmer;
import com.bk.customview.ShimmerTextView;
import com.bk.lockscreen.utils.LockscreenUtils;
import com.bk.lockscreen.utils.Utils;
import com.bk.model.GroupNotification;
import com.bk.model.MyNotification;
import com.bk.setting.BasicSetting;
import com.bk.setting.MyParcelable;
import com.bk.table.tblAplicationInGroup;
import com.bk.table.tblGroupNotification;
import com.bk.table.tblNotification;

public class LockScreenActivity extends Activity {

	// Member variables
	private LockscreenUtils mLockscreenUtils;
	// Set appropriate flags to make the screen appear over the keyguard
	// private TelephonyManager mTelephonyManager;

	private TextView mDateTime;
	private ShimmerTextView mOperator;
	private Shimmer mShimmer;

	private Handler mTimerUpdate;
	private Calendar c = Calendar.getInstance();
	private SimpleDateFormat sdf;
	private ImageButton mFlashLight;

	private TextView mDate;
	private TextView mTime;
	private TextView mNoon;
	private TextView mBattery;
	private ImageView mBatteryImg;

	private AnimatedExpandableListView mListNotification;
	private MyExpandAdapter mListNotificationAdapter;

	private List<GroupNotification> mListGroupNotification = new ArrayList<GroupNotification>();

	private Context mContext;

	private ParticleSpaceEffect mParticleSpaceEffect;
	private GestureDetector mGestureDetector;

	private SoundPool mSoundPool = null;
	private int[] sounds = null;
	boolean loaded = false;
	private int dragSoundCount = 0;
	final int DRAG_SOUND_COUNT_INTERVAL = 60;
	final int DRAG_SOUND_COUNT_START_POINT = DRAG_SOUND_COUNT_INTERVAL - 20;
	final int SOUND_ID_TAB = 0;
	final int SOUND_ID_DRAG = 1;
	final int SOUND_ID_UNLOCK = 2;
	private float leftVolumeMax = 0.3f;
	private float rightVolumeMax = 0.3f;

	private final static String TAG = "LockScreenActivity";

	private final static int NOTIFI_POSTED = 1000;

	private MyContentObserver mContentObserver;
	private LockScreenActivity mThisActivity;

	/**
	 * flash ligh :
	 * 
	 */
	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	Parameters params;
	MediaPlayer mp;

	/*
	 * Get the camera
	 */
	@SuppressWarnings("deprecation")
	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				params = camera.getParameters();
			} catch (RuntimeException e) {
				Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
			}
		}
	}

	/*
	 * Turning On flash
	 */
	private void turnOnFlash() {
		if (!isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// play sound
			playSound();

			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			camera.startPreview();
			isFlashOn = true;

			// changing button/switch image
			toggleButtonImage();
		}

	}

	/*
	 * Turning Off flash
	 */
	private void turnOffFlash() {
		if (isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// play sound
			playSound();

			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(params);
			camera.stopPreview();
			isFlashOn = false;

			// changing button/switch image
			toggleButtonImage();
		}
	}

	/*
	 * Playing sound will play button toggle sound on flash on / off
	 */
	private void playSound() {
		if (isFlashOn) {
			mp = MediaPlayer.create(this, R.raw.light_switch_off);
		} else {
			mp = MediaPlayer.create(this, R.raw.light_switch_on);
		}
		mp.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mp.release();
			}
		});
		mp.start();
	}

	/*
	 * Toggle switch button images changing image states to on / off
	 */
	private void toggleButtonImage() {
		if (isFlashOn) {
			mFlashLight.setBackground(getResources().getDrawable(
					R.drawable.light_on));
		} else {
			mFlashLight.setBackground(getResources().getDrawable(
					R.drawable.light_off));
		}
	}

	private BroadcastReceiver batteryStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING;
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,
					-1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

			if ((acCharge) || (usbCharge) || (batteryCharge)) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_charge2_green));
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.charging));
				mBattery.setTextColor(Color.GREEN);
			} else if (60 <= level && level < 80) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_high));
				mBattery.setTextColor(Color.WHITE);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else if (20 <= level && level < 60) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_half));
				mBattery.setTextColor(Color.WHITE);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else if (level < 20) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_low_red));
				mBattery.setTextColor(Color.RED);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_full));
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
				mBattery.setTextColor(Color.WHITE);
			}
		}
	};
	private BroadcastReceiver mUpdateTimeDate = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mTimerUpdate.sendEmptyMessage(0);
		}

	};

	private void setDatalist(MyNotification myNotification) {
		if (!isGroupExits(getGroupId(myNotification.getPakageNam()))) {
			ArrayList<MyNotification> mArraylistNotification = new ArrayList<MyNotification>();
			mArraylistNotification.add(myNotification);
			GroupNotification groupNotification = new GroupNotification(
					getGroupId(myNotification.getPakageNam()),
					getGroupName(myNotification.getGroupID()),
					mArraylistNotification, false);
			mListNotification.setVisibility(View.VISIBLE);
			mListGroupNotification.add(groupNotification);
			Collections.sort(mListGroupNotification,
					new Comparator<GroupNotification>() {

						@Override
						public int compare(GroupNotification lhs,
								GroupNotification rhs) {
							return lhs.getGroupID() - rhs.getGroupID();
						}
					});
			mListNotificationAdapter.notifyDataSetChanged();
			setOpenDefault();
		} else {
			GroupNotification groupNotification = getGroupNotification(myNotification
					.getGroupID());
			groupNotification.getmListNotification().add(0, myNotification);
			mListNotificationAdapter.notifyDataSetChanged();
			mListNotification.setVisibility(View.VISIBLE);
			setOpenDefault();
		}
	}

	private boolean isGroupExits(int groupid) {
		for (GroupNotification groupnotification : mListGroupNotification) {
			if (groupnotification.getGroupID() == groupid) {
				return true;
			}
		}
		return false;
	}

	private GroupNotification getGroupNotification(int id) {
		GroupNotification groupNotification = null;
		for (GroupNotification groupNotification1 : mListGroupNotification) {
			if (groupNotification1.getGroupID() == id) {
				groupNotification = groupNotification1;
				break;
			}
		}
		return groupNotification;
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

	private String getGroupName(int groupid) {
		String group_Name = null;

		Log.e(TAG, "group id = " + groupid);

		Cursor c = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
				null, tblGroupNotification.GROUP_ID + " =?",
				new String[] { groupid + "" }, null);
		while (c.moveToNext()) {
			group_Name = c.getString(c
					.getColumnIndexOrThrow(tblGroupNotification.GROUP_NAME));
		}
		c.close();
		return group_Name;
	}

	@Override
	protected void onPause() {
		if (mShimmer != null && mShimmer.isAnimating()) {
			mShimmer.cancel();
			mShimmer = null;
		}
		dissmissDialog();
		super.onPause();
		unregisterReceiver(batteryStatusReceiver);
		unregisterReceiver(mUpdateTimeDate);
		getContentResolver().unregisterContentObserver(mContentObserver);
		releaseSound();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		if (mThisActivity != null) {
			mThisActivity = null;
		}
		if (mParticleSpaceEffect != null) {
			mParticleSpaceEffect = null;
		}
		if (hasFlash && isFlashOn)
			turnOffFlash();
		if (camera != null) {
			camera.release();
			camera = null;
		}

		super.onDestroy();
	}

	private void dissmissDialog() {
		mLockscreenUtils.unlock();
		if (mThisActivity != null) {
			mThisActivity = null;
		}

		Log.e(TAG, "da dismis dialog");
	}

	@Override
	protected void onResume() {

		/*
		 * First check if device is supporting flashlight or not
		 */
		hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

		if (!hasFlash) {
			// device doesn't support flash
			// Show alert message and close the application
			AlertDialog alert = new AlertDialog.Builder(this).create();
			alert.setTitle(getResources().getString(
					R.string.error_flashlight_title));
			alert.setMessage(getResources().getString(
					R.string.error_flashlight_message));
			alert.setButton(
					getResources().getString(R.string.error_flashlight_OK),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// closing the application
							finish();
						}
					});
			alert.show();
			return;
		}
		// get the camera
		getCamera();
		// displaying button image
		toggleButtonImage();

		init();
		Typeface face = Typeface.createFromAsset(mContext.getAssets(),
				"Roboto-Thin.ttf");
		mTime.setTypeface(face, Typeface.NORMAL);

		// Start animation :
		if (mShimmer == null) {
			mShimmer = new Shimmer();
		}
		mShimmer.start(mOperator);
		// lock home button
		setupNotificationList();
		settimedate();
		setDateTime();

		try {
			Log.e(TAG, "tuyen.px da khoa trong onResume");
			lockHomeButton();
			disableKeyguard();
		} catch (Exception e) {
			Log.e(TAG, "tuyen.px khong  khoa trong onResume");
			Log.d(TAG, e.toString());
		}

		mContentObserver = new MyContentObserver(mHandler);
		registerReceiver(batteryStatusReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		registerReceiver(mUpdateTimeDate, new IntentFilter(
				Intent.ACTION_TIME_TICK));
		getContentResolver().registerContentObserver(
				SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION, true,
				mContentObserver);
		super.onResume();
	}

	private ArrayList<MyNotification> getAllNotificationfromDB() {
		ArrayList<MyNotification> list = new ArrayList<MyNotification>();
		Cursor c = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION, null,
				null, null, null);
		while (c.moveToNext()) {
			MyNotification myNotification = new MyNotification(
					c.getInt(c.getColumnIndexOrThrow(tblNotification._ID)),
					c.getInt(c
							.getColumnIndexOrThrow(tblNotification.NOTIFICATION_ID)),
					c.getString(c
							.getColumnIndexOrThrow(tblNotification.PACKAGE_NAME)),
					"",
					c.getString(c
							.getColumnIndexOrThrow(tblNotification.TITLE_NOTIFICATION)),
					c.getString(c
							.getColumnIndexOrThrow(tblNotification.CONTENT_NOTIFICATION)),
					c.getInt(c.getColumnIndexOrThrow(tblNotification.GROUP_ID)),
					c.getString(c
							.getColumnIndexOrThrow(tblNotification.TIME_NOTIFICATION)),
					c.getInt(c
							.getColumnIndexOrThrow(tblNotification.ICON_NOTIFICATION)));
			list.add(myNotification);
		}
		c.close();
		return list;
	}

	private void setupNotificationList() {
		mListNotificationAdapter.setData(mListGroupNotification);
		mListNotification.setAdapter(mListNotificationAdapter);
		SetupListView();
	}

	private void SetupListView() {
		mListGroupNotification.removeAll(mListGroupNotification);
		ArrayList<MyNotification> arrayList = getAllNotificationfromDB();
		if (arrayList.size() > 0) {
			for (MyNotification myNotification : arrayList) {
				setDatalist(myNotification);
			}
			mListNotification.setVisibility(View.VISIBLE);
		} else {
			mListNotification.setVisibility(View.GONE);
		}
	}

	private void setOpenDefault() {
		for (int i = 0; i < mListGroupNotification.size(); i++) {
			if (mListGroupNotification.get(i).getGroupID() == 1) {
				mListNotification.expandGroupWithAnimation(i);
			}
		}
	}

	public void expandGroup(int possion) {
		mListNotification.expandGroupWithAnimation(possion);
	}

	public void hideGroup(int possion) {
		mListNotification.collapseGroupWithAnimation(possion);
	}

	public void FullScreencall() {

		if (Build.VERSION.SDK_INT >= 19) { // 19 or above api
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		} else {
			// for lower api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN;

			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "tran.thang onCreate");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lockscreen);
		try {
			FullScreencall();
		} catch (Exception e) {
			Log.d("failed", "XXX");
		}
		// unlock screen in case of app get killed by system
		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {
			enableKeyguard();
			unlockHomeButton();
		} else {
			try {
				// disable keyguard
				lockHomeButton();
				disableKeyguard();
				// listen the events get fired during the call
				TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (null != mTelephonyManager)
					mTelephonyManager.listen(mPhoneStateListener,
							PhoneStateListener.LISTEN_CALL_STATE);
			} catch (Exception e) {
			}
		}
		// Bitmap c = null;
		MyParcelable myParcelable = MyParcelable.getInstance();
		Bitmap bitmapTemp = myParcelable.getBitmap("data");
		if (bitmapTemp == null) {
			SharedPreferences preferences = getSharedPreferences(
					Utils.BACKGROUNDPREFERENCE, Context.MODE_PRIVATE);
			String path = preferences.getString(Utils.CURRENTBACKGROUND, "");
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int width = dm.widthPixels;
			int height = dm.heightPixels;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			if (path.trim().equals("")) {
				bitmapTemp = BitmapFactory.decodeResource(getResources(),
						R.drawable.face, options);
			} else {
				bitmapTemp = BitmapFactory.decodeFile(path, options);
			}
			options.inSampleSize = calculateInSampleSize(options, width, height) * 2;
			options.inJustDecodeBounds = false;
			if (path.trim().equals("")) {
				bitmapTemp = BitmapFactory.decodeResource(getResources(),
						R.drawable.face, options);
			} else {
				bitmapTemp = BitmapFactory.decodeFile(path, options);
			}
			myParcelable.putBitmap("data", bitmapTemp);
		}
		if (bitmapTemp != null) {
			bitmapTemp = null;
		}
		if (mThisActivity == null) {
			mThisActivity = this;
		}
		mListNotificationAdapter = new MyExpandAdapter(mThisActivity);
		mListNotificationAdapter.setLockScreenActivity(mThisActivity);
		mListNotification = (AnimatedExpandableListView) findViewById(R.id.list_notification);
		mParticleSpaceEffect = (ParticleSpaceEffect) findViewById(R.id.effectbackground);
		mParticleSpaceEffect.setDrawingCacheEnabled(false);
		mGestureDetector = new GestureDetector(new SwipeGestureDetector());
		mParticleSpaceEffect.handleCustomEvent(EffectCmdType.SETBG, null);
		mContext = getApplicationContext();
		mDateTime = (TextView) findViewById(R.id.date);
		mOperator = (ShimmerTextView) findViewById(R.id.operator);
		mDate = (TextView) findViewById(R.id.date);
		mTime = (TextView) findViewById(R.id.time);
		mNoon = (TextView) findViewById(R.id.noon);
		mBattery = (TextView) findViewById(R.id.battery);
		mBatteryImg = (ImageView) findViewById(R.id.battery_img);
		mFlashLight = (ImageButton) findViewById(R.id.btnflash);
		mFlashLight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isFlashOn) {
					// turn off flash
					turnOffFlash();
				} else {
					// turn on flash
					turnOnFlash();
				}
			}
		});

		Log.e("tuyen.px",
				"tuyen.px color value = "
						+ PreferenceManager.getDefaultSharedPreferences(
								getApplicationContext()).getInt(
								BasicSetting.COLOR_DIALOG, 0));

	}

	private float lastx = 0;
	private float lasty = 0;

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

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	private void makeSound() {
		Log.d(TAG, "tran.thang makeSound");
		boolean flag = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(BasicSetting.KEY_SOUND_UNLOCK, true);
		if (flag) {
			if (mSoundPool == null) {
				sounds = new int[3];
				// mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM,
				// 0);

				if ((android.os.Build.VERSION.SDK_INT) == 21) {
					AudioAttributes attr = new AudioAttributes.Builder()
							.setUsage(
									AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
							.setContentType(
									AudioAttributes.CONTENT_TYPE_SONIFICATION)
							.build();

					mSoundPool = new SoundPool.Builder().setMaxStreams(10)
							.setAudioAttributes(attr).build();
				} else {
					mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
				}

				mSoundPool
						.setOnLoadCompleteListener(new OnLoadCompleteListener() {

							@Override
							public void onLoadComplete(SoundPool arg0,
									int arg1, int arg2) {
								// TODO Auto-generated method stub
								loaded = true;
							}
						});
				sounds[SOUND_ID_TAB] = mSoundPool.load(mContext,
						R.raw.particle_tap, 1);
				sounds[SOUND_ID_DRAG] = mSoundPool.load(mContext,
						R.raw.particle_drag, 1);
				sounds[SOUND_ID_UNLOCK] = mSoundPool.load(mContext,
						R.raw.lens_flare_unlock, 1);
			}
		}
	}

	private void playSound(int soundId) {
		if (loaded && mSoundPool != null) {
			// Log.d(TAG, "tran.thang playSound() - soundId = " + soundId);
			mSoundPool.play(sounds[soundId], leftVolumeMax, rightVolumeMax, 0,
					0, 1.0f);
		}
	}

	private void releaseSound() {
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.e(TAG, "tran.thang onTouchEvent = " + event);
		mGestureDetector.onTouchEvent(event);
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			dragSoundCount = DRAG_SOUND_COUNT_START_POINT;
			if (mSoundPool == null) {
				Log.d(TAG, "ACTION_DOWN, mSoundPool == null");
				makeSound();
			}
			playSound(SOUND_ID_TAB);
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			dragSoundCount++;
			if (dragSoundCount >= DRAG_SOUND_COUNT_INTERVAL) {
				playSound(SOUND_ID_DRAG);
				dragSoundCount = 0;
			}
		}
		mParticleSpaceEffect.handleTouchEvent(event, null);

		return true;
	}

	private static final int SWIPE_MIN_DISTANCE = 200 * 200;

	class SwipeGestureDetector extends SimpleOnGestureListener {
		// Swipe properties, you can change it to make the swipe
		// longer or shorter and speed
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				float diffAbsY = Math.abs(e1.getY() - e2.getY());
				float diffAbsX = Math.abs(e1.getX() - e2.getX());
				float distance = diffAbsX * diffAbsX + diffAbsY * diffAbsY;
				if (distance > SWIPE_MIN_DISTANCE) {
					unlockHomeButton();
				}

			} catch (Exception e) {
				Log.e("YourActivity", "Error on gestures");
			}
			return false;
		}
	}

	private void settimedate() {
		c = Calendar.getInstance();

		sdf = new SimpleDateFormat("hh:mm a");
		String time = sdf.format(c.getTime());
		mTime.setText(time.substring(0, time.length() - 3));
		mNoon.setText(time.substring(time.length() - 2));

		sdf = new SimpleDateFormat("EE, d  MMM yyyy");
		String date = sdf.format(c.getTime());
		mDate.setText(date.substring(0, date.length() - 5));
	}

	private void setDateTime() {
		mTimerUpdate = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				settimedate();

			}
		};
	}

	private void init() {
		mLockscreenUtils = LockscreenUtils.getInstance();
	}

	// Handle events of calls and unlock screen if necessary

	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				unlockHomeButton();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	// Don't finish Activity on Back press
	@Override
	public void onBackPressed() {
		return;
	}

	// Handle button clicks
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_HOME
				|| keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_POWER
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_CAMERA;

	}

	// Lock home button
	public void lockHomeButton() {
		if (mThisActivity == null) {
			mThisActivity = this;
		}
		mLockscreenUtils.lock(mThisActivity);
	}

	// Unlock home button and wait for its callback
	public void unlockHomeButton() {
		mLockscreenUtils.unlock();
		unlockDevice();
	}

	@SuppressWarnings("deprecation")
	private void disableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.reenableKeyguard();
	}

	// Simply unlock device by finishing the activity
	private void unlockDevice() {
		if (mParticleSpaceEffect != null) {
			mParticleSpaceEffect.handleCustomEvent(EffectCmdType.UNLOCK, null);
			playSound(SOUND_ID_UNLOCK);
			finish();
		}
	}

	private void openSMS() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			String defaultSmsPackageName = Telephony.Sms
					.getDefaultSmsPackage(this);
			openNotification(defaultSmsPackageName);

		} else {
			openNotification("com.android.mms");
		}
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		Log.d(TAG, "tran.thang check Height = " + height + "width = " + width);
		Log.d(TAG, "tran.thang check " + "reqHeight = " + reqHeight
				+ "reqWidth = " + reqWidth);
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth,
			int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		Log.d(TAG, "tran.thang calculateInSampleSize resId inSampleSize "
				+ options.inSampleSize);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(getResources(), resId, options);
	}

	private Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		Log.d(TAG, "tran.thang calculateInSampleSize pathName inSampleSize "
				+ options.inSampleSize);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	private void openNotification(String pkgName) {
		PackageManager pm = getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);

		if (launchIntent != null) {
			startActivity(launchIntent);
		} else {
			if (pkgName.equals("com.android.server.telecom")
					|| pkgName.equals("com.android.phone")) {
				Intent showCallLog = new Intent();
				showCallLog.setAction(Intent.ACTION_VIEW);
				showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
				startActivity(showCallLog);
				finish();
			}
		}
	}

	class MyContentObserver extends ContentObserver {

		public MyContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			Message msg = new Message();
			Log.e(TAG, "===== DATACONTENT OBSERVER ONCHANGE =====");
			msg.what = NOTIFI_POSTED;
			mHandler.sendMessage(msg);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == NOTIFI_POSTED) {
				SetupListView();
			}
		};
	};

	private void StartAcitivity(String Name) {
		String pakageName = getPackageName(Name);
		PackageManager pm = getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(pakageName);
		if (launchIntent != null) {
			startActivity(launchIntent);
		}
		finish();
	}

	private List<ResolveInfo> mListAplicationInfo;

	private String getPackageName(String Name) {

		String AppName = "";
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		mListAplicationInfo = mContext.getPackageManager()
				.queryIntentActivities(intent, 0);
		for (int i = 0; i < mListAplicationInfo.size(); i++) {
			String applicationname = mListAplicationInfo.get(i).loadLabel(
					mContext.getPackageManager())
					+ "";
			if (applicationname.equalsIgnoreCase(Name)) {
				AppName = mListAplicationInfo.get(i).activityInfo.packageName;
				break;
			}
		}
		return AppName;
	}

}