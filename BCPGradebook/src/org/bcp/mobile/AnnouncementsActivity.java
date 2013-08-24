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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AnnouncementsActivity extends SlidingFragmentActivity {
	MenuListFragment mFrag;
	SlidingMenu sm;
	private PullToRefreshListView myList;
	private NewsAdapter adapter;
	private ArrayList<Item> listContent = new ArrayList<Item>();
	private String url = "http://greco.bcp.org/webs/svc/anc/daily.php";
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
		
		//listContent.add(new News("Hey, these don't look like announcements. Give me my announcements!", "", "You're right, these aren't the announcements you're looking for.  The announcements feature is currently under development and will most likely be implemented a week or two into the school year."));
		//listContent.add(new News("But where can I check the announcements in the meantime?", "", "Just click here to open the announcements in your phone's browser."));
		//listContent.add(new News("You're awesome! How can I ever repay you?", "", "Glad to be of help! I'd appreciate it if you could rate this app on the Google Play Store. Check the sidebar for the link."));
		
		//android.R.layout.simple_list_item_1
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
				
				doc = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
				//doc = Jsoup.connect(url).timeout(7000).get();
				Elements divs = doc.select("div");
				String tempSubtitle = "";
				for(Element div : divs) {
					//System.out.println("asdf Checking div class: " + div.className() + ", " + div.text());
					if(div.className().equals("title")) {
						listContent.add(new SectionItem(div.text()));
					}
					else if(div.className().equals("sub_title")) {
						tempSubtitle = div.text();
					}
					else if(div.className().equals("dets")) {
						listContent.add(new News(tempSubtitle, "http://greco.bcp.org/webs/svc/anc/daily.php", div.text()));
					}
					else {
						// wat how did we get here
					}
				}
				/*
				Elements allDays = doc.select("dl.calendar-day");
				for(Element oneDay : allDays) {
					System.out.println("SOUP elem: " + oneDay.text());
					String month = oneDay.select("dt span.month").first().text();
					String day = oneDay.select("dt span.date").first().text();
					String dayOfWeek = oneDay.select("dt span.day").first().text();
					String textFull = "";
					Elements texts = oneDay.select("dd");
					for(Element text : texts) {
						textFull += text.text() + "\n";
					}
					
					if(textFull.endsWith("\n")) {
						textFull = textFull.substring(0, textFull.length() - 1);
					}
					listContent.add(new Event(month, day, dayOfWeek, 
							textFull.replaceAll("Location:", "\nLocation:").replaceAll("Time:", "\nTime:").replaceAll("Visit this Link", "")));
							*/
				
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
				displayCrouton("UPDATED", 1000, Style.INFO);
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
