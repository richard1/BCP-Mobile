package org.bcp.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bcp.mobile.lib.Item;
import org.bcp.mobile.lib.News;
import org.bcp.mobile.lib.NewsAdapter;
import org.bcp.mobile.lib.XmlParser;
import org.bcp.mobile.lib.XmlParser.Entry;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import org.bcp.mobile.R;

import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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

public class NewsRssActivity extends SlidingFragmentActivity {
	private MenuListFragment mFrag;
	private SlidingMenu sm;
	private PullToRefreshListView myList;
	private NewsAdapter adapter;
	private ArrayList<Item> listContent = new ArrayList<Item>();
	
	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String url = ((News)adapter.getItem(position - 1)).link;
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	startActivity(browserIntent);
		}
	};
	
	private HttpURLConnection conn;
	public static final int MAX_NEWS_ARTICLES = 25;
	public static final String NEWS_RSS_FEED = "http://www.bcp.org/news/rss.aspx?ModuleID=191";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_rss);
		setTitle("News");
		setBehindContentView(R.layout.menu_frame);
		
		displayCrouton("RETRIEVING NEWS...", 3000, Style.INFO);
		
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
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		myList = (PullToRefreshListView) findViewById(R.id.news_list);
		adapter = new NewsAdapter(this, R.layout.news_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
		
		myList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new DownloadNewsTask().execute("http://www.bcp.org/news/rss.aspx?ModuleID=191");
			}
		});
		
		new DownloadNewsTask().execute(NEWS_RSS_FEED);
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
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	private class DownloadNewsTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			try {
	            return loadXmlFromNetwork(urls[0]);
	        } catch (IOException e) {
	        	e.printStackTrace();
	            return "Connection error";
	        } catch (JSONException e) {
				e.printStackTrace();
				return "JSON error";
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return "XMLPullParser error";
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
	
	private String loadXmlFromNetwork(String urlString) throws IOException, JSONException, XmlPullParserException {
		InputStream stream = null;
		XmlParser xmlParser = new XmlParser();
		List<Entry> entries = null;

		try {
			stream = downloadUrl(urlString);        
			entries = xmlParser.parse(stream);
		} finally {
			if (stream != null) {
				stream.close();
			} 
		}

		listContent.clear();
		int count = 0;
		for (Entry entry : entries) { 
			listContent.add(new News(entry.title, entry.link, "Posted on " + entry.summary.substring(0, entry.summary.indexOf(":") - 3)));
			count++;
			if(count >= MAX_NEWS_ARTICLES) {
				break;
			}
		}
	    return "Success";		
	}

	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	public void refreshList() {
		myList = (PullToRefreshListView) findViewById(R.id.news_list);
		adapter = new NewsAdapter(this, R.layout.news_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
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
		Crouton.makeText(NewsRssActivity.this, text, style1).setConfiguration(config).show();
	}
}