package com.bk.lockscreen;

import com.bk.contentprovider.SmartLockScreenNotificationProvider;
import com.bk.table.tblAplicationInGroup;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

public class GetAppInstallRemove extends BroadcastReceiver {

	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) { 
			String pakage = intent.getDataString();
		    Toast.makeText(mContext, " onReceive !!!! PACKAGE_REMOVED" + pakage, Toast.LENGTH_LONG).show();
		    mContext.getContentResolver().delete(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP, 
		    		tblAplicationInGroup.PACKAGE + "=?", new String[] {pakage});
		 
		} else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String pakage = intent.getDataString();
		    Toast.makeText(mContext, " onReceive !!!!." + "PACKAGE_ADDED" + pakage, Toast.LENGTH_LONG).show();
		    ContentValues cv = new ContentValues();
		    cv.put(tblAplicationInGroup.GROUP_ID, getGroupId(pakage));
		    cv.put(tblAplicationInGroup.PACKAGE, pakage);
		    mContext.getContentResolver().insert(SmartLockScreenNotificationProvider.URI_TBL_APPLICATIONINGROUP, cv);
        }
    }
	
	private int getGroupId(String packagename) {
		Cursor c = mContext.getContentResolver().query(
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
}
