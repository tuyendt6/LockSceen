package com.bk.contentprovider;

import com.bk.table.tblAplicationInGroup;
import com.bk.table.tblGroupNotification;
import com.bk.table.tblNotification;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author tuyen.px
 */

public class SmartLockScreenNotificationProvider extends ContentProvider {

	public static final String PRO_NAME = "com.bk.contentprovider";
	public static final Uri URI_TBL_GROUPNOTIFICATION = Uri.parse("content://"
			+ PRO_NAME + "/" + tblGroupNotification.TBL_NAME);
	public static final Uri URI_TBL_NOTIFICATION = Uri.parse("content://"
			+ PRO_NAME + "/" + tblNotification.TBL_NAME);
	public static final Uri URI_TBL_APPLICATIONINGROUP = Uri.parse("content://"
			+ PRO_NAME + "/" + tblAplicationInGroup.TBL_NAME);

	private static final int M_GROUPNOTIFICATION = 0;
	private static final int M_NOTIFICATION = 1;
	private static final int M_APPLICATIONINGROUP = 2;
	private static final String TAG = "SmartLockScreenNotificationProvider";

	public static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PRO_NAME, tblGroupNotification.TBL_NAME,
				M_GROUPNOTIFICATION);
		uriMatcher.addURI(PRO_NAME, tblNotification.TBL_NAME, M_NOTIFICATION);
		uriMatcher.addURI(PRO_NAME, tblAplicationInGroup.TBL_NAME,
				M_APPLICATIONINGROUP);
	}

	private static final String DATABASE_NAME = "Database_SmarLockScreen";

	private static final int DATABASE_VERSION = 1;

	private DatabaseHelper mDbHelper;

	// using DatabaseHelper Class to help manage your database
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			tblGroupNotification.onCreate(db); // 0
			tblNotification.onCreate(db);
			tblAplicationInGroup.onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			tblGroupNotification.onUpgrade(db, oldVersion, newVersion); // 0
			tblNotification.onUpgrade(db, oldVersion, newVersion); // 0
			tblAplicationInGroup.onUpgrade(db, oldVersion, newVersion);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = "";
		switch (uriMatcher.match(uri)) {
		case M_GROUPNOTIFICATION: // 0
			table = tblGroupNotification.TBL_NAME;
			break;
		case M_NOTIFICATION: // 0
			table = tblNotification.TBL_NAME;
			break;
		case M_APPLICATIONINGROUP: // 0
			table = tblAplicationInGroup.TBL_NAME;
			break;
		default:
			break;
		}
		Log.e(TAG, "Xoa table " + table);

		SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
		int deleteCount = sqlDB.delete(table, selection, selectionArgs);
		Log.e(TAG, "Tong so dong da xoa = " + deleteCount);

		// Thong bao den cac observer ve su thay doi
		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		int key = uriMatcher.match(uri);
		String table = "";

		switch (key) {
		case M_GROUPNOTIFICATION: // 1
			table = tblGroupNotification.TBL_NAME;
			break;
		case M_NOTIFICATION: // 2
			table = tblNotification.TBL_NAME;
			break;
		case M_APPLICATIONINGROUP: // 0
			table = tblAplicationInGroup.TBL_NAME;
			break;
		default:
			break;
		}

		SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
		long rowID = sqlDB.insertWithOnConflict(table, "", values,
				SQLiteDatabase.CONFLICT_REPLACE);
		getContext().getContentResolver().notifyChange(uri, null);
		if (rowID > 0) {
			return Uri.withAppendedPath(uri, String.valueOf(rowID));
		} else {
			return null;

		}
	}

	@Override
	public boolean onCreate() {

		Context context = getContext();
		mDbHelper = new DatabaseHelper(context);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		int key = uriMatcher.match(uri);
		String table = "";

		switch (key) {
		case M_GROUPNOTIFICATION:// 1
			table = tblGroupNotification.TBL_NAME;
			break;
		case M_NOTIFICATION:// 2
			table = tblNotification.TBL_NAME;
			break;
		case M_APPLICATIONINGROUP: // 0
			table = tblAplicationInGroup.TBL_NAME;
			break;
		default:
			break;
		}

		SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(table);
		sqlBuilder.setDistinct(true);
		Cursor c = sqlBuilder.query(sqlDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int key = uriMatcher.match(uri);
		String table = "";

		switch (key) {
		case M_GROUPNOTIFICATION:// 1
			table = tblGroupNotification.TBL_NAME;
			break;
		case M_NOTIFICATION:// 2
			table = tblNotification.TBL_NAME;
			break;
		case M_APPLICATIONINGROUP: // 0
			table = tblAplicationInGroup.TBL_NAME;
			break;
		default:
			break;
		}

		SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
		int rowEffect = sqlDB.update(table, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowEffect;
	}
}
