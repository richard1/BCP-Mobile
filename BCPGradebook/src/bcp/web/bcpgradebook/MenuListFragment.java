package bcp.web.bcpgradebook;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuListFragment extends ListFragment {
	
	SampleAdapter adapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new SampleAdapter(getActivity());
		//for (int i = 0; i < 20; i++) {
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_menu_search));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_dialog_info));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_menu_day));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_lock_silent_mode_off));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_menu_my_calendar));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_menu_gallery));
			adapter.add(new SampleItem("Sample List", android.R.drawable.ic_menu_view));
			
			adapter.add(new SampleItem("About", R.drawable.icon_about));
			adapter.add(new SampleItem("Announcements", R.drawable.icon_announcements));
			adapter.add(new SampleItem("Grades", R.drawable.icon_grades));
			adapter.add(new SampleItem("Log Out", R.drawable.icon_logout));
			adapter.add(new SampleItem("News", R.drawable.icon_news));
			adapter.add(new SampleItem("Rate", R.drawable.icon_rate));
		//}
		setListAdapter(adapter);
	}
	
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(getActivity(), ((SampleItem)adapter.getItem(position)).tag, Toast.LENGTH_SHORT).show();
    }

	private class SampleItem {
		public String tag;
		public int iconRes;
		public SampleItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
}