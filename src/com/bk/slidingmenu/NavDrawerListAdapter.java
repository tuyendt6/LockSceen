package com.bk.slidingmenu;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bk.lockscreen.R;

public class NavDrawerListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
	public List<ViewHolder> listView=new ArrayList<ViewHolder>();
	private LayoutInflater mInflater;
	public NavDrawerListAdapter(Context context,
			ArrayList<NavDrawerItem> navDrawerItems) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.navDrawerItems = navDrawerItems;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {			
			convertView = mInflater.inflate(R.layout.drawer_list_item, null);
			holder=new ViewHolder();
			holder.imgIcon = (ImageView) convertView.findViewById(R.id.icon);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			holder.txtCount = (TextView) convertView.findViewById(R.id.counter);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}

		holder.imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
		holder.txtTitle.setText(navDrawerItems.get(position).getTitle());

		if(position==0)
			holder.txtTitle .setTextColor(context.getResources().getColor(android.R.color.white));
		else
			holder.txtTitle .setTextColor(context.getResources().getColor(R.color.title));
		if (navDrawerItems.get(position).getCounterVisibility()) {
			holder.txtCount .setText(navDrawerItems.get(position).getCount());
		} else {
			// hide the counter view
			holder.txtCount .setVisibility(View.GONE);
		}
		if(!listView.contains(holder))
			listView.add(holder);

		convertView.setTag(holder);
		
		return convertView;
	}
	public class ViewHolder{
		public ImageView imgIcon;
		public TextView txtTitle;
		public TextView txtCount;
	}
	public void resetAll(int position){
		for (int i = 0; i < listView.size(); i++) {
			listView.get(i).txtTitle.setTextColor(context.getResources().getColor(R.color.title));
		}
		listView.get(position).txtTitle.setTextColor(context.getResources().getColor(android.R.color.white));
	}
}
