package com.bk.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author tuyen.px
 */

public class tblGroupNotification {
	public static final String TBL_NAME = "tbl_GroupNotification";

	public static final String _ID = "id";// 1

	public static final String GROUP_ID = "group_id";// 2
	public static final String GROUP_NAME = "group_name";// 3

	private static String createData() {
		StringBuilder sBuiler = new StringBuilder();
		sBuiler.append("create table " + TBL_NAME + " (");
		sBuiler.append(_ID + " integer primary key autoincrement, ");
		sBuiler.append(GROUP_ID + " integer, ");
		sBuiler.append(GROUP_NAME + " text);");
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
