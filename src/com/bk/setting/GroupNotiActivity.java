package com.bk.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.customadapter.ApplicationAdapter;
import com.bk.lockscreen.R;
import com.bk.model.MyNotification;
import com.bk.table.tblAplicationInGroup;
import com.bk.table.tblNotification;

public class GroupNotiActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	// for load application
	private PackageManager packageManager = null;
	private List<ApplicationInfo> applist = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);

		packageManager = getApplication().getPackageManager();
		// new LoadApplications().execute();
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 5) {
				position = 9;
			}
			Fragment fragment = new GroupNotiFragment(position + 1);
			return fragment;
		}

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.priority).toUpperCase(l);
			case 1:
				return getString(R.string.social).toUpperCase(l);
			case 2:
				return getString(R.string.work).toUpperCase(l);
			case 3:
				return getString(R.string.media).toUpperCase(l);
			case 4:
				return getString(R.string.hidden).toUpperCase(l);
			case 5:
				return getString(R.string.other).toUpperCase(l);
			}
			return null;
		}
	}

	public static class GroupNotiFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";
		private MyContentObserver mContentObserver;
		private final static int NOTIFI_POSTED = 1000;
		private ArrayList<String> packageList = null;
		private ArrayList<ApplicationInfo> applist = null;
		private ApplicationAdapter listadaptor = null;
		private ListView lvApp;
		private int groupID = 0;
		private int select = 0;

		public GroupNotiFragment(int groupID) {
			this.groupID = groupID;
			this.select = groupID - 1;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_app_list,
					container, false);
			lvApp = (ListView) rootView.findViewById(R.id.lv_app);
			// myDB = new GroupNotificationBD(getActivity());
			applist = new ArrayList<ApplicationInfo>();

			listadaptor = new ApplicationAdapter(getActivity(),
					R.layout.snippet_list_row, applist);
			new taskLoadFile(getActivity()).execute();

			lvApp.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						final int pos, long arg3) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle("Select group");
					final CharSequence[] group = { "Priority", "Social",
							"Work", "Media", "Hidden", "Other" };

					builder.setSingleChoiceItems(group, groupID - 1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									select = which;
								}
							})
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											ContentValues cv = new ContentValues();
											cv.put(tblAplicationInGroup.GROUP_ID,
													select + 1);

											getActivity()
													.getContentResolver()
													.update(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
															cv,
															tblAplicationInGroup.PACKAGE
																	+ "=?",
															new String[] { applist
																	.get(pos).packageName });
											loadApplication();

										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			});

			return rootView;
		}

		@Override
		public void onDestroy() {
			getActivity().getContentResolver().unregisterContentObserver(
					mContentObserver);
			super.onDestroy();
		}

		@Override
		public void onResume() {
			mContentObserver = new MyContentObserver(mHandler);
			getActivity()
					.getContentResolver()
					.registerContentObserver(
							SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
							true, mContentObserver);
			super.onResume();
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
				msg.what = NOTIFI_POSTED;
				mHandler.sendMessage(msg);
			}
		}

		private Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == NOTIFI_POSTED) {
					loadApplication();
				}
			};
		};

		class taskLoadFile extends AsyncTask<Void, Void, Void> {
			private ProgressDialog mDialog;

			public taskLoadFile(Activity activity) {
				mDialog = new ProgressDialog(activity);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mDialog.setMessage("loading....");
				mDialog.setCanceledOnTouchOutside(false);
				if (mDialog != null && !mDialog.isShowing()) {
					mDialog.show();
				}
			}

			@Override
			protected Void doInBackground(Void... params) {
				loadApplication();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
				}

				if (applist.size() > 0) {
					listadaptor.notifyDataSetChanged();
					lvApp.setAdapter(listadaptor);
				}
			}

		}

		private void loadApplication() {
			listadaptor.clear();
			applist.clear();
			Cursor c = getActivity()
					.getContentResolver()
					.query(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP,
							null, tblAplicationInGroup.GROUP_ID + "=?",
							new String[] { "" + groupID }, null);
			while (c.moveToNext()) {
				try {
					ApplicationInfo app = getActivity().getPackageManager()
							.getApplicationInfo(c.getString(2), 0);
					applist.add(app);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			c.close();
		}
	}

}
