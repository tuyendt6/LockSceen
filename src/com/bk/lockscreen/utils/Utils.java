package com.bk.lockscreen.utils;

import java.util.HashMap;

import android.app.PendingIntent;

public class Utils {
	public static HashMap<Integer, PendingIntent> hashMap = new HashMap<Integer, PendingIntent>();
	public static final String BACKGROUNDPREFERENCE = "preference_background";
	public static final String CURRENTBACKGROUND = "current_background";
	public static final String CURRENTGUESTURE = "current_guesture";
}
