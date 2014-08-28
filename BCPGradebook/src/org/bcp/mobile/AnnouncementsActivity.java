package org.bcp.mobile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.bcp.mobile.R;
import org.bcp.mobile.lib.Item;
import org.bcp.mobile.lib.News;
import org.bcp.mobile.lib.NewsAdapter;
import org.bcp.mobile.lib.SectionItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AnnouncementsActivity extends SlidingFragmentActivity {
	
	private static final String ANNOUNCEMENTS_URL = "http://times.bcp.org/anc/announcements/announcements.php";

	private MenuListFragment mFrag;
	private SlidingMenu sm;
	private PullToRefreshListView myList;
	private NewsAdapter adapter;
	private ArrayList<Item> listContent = new ArrayList<Item>();
	
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ANNOUNCEMENTS_URL));
        	startActivity(browserIntent);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_announcements);
		setTitle("Announcements");
		setBehindContentView(R.layout.menu_frame);
		
		displayCrouton("RETRIEVING ANNOUNCEMENTS", 3000, Style.INFO);
		
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
		
		myList = (PullToRefreshListView) findViewById(R.id.announcements_list);
		adapter = new NewsAdapter(this, R.layout.news_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
		myList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new DownloadAnnouncementsTask().execute("");
			}
		});
		new DownloadAnnouncementsTask().execute("");		
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
	
	private class DownloadAnnouncementsTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Document doc;
			try {
				listContent.clear();
				
				doc = Jsoup.parse(new URL(ANNOUNCEMENTS_URL).openStream(), "ISO-8859-1", ANNOUNCEMENTS_URL);
				Elements divs = doc.select("div");
				String tempSubtitle = "";
				for(Element div : divs) {
					if(div.className().equals("title")) {
						listContent.add(new SectionItem(div.text()));
					}
					else if(div.className().equals("sub_title")) {
						tempSubtitle = div.text();
					}
					else if(div.className().equals("dets")) {
						listContent.add(new News(tempSubtitle, ANNOUNCEMENTS_URL, div.text().replaceAll("More Info...", "")));
					}
					else {
						Log.e("announcements", "Unknown announements error");
					}
				}
				return "Success";
			} catch (IOException e) {
				e.printStackTrace();
				return "IO error";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if(!isOnline()) {
				displayCrouton("NO INTERNET CONNECTION", 3000, Style.ALERT);
			} else {
				refreshList();
			}
			myList.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
	
	public void refreshList() {
		myList = (PullToRefreshListView) findViewById(R.id.announcements_list);
		adapter = new NewsAdapter(this, R.layout.news_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	public void displayCrouton(String text, int timeMilli, Style style) {
		Style style1;
		if(style.equals(Style.ALERT)) {
			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoRedLight).build();
		} else if(style.equals(Style.CONFIRM)) {
			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoGreenLight).build();
		} else {
			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoBlueLight).build();
		}
		Configuration config = new Configuration.Builder().setDuration(timeMilli).build();
		Crouton.makeText(AnnouncementsActivity.this, text, style1).setConfiguration(config).show();
	}
}
