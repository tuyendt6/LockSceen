package com.bk.customadapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.customview.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import com.bk.lockscreen.LockScreenActivity;
import com.bk.lockscreen.R;
import com.bk.lockscreen.utils.CalendarNotification;
import com.bk.lockscreen.utils.Utils;
import com.bk.model.GroupNotification;
import com.bk.model.MyNotification;
import com.bk.setting.NotificationSetting;
import com.bk.table.tblNotification;

public class MyExpandAdapter extends AnimatedExpandableListAdapter {

	protected static final String TAG = "MyExpandAdapter";

	private LayoutInflater inflater;

	private TextView titleNotiTextView;
	private TextView contentNotiTextView;
	private CalendarNotification calendarNoti;
	private Dialog remindDialog;
	private DisplayMetrics displaySize = new DisplayMetrics();

	private LockScreenActivity lockScreenActivity;

	Context mContext;
	private float downX, downY, upX, upY;
	private float deltaX;
	private MyTouchListener mOnTouchListener;

	private String titleCalendar = "";
	private String timeCalendar = "";
	private String texthillCalendar = "";

	private List<GroupNotification> items;

	public MyExpandAdapter(Context mContext) {
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
		initRemindDialog(mContext);
	}

	private void initRemindDialog(Context context) {
		Log.d(TAG, "tran.thang initRemindDialog");

		remindDialog = new Dialog(context);
		remindDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		remindDialog.setCanceledOnTouchOutside(true);
		remindDialog.setCancelable(true);
		remindDialog.setTitle("Remind after");

		LinearLayout view = new LinearLayout(context);
		view.setOrientation(LinearLayout.VERTICAL);
		view.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		view.setLayoutParams(lp);


		// Add buttons
		NotificationButton fifteen, thirty, hour, twoHour;
		fifteen = new NotificationButton(mContext, 1, "15 minutes");
		thirty = new NotificationButton(mContext, 2, "30 minutes");
		hour = new NotificationButton(mContext, 3, "1 hour");
		twoHour = new NotificationButton(mContext, 4, "2 hours");

		view.addView(fifteen);
		view.addView(thirty);
		view.addView(hour);
		view.addView(twoHour);
		remindDialog.setContentView(view);
		remindDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.clock_icon);
	}

	static class ChildHolder {
		TextView title;
		TextView hint;
		TextView time;
		ImageView img;
		Button remindButton;
		Button deleteButton;
		LinearLayout remindButtonLayout;
		LinearLayout imageViewItemLayout;
	}

	static class GroupHolder {
		TextView title;
		Button delete;
		ImageView img;
	}

	public void setData(List<GroupNotification> items) {
		this.items = items;
	}

	@Override
	public MyNotification getChild(int arg0, int arg1) {
		return items.get(arg0).getmListNotification().get(arg1);
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		return arg1;
	}

	@Override
	public GroupNotification getGroup(int arg0) {
		return items.get(arg0);
	}

	@Override
	public int getGroupCount() {
		return items.size();
	}

	@Override
	public long getGroupId(int arg0) {
		return arg0;
	}

	@Override
	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {

		View v = arg2;
		final GroupHolder holder;
		final int posison = arg0;
		final boolean isExpand = arg1;
		final GroupNotification groupNotification = getGroup(arg0);
		final Animation in = new AlphaAnimation(0.0f, 1.0f);
		in.setDuration(500);

		final Animation out = new AlphaAnimation(1.0f, 0.0f);
		out.setDuration(500);

		if (v == null) {
			holder = new GroupHolder();
			v = inflater.inflate(R.layout.groupnotificationitem, arg3, false);
			holder.title = (TextView) v.findViewById(R.id.txtNameGroup);
			holder.delete = (Button) v.findViewById(R.id.btndelete);
			holder.img = (ImageView) v.findViewById(R.id.button1);
			v.setTag(holder);
		} else {
			holder = (GroupHolder) v.getTag();
		}
		holder.title.setText(groupNotification.getName() + "  "
				+ getChildrenCount(arg0));
		holder.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (holder.delete.getText().toString().trim()
						.equalsIgnoreCase("Delete")) {
					deletedatabase(groupNotification);
				} else {

					holder.delete.startAnimation(in);
					holder.delete.setText("Delete");
				}
			}
		});
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (holder.delete.getText().toString()
						.equalsIgnoreCase("Delete")) {
					holder.delete.startAnimation(out);
					holder.delete.setText("X");
					return;
				}
				if (isExpand) {
					holder.img.setImageDrawable(mContext.getResources()
							.getDrawable(R.drawable.up));
					lockScreenActivity.hideGroup(posison);
					holder.delete.setVisibility(View.INVISIBLE);

				} else {
					holder.img.setImageDrawable(mContext.getResources()
							.getDrawable(R.drawable.down));
					lockScreenActivity.expandGroup(posison);
					holder.delete.setVisibility(View.VISIBLE);
				}

			}
		});

		return v;
	}

	private void deletedatabase(GroupNotification groupNotification) {
		for (MyNotification myNotification : groupNotification
				.getmListNotification()) {
			cancelNotification(myNotification);
			mContext.getContentResolver().delete(
					SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
					tblNotification._ID + "=?",
					new String[] { myNotification.get_ID() + "" });
			Utils.hashMap.remove(myNotification.getNotificationID());
		}
		items.remove(groupNotification);
		notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

	@Override
	public View getRealChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Log.d(TAG, "tran.thang getRealChildView groupPosition = "
				+ groupPosition + ", childPosition =" + childPosition
				+ ", isLastChild = " + isLastChild + ", convertView ="
				+ convertView + ", parent =" + parent);
		final ChildHolder holder;
		View v = convertView;
		MyNotification notification = getChild(groupPosition, childPosition);

		if (v == null) {
			holder = new ChildHolder();
			v = inflater.inflate(R.layout.notificationitem, parent, false);
			
			RelativeLayout notiLayout = (RelativeLayout)v.findViewById(R.id.noti_layout);
			if (PreferenceManager.getDefaultSharedPreferences(mContext)
					.getBoolean(NotificationSetting.KEY_USE_SHOW_NOTI_BACKGROUND, false)) {
				notiLayout.setBackground(mContext.getResources().getDrawable(R.drawable.custom_background));
			}

			titleNotiTextView = (TextView) v.findViewById(R.id.texttitle);
			contentNotiTextView = (TextView) v.findViewById(R.id.texthill);
			holder.title = (TextView) v.findViewById(R.id.texttitle);
			holder.hint = (TextView) v.findViewById(R.id.texthill);
			holder.time = (TextView) v.findViewById(R.id.notidate);
			holder.img = (ImageView) v.findViewById(R.id.imageView1);
			holder.remindButton = (Button) v.findViewById(R.id.remindbutton);
			holder.remindButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View button) {
//					Log.d(TAG, "tran.thang onClick remind button");
					// TODO Auto-generated method stub
					titleCalendar = (String) holder.title.getText();
					timeCalendar = (String) holder.time.getText();
					texthillCalendar = (String) holder.hint.getText();
					remindDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					remindDialog.show();
				}
			});
			holder.deleteButton = (Button) v.findViewById(R.id.deleteButton);
			holder.remindButtonLayout = (LinearLayout) v
					.findViewById(R.id.remindbuttonLayout);
			holder.imageViewItemLayout = (LinearLayout) v
					.findViewById(R.id.imageViewItemLayout);
			v.setTag(R.layout.notificationitem, holder);
		} else {
			holder = (ChildHolder) v.getTag(R.layout.notificationitem);
		}

		v.setTag(new Sample(groupPosition, childPosition));
		mOnTouchListener = new MyTouchListener();
		v.setOnTouchListener(mOnTouchListener);
		holder.title.setText(notification.getTitle());
		String textHint = notification.getText();
		holder.hint.setText(textHint);
		holder.time.setText(convertTime(notification.getTime()));
		Drawable icon = null;
		try {
			icon = mContext.getPackageManager().getApplicationIcon(
					notification.getPakageNam());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (icon != null) {
			holder.img.setImageDrawable(icon);
		}

		return v;
	}

	class Sample {
		int parentPos;
		int childPos;

		public Sample(int parentPos, int childPos) {
			super();
			this.parentPos = parentPos;
			this.childPos = childPos;
		}
	}

	private Sample mSample = null;

	class MyTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "tran.thang onTouch here event = " +
			// event.getAction());
			final ChildHolder holder = (ChildHolder) v
					.getTag(R.layout.notificationitem);
			int action = event.getAction();
			final Sample sample = (Sample) v.getTag();
			int child = sample.childPos;
			int parent = sample.parentPos;

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				// Log.d(TAG, "tran.thang  ACTION_DOWN - ");
				downX = event.getX();
				downY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
