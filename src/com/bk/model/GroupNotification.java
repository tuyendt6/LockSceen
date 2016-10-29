package com.bk.model;

import java.util.ArrayList;

public class GroupNotification {

	private int groupID;
	private String Name;
	private ArrayList<MyNotification> mListNotification;
	private boolean Checked;

	public GroupNotification() {
	}

	public GroupNotification(int groupID, String name,
			ArrayList<MyNotification> mListNotification, boolean checked) {
		super();
		this.groupID = groupID;
		Name = name;
		this.mListNotification = mListNotification;
		Checked = checked;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public ArrayList<MyNotification> getmListNotification() {
		return mListNotification;
	}

	public void setmListNotification(ArrayList<MyNotification> mListNotification) {
		this.mListNotification = mListNotification;
	}

	public boolean isChecked() {
		return Checked;
	}

	public void setChecked(boolean checked) {
		Checked = checked;
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	@Override
	public String toString() {
		return Name;
	}
}
