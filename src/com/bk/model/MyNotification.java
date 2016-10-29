package com.bk.model;

public class MyNotification {

	private int _ID;
	private int NotificationID;
	private String PakageNam;
	private String tiker;
	private String title;
	private String text;
	private int GroupID;
	private String time;
	private int icon;

	public MyNotification() {
	}

	public MyNotification(int id, int notificationID, String pakageNam,
			String tiker, String title, String text, int groupid, String time,
			int icon) {
		super();
		_ID = id;
		NotificationID = notificationID;
		PakageNam = pakageNam;
		this.tiker = tiker;
		this.title = title;
		this.text = text;
		GroupID = groupid;
		this.time = time;
		this.icon = icon;
	}

	public String getPakageNam() {
		return PakageNam;
	}

	public void setPakageNam(String pakageNam) {
		PakageNam = pakageNam;
	}

	public String getTiker() {
		return tiker;
	}

	public void setTiker(String tiker) {
		this.tiker = tiker;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getGroupID() {
		return GroupID;
	}

	public void setGroupID(int groupID) {
		GroupID = groupID;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int get_ID() {
		return _ID;
	}

	public void set_ID(int _ID) {
		this._ID = _ID;
	}

	public int getNotificationID() {
		return NotificationID;
	}

	public void setNotificationID(int notificationID) {
		NotificationID = notificationID;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "PakageNam : " + PakageNam + " GroupID:" + GroupID
				+ " NotificationID : " + NotificationID + " Title : " + title
				+ " Text " + text;
	}

}