//				 Log.d(TAG, "tran.thang ACTION_MOVE");
				upX = event.getX();
				upY = event.getY();
				deltaX = downX - upX;
				calcuateDifference(sample, holder, deltaX);
				mSample = sample;
				// horizontal swipe detection

				break;
			case MotionEvent.ACTION_UP:
//				 Log.d(TAG, "tran.thang ACTION_UP");
				if (Math.abs(deltaX) < 10) {
					StartAcitivity(items.get(parent).getmListNotification()
							.get(child));
					mContext.getContentResolver()
							.delete(SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
									tblNotification._ID + "=?",
									new String[] { items.get(parent)
											.getmListNotification().get(child)
											.get_ID()
											+ "" });
					items.remove(parent);
				}else if (Math.abs(deltaX) > 50) {
					if (deltaX < 0) {
//						Log.d(TAG, "tran.thang left");
						boolean remindVisible = holder.remindButtonLayout.getVisibility() == View.VISIBLE;
//						Log.d(TAG, "tran.thang remind button visible = " + remindVisible);
						if (!remindVisible) {
							
							holder.img.setVisibility(View.GONE);
							holder.deleteButton.setVisibility(View.VISIBLE);
							holder.deleteButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									mContext.getContentResolver()
											.delete(SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
													tblNotification._ID
															+ "=?",
													new String[] { items
															.get(sample.parentPos)
															.getmListNotification()
															.get(sample.childPos)
															.get_ID()
															+ "" });
									items.remove(sample.parentPos);
								}
							});

						} else{
							holder.remindButtonLayout.setVisibility(View.GONE);
							holder.img.setVisibility(View.VISIBLE);
							holder.deleteButton.setVisibility(View.GONE);
						}
					}else {
						
						boolean deleteVisible = holder.deleteButton.getVisibility() == View.VISIBLE;
						if (!deleteVisible) {
							holder.remindButtonLayout.setVisibility(View.VISIBLE);
							holder.deleteButton.setVisibility(View.GONE);
							holder.img.setVisibility(View.VISIBLE);
						} else{
							holder.deleteButton.setVisibility(View.GONE);
							holder.remindButtonLayout.setVisibility(View.GONE);
							holder.img.setVisibility(View.VISIBLE);
						}
					}
				}else{
//					Log.d(TAG, "tran.thang right");
					
				}
				break;
			}

			return true;
		}
	}

	private void StartAcitivity(MyNotification myNotification) {
		PendingIntent it = null;
		int key = 0;
		for (Integer i : Utils.hashMap.keySet()) {
			if (myNotification.getNotificationID() == i) {
				it = Utils.hashMap.get(i);
				key = i;
				break;
			}
		}
		Log.e(TAG, myNotification.getPakageNam());
		Utils.hashMap.remove(key);
		if (it != null) {
			try {
				it.send(mContext, 0, new Intent());
			} catch (CanceledException e) {
				openNotification(myNotification.getPakageNam());
				lockScreenActivity.finish();
				e.printStackTrace();
			}
		} else {
			openNotification(myNotification.getPakageNam());
			lockScreenActivity.finish();
		}
	}

	private void openNotification(String pkgName) {
		PackageManager pm = mContext.getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);

		if (launchIntent != null) {
			mContext.startActivity(launchIntent);
		} else {
			if (pkgName.equals("com.android.server.telecom")
					|| pkgName.equals("com.android.phone")) {
				Intent showCallLog = new Intent();
				showCallLog.setAction(Intent.ACTION_VIEW);
				showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
				mContext.startActivity(showCallLog);
				lockScreenActivity.finish();
			}
		}
	}

	private AlertDialog alertDialog = null;

	private void calcuateDifference(final Sample sample,
			final ChildHolder holder, final float deltaX) {
		(((Activity) mContext)).runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				Log.d(TAG, "tran.thang calcuateDifference deltaX = " + deltaX);
				if (Math.abs(deltaX) > 50) {
					if (deltaX < 0) {
//						Log.d(TAG, "tran.thang calcuateDifference right");
						
						if(holder.remindButtonLayout.getVisibility() == View.GONE){
							holder.deleteButton.setVisibility(View.VISIBLE);
							holder.img.setVisibility(View.GONE);
						}else{
							holder.remindButtonLayout.setVisibility(View.GONE);
						}
					}else {
//						Log.d(TAG, "tran.thang calcuateDifference left");
						holder.remindButton.setVisibility(View.VISIBLE);
					}
				}
			}
		});
	}

	private String convertTime(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		Date resultdate = new Date(Long.parseLong(time.trim()));
		return sdf.format(resultdate);
	}

	@Override
	public int getRealChildrenCount(int groupPosition) {
		return items.get(groupPosition).getmListNotification().size();
	}

	private final class NotificationButton extends Button implements
			OnClickListener {

		public NotificationButton(Context context, int id, String content) {
			super(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			setLayoutParams(lp);
			setText(content);
			setId(id);
			setGravity(Gravity.CENTER);
			setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case 1:
//				Log.i(TAG, "15 minutes");
				addEventCalendar(15);
				remindDialog.dismiss();
				if (mSample != null) {
					deleteNotification(mSample);
				}
				break;
			case 2:
				addEventCalendar(30);
//				Log.i(TAG, "30 minutes");
				remindDialog.dismiss();
				if (mSample != null) {
					deleteNotification(mSample);
				}
				break;
			case 3:
				addEventCalendar(60);
//				Log.i(TAG, "60 minutes");
				remindDialog.dismiss();
				if (mSample != null) {
					deleteNotification(mSample);
				}
				break;
			case 4:
				addEventCalendar(120);
//				Log.i(TAG, "120 minutes");
				remindDialog.dismiss();
				if (mSample != null) {
					deleteNotification(mSample);
				}
				break;
			default:
				break;
			}
		}

		private void deleteNotification(Sample sample) {
			int child = sample.childPos;
			int parent = sample.parentPos;
			mContext.getContentResolver().delete(
					SmartLockScreenNotificationProvider.URI_TBL_NOTIFICATION,
					tblNotification._ID + "=?",
					new String[] { items.get(parent).getmListNotification()
							.get(child).get_ID()
							+ "" });
			items.remove(parent);
		}

		private void addEventCalendar(int time) {
			calendarNoti = new CalendarNotification(mContext);
			calendarNoti.addEvent("At " + timeCalendar + " : " + titleCalendar,
					texthillCalendar, time);
		}
	}

	public LockScreenActivity getLockScreenActivity() {
		return lockScreenActivity;
	}

	public void setLockScreenActivity(LockScreenActivity lockScreenActivity) {
		this.lockScreenActivity = lockScreenActivity;
	}

	private void cancelNotification(MyNotification myNotification) {
		NotificationManager nMgr = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancel(myNotification.getNotificationID());
	}
}
