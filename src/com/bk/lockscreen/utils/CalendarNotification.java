package com.bk.lockscreen.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;
import android.widget.Toast;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class CalendarNotification {
	private final String TAG = "CalendarNotification";
	private Context mContext;
	
	public CalendarNotification(Context mContext){
		this.mContext = mContext;
	}
	
	private long getCalendarId() {
        String[] projection = new String[] { Calendars._ID };
        Cursor cursor = mContext.getContentResolver().query(Calendars.CONTENT_URI,
                projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        return -1;
    }
	
	public void addEvent(String title, String content, int minute) {
        Log.d(TAG, "tran.thang addEvent-Start");
        long calID = getCalendarId();

        if (calID == -1){
            Log.e(TAG, "tran.thang addEvent-Not found calID");
            return;
        }

        long startMillis = 0;
        long endMillis = 0;
        
        String date[]=getDate(minute*60*1000).split("-");
        //Log.e(TAG, "tran.thang date[] = " + getDate(minute*60*1000));
        
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Integer.parseInt(date[0]), Integer.parseInt(date[1])-1, Integer.parseInt(date[2]), "am".equalsIgnoreCase(date[5])?Integer.parseInt(date[3]):Integer.parseInt(date[3])+12, Integer.parseInt(date[4]));
        startMillis = beginTime.getTimeInMillis();
        
        Calendar endTime = Calendar.getInstance();       
        endTime.set(Integer.parseInt(date[0]), Integer.parseInt(date[1])-1, Integer.parseInt(date[2]), "am".equalsIgnoreCase(date[5])?Integer.parseInt(date[3]):Integer.parseInt(date[3])+12, Integer.parseInt(date[4])+1);
        endMillis = endTime.getTimeInMillis();
        
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.EVENT_TIMEZONE, "Asia/Ho_Chi_Minh");
        values.put(Events.TITLE, title);
        values.put(Events.DESCRIPTION, content);
        values.put(Events.CALENDAR_ID, calID);   
        Uri uri = cr.insert(Events.CONTENT_URI, values);
        
        long eventID = Long.parseLong(uri.getLastPathSegment());
        if (eventID <= 0){
            Log.e(TAG, "tran.thang addEvent-Could not create event");
            return;
        }
      
        cr = mContext.getContentResolver();
        values = new ContentValues();
        values.put(Reminders.MINUTES, 0);
        values.put(Reminders.EVENT_ID, eventID);
        values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
        uri = cr.insert(Reminders.CONTENT_URI, values);
        eventID = Long.parseLong(uri.getLastPathSegment());
        Log.i(TAG, "tran.thang addEvent-Remind id:"+eventID);
        
        // Delete notification here
        Toast.makeText(mContext, "Notification was added to calendar!", Toast.LENGTH_SHORT).show();
    }
	
	private String getDate(long time){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-hh-mm-a");
        Date date=new Date(System.currentTimeMillis()+time);
        return format.format(date);
    }
}
