package com.bk.customadapter;


import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bk.lockscreen.R;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
	private List<ApplicationInfo> appsList = null;
	private Context context;
	private PackageManager packageManager;
	private ViewHolder holder;

	public ApplicationAdapter(Context context, int textViewResourceId,
			List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.context = context;
		this.appsList = appsList;
		packageManager = context.getPackageManager();
	}

	@Override
	public int getCount() {
		return ((null != appsList) ? appsList.size() : 0);
	}

	@Override
	public ApplicationInfo getItem(int position) {
		return ((null != appsList) ? appsList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == convertView) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.snippet_list_row,
					null);
			holder = new ViewHolder();
			holder.appName = (TextView) convertView.findViewById(R.id.app_name);
			holder.packageName = (TextView) convertView.findViewById(R.id.app_paackage);
			holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ApplicationInfo applicationInfo = appsList.get(position);
		if (null != applicationInfo) {

			holder.appName.setText(applicationInfo.loadLabel(packageManager));
			holder.packageName.setText(applicationInfo.packageName);
			holder.appIcon.setImageDrawable(applicationInfo
					.loadIcon(packageManager));
		}
		return convertView;
	}
	public static class ViewHolder {
		TextView appName;
		TextView packageName;
		ImageView appIcon;
	}
}
