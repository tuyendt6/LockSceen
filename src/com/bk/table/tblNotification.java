package com.bk.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author tuyen.px
 */

public class tblNotification {
	public static final String TBL_NAME = "tbl_Notification";

	public static final String _ID = "id";// 1

	public static final String NOTIFICATION_ID = "noticication_id";

	public static final String GROUP_ID = "group_id";// 2
	public static final String TITLE_NOTIFICATION = "title_notification";// 3
	public static final String CONTENT_NOTIFICATION = "content_notification"; // 4
	public static final String PACKAGE_NAME = "package_name";// 5
	public static final String TIME_NOTIFICATION = "time_notification";// 6
	public static final String ICON_NOTIFICATION = "icon_notification";// 6
	public static final String COUNT_NOTIFICATION = "count_notification";// 6

	private static String createData() {
		StringBuilder sBuiler = new StringBuilder();
		sBuiler.append("create table " + TBL_NAME + " (");
		sBuiler.append(_ID + " integer primary key autoincrement, ");
		sBuiler.append(GROUP_ID + " integer, ");
		sBuiler.append(NOTIFICATION_ID + " integer, ");
		sBuiler.append(TITLE_NOTIFICATION + " text, ");
		sBuiler.append(CONTENT_NOTIFICATION + " text, ");
		sBuiler.append(PACKAGE_NAME + " text, ");
		sBuiler.append(ICON_NOTIFICATION + " integer, ");
		sBuiler.append(COUNT_NOTIFICATION + " integer, ");
		sBuiler.append(TIME_NOTIFICATION + " text);");
		return sBuiler.toString();
	}

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(createData());
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
		onCreate(database);
	}
}
