package com.bk.lockscreen;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.lockscreen.utils.LockscreenService;
import com.bk.lockscreen.utils.MyAdminReceiver;
import com.bk.lockscreen.utils.NotificationService;
import com.bk.lockscreen.utils.Utils;
import com.bk.setting.AboutFragment;
import com.bk.setting.BasicSetting;
import com.bk.setting.HelpFragment;
import com.bk.setting.MyParcelable;
import com.bk.setting.NotificationSetting;
import com.bk.slidingmenu.NavDrawerItem;
import com.bk.slidingmenu.NavDrawerListAdapter;
import com.bk.table.tblAplicationInGroup;
import com.bk.table.tblGroupNotification;

public class MainActivity extends Activity {

	// slidding menu
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private String[] mListGroupPriority;
	private String[] mListGroupSocial;
	private static final int ADMIN_INTENT = 15;

	// for load application
	private PackageManager packageManager = null;
	private List<String> applist = null;

	private String[] mListGroupWork;
	private String[] mListGroupMedia;
	private String[] mListGroupHidden;
	private String[] mListGroupOther;
	private static final String description = "Sample Administrator description";
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mComponentName;

	@SuppressLint("ShowToast")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isShow()) {
			ContentResolver contentResolver = getContentResolver();
			String enabledNotificationListeners = Settings.Secure.getString(
					contentResolver, "enabled_notification_listeners");
			String packageName = getPackageName();
			// check xem nguoi dung da enable accessnotification len chua .
			if (enabledNotificationListeners == null
					|| !enabledNotificationListeners.contains(packageName)) {
				stopService(new Intent(this, NotificationService.class));
			} else {
				startService(new Intent(this, NotificationService.class));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		packageManager = getApplication().getPackageManager();
		initFirst(savedInstanceState);
		initDataBase();
		// start service for observing intents
		startService(new Intent(getApplicationContext(),
				LockscreenService.class));
		mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mComponentName = new ComponentName(this, MyAdminReceiver.class);
		new LoadAsystask().execute();
		boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
		if (!isAdmin) {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					mComponentName);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					description);
			startActivityForResult(intent, ADMIN_INTENT);
		}

	}

	class LoadAsystask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Bitmap c = null;
			MyParcelable myParcelable = MyParcelable.getInstance();
			Bitmap bitmapTemp = myParcelable.getBitmap("data");
			if (bitmapTemp == null) {

				SharedPreferences preferences = getSharedPreferences(
						Utils.BACKGROUNDPREFERENCE, Context.MODE_PRIVATE);
				String path = preferences
						.getString(Utils.CURRENTBACKGROUND, "");

				Display display = getWindowManager().getDefaultDisplay();
				int width = display.getWidth();
				int height = display.getHeight();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;

				if (path.trim().equals("")) {
					bitmapTemp = BitmapFactory.decodeResource(getResources(),
							R.drawable.face, options);
				} else {
					bitmapTemp = BitmapFactory.decodeFile(path, options);
				}

				options.inSampleSize = calculateInSampleSize(options, width,
						height) * 2;
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
			return null;
		}

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

	private boolean isShow() {
		if (Build.VERSION.SDK_INT >= 18) {
			return true;
		}
		return false;
	}

	// slidding menu
	public void initFirst(Bundle savedInstanceState) {
		mTitle = mDrawerTitle = getTitle();
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		navDrawerItems = new ArrayList<NavDrawerItem>();
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));

		if (isShow()) {
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
					.getResourceId(1, -1)));
		}
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1)));
		// Recycle the typed array
		navMenuIcons.recycle();
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.icon_menu, // menu toggle icon
				R.string.menu_name, // drawer open - description for
				// accessibility
				R.string.app_name // drawer close - description for
		// accessibility
		) {
			public void onDrawerClosed(android.view.View drawerView) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			};

			public void onDrawerOpened(android.view.View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			};

		};
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		if (savedInstanceState == null) {
			mDrawerList.setItemChecked(0, true);
			displayView(0);
		}
	}

	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			adapter.resetAll(position);
			displayView(position);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		super.setTitle(title);
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		if (isShow()) {
			switch (position) {

			case 0:
				fragment = new BasicSetting();
				break;
			case 1:
				fragment = new NotificationSetting();
				break;
			case 2:
				// help fragment
				fragment = new HelpFragment();
				break;
			case 3:
				fragment = new AboutFragment();
				break;
			default:
				break;
			}
		} else {
			switch (position) {

			case 0:
				fragment = new BasicSetting();
				break;
			case 1:
				// help fragment
				fragment = new HelpFragment();
				break;
			case 2:
				fragment = new AboutFragment();
				break;
			default:
				break;
			}
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.setting:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// init Database for lockScreen .
	private void initDataBase() {
		Cursor c = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
				null, null, null, null);
		if (c.getCount() < 1) {
			ContentValues v = new ContentValues();
			v.put(tblGroupNotification.GROUP_NAME, "PRIORITY");
			v.put(tblGroupNotification.GROUP_ID, 1);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v);
			ContentValues v2 = new ContentValues();
			v2.put(tblGroupNotification.GROUP_NAME, "SOCIAL");
			v2.put(tblGroupNotification.GROUP_ID, 2);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v2);

			ContentValues v3 = new ContentValues();
			v3.put(tblGroupNotification.GROUP_NAME, "WORK");
			v3.put(tblGroupNotification.GROUP_ID, 3);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v3);

			ContentValues v4 = new ContentValues();
			v4.put(tblGroupNotification.GROUP_NAME, "MEDIA");
			v4.put(tblGroupNotification.GROUP_ID, 4);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v4);

			ContentValues v5 = new ContentValues();
			v5.put(tblGroupNotification.GROUP_NAME, "HIDDEN");
			v5.put(tblGroupNotification.GROUP_ID, 5);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v5);

			ContentValues v6 = new ContentValues();
			v6.put(tblGroupNotification.GROUP_NAME, "OTHER");
			v6.put(tblGroupNotification.GROUP_ID, 10);
			getContentResolver()
					.insert(SmartLockScreenNotificationProvider.URI_TBL_GROUPNOTIFICATION,
							v6);

		}
		c.close();

		Cursor d = getContentResolver().query(
				SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
				null, null, null, null);
		if (d.getCount() < 1) {

			// priority :
			mListGroupPriority = getResources()
					.getStringArray(R.array.PRIORITY);

			for (int i = 0; i < mListGroupPriority.length; i++) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 1);
				values.put(tblAplicationInGroup.PACKAGE, mListGroupPriority[i]);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);
			}

			// social :

			mListGroupSocial = getResources().getStringArray(R.array.SOCIAL);
			for (int i = 0; i < mListGroupSocial.length; i++) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 2);
				values.put(tblAplicationInGroup.PACKAGE, mListGroupSocial[i]);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);
			}

			// work :
			mListGroupWork = getResources().getStringArray(R.array.WORK);
			for (int i = 0; i < mListGroupWork.length; i++) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 3);
				values.put(tblAplicationInGroup.PACKAGE, mListGroupWork[i]);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);
			}

			// Media :
			mListGroupMedia = getResources().getStringArray(R.array.MEDIA);
			for (int i = 0; i < mListGroupMedia.length; i++) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 4);
				values.put(tblAplicationInGroup.PACKAGE, mListGroupMedia[i]);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);

			}
			// hidden :

			mListGroupHidden = getResources().getStringArray(R.array.Hidden);
			for (int i = 0; i < mListGroupHidden.length; i++) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 5);
				values.put(tblAplicationInGroup.PACKAGE, mListGroupHidden[i]);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);
			}

			// other
			new LoadAppsToOther().execute();

		}

		d.close();
	}

	private List<String> checkForLaunchIntent(List<ApplicationInfo> list) {
		ArrayList<String> applist = new ArrayList<String>();

		for (ApplicationInfo info : list) {
			boolean check = false;
			for (String pakage : mListGroupHidden) {
				check = check || pakage.equals(info.packageName) ? true : false;
			}
			for (String pakage : mListGroupMedia) {
				check = check || pakage.equals(info.packageName) ? true : false;
			}
			for (String pakage : mListGroupPriority) {
				check = check || pakage.equals(info.packageName) ? true : false;
			}
			for (String pakage : mListGroupSocial) {
				check = check || pakage.equals(info.packageName) ? true : false;
			}
			for (String pakage : mListGroupWork) {
				check = check || pakage.equals(info.packageName) ? true : false;
			}
			try {
				if ((!check)
						&& (null != packageManager
								.getLaunchIntentForPackage(info.packageName))) {
					applist.add(info.packageName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return applist;
	}

	public class LoadAppsToOther extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			applist = checkForLaunchIntent(packageManager
					.getInstalledApplications(PackageManager.GET_META_DATA));
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			for (String pakage : applist) {
				ContentValues values = new ContentValues();
				values.put(tblAplicationInGroup.GROUP_ID, 10);
				values.put(tblAplicationInGroup.PACKAGE, pakage);
				getContentResolver()
						.insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
								values);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		boolean flag = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(BasicSetting.KEY_USE_SMART_LOCKSCREEN, true);
		if (!flag) {
			System.exit(0);
		}
	}

}