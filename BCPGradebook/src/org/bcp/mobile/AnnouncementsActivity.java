package org.bcp.mobile;

import java.util.ArrayList;

import org.bcp.mobile.R;
import org.bcp.mobile.lib.News;
import org.bcp.mobile.lib.NewsAdapter;

import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AnnouncementsActivity extends SlidingFragmentActivity {
	MenuListFragment mFrag;
	SlidingMenu sm;
	private ListView myList;
	private NewsAdapter adapter;
	private ArrayList<News> listContent = new ArrayList<News>();
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String url = "http://www.bcp.org/students/student-life/daily-announcements/index.aspx";
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	startActivity(browserIntent);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_announcements);
		setTitle("Announcements");
		setBehindContentView(R.layout.menu_frame);
		
		if (savedInstanceState == null) {
			mFrag = new MenuListFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, mFrag).commit();
		} else {
			mFrag = (MenuListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}
		
		sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE); // fullscreen is bad

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		listContent.add(new News("Hey, these don't look like announcements. Give me my announcements!", "", "You're right, these aren't the announcements you're looking for.  The announcements feature is currently under development and will most likely be implemented a week or two into the school year."));
		listContent.add(new News("But where can I check the announcements in the meantime?", "", "Just click here to open the announcements in your phone's browser."));
		listContent.add(new News("You're awesome! How can I ever repay you?", "", "Glad to be of help! I'd appreciate it if you could rate this app on the Google Play Store. Check the sidebar for the link."));
		
		//android.R.layout.simple_list_item_1
		myList = (ListView) findViewById(R.id.announcements_list);
		adapter = new NewsAdapter(this, R.layout.news_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
	}

	@Override
	public void onBackPressed() {
		if(sm.isMenuShowing()) {
			toggle();
		}
		else {
			super.onBackPressed();
		}
	}
	
	protected void onResume() {
		super.onResume();
		if(sm.isMenuShowing()) {
			toggle();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
}
