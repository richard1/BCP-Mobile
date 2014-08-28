package org.bcp.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.bcp.mobile.lib.EventsAdapter;
import org.bcp.mobile.lib.Item;
import org.bcp.mobile.lib.SectionItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.bcp.mobile.R;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class CalendarActivity extends SlidingFragmentActivity {
	
	private static final String CALENDAR_MODULE = "&ModuleID=203:228:255:199:202:312:314:379:475:480:490:501:255:377";

	private MenuListFragment mFrag;
	private SlidingMenu sm;
	private String calUrl1 = "http://www.bcp.org/calendars/index.aspx?&StartDate=";
	private String calUrl2 = "http://www.bcp.org/calendars/index.aspx?&StartDate=";
	private ArrayList<Item> events = new ArrayList<Item>();
	private PullToRefreshListView myList;
	private EventsAdapter adapter;
	private OnItemClickListener listener;
	private int month;
	private int year;
	private int dayOfMonth;
	private int closestItem = 0;
	private int closestDay = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);
		setTitle("Calendar");
		setBehindContentView(R.layout.menu_frame);
		
		Calendar c = Calendar.getInstance(); 
		month = c.get(Calendar.MONTH) + 1; // January -> 0, December -> 11 ... this corrects it		
		year = c.get(Calendar.YEAR);
		dayOfMonth = c.get(Calendar.DATE);
		calUrl1 += month + "/1/" + year + CALENDAR_MODULE;
		if(month == 12) {
			calUrl2 += "1/1/" + (year + 1) + CALENDAR_MODULE;
		}
		else {
			calUrl2 += (month + 1) + "/1/" + year + CALENDAR_MODULE;
		}
		
		displayCrouton("GETTING EVENTS IN " + getStringMonth(month) + "...", 3000, Style.INFO);
		
		listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String url = calUrl2;
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        	startActivity(browserIntent);
			}
		};
		
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
				
		myList = (PullToRefreshListView) findViewById(R.id.events_list);
		adapter = new EventsAdapter(this, R.layout.events_row, events);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
		
		myList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new DownloadEventsTask().execute("");
			}
		});
		new DownloadEventsTask().execute("");
	}
	
	private class DownloadEventsTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Document doc;
			try {
				events.clear();
				events.add(new SectionItem(monthToString(month) + " " + year));
				int numEvents = 0;
				doc = Jsoup.connect(calUrl1).get();
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
					events.add(new Event(month, day, dayOfWeek, 
							textFull.replaceAll("Location:", "\nLocation:").replaceAll("Time:", "\nTime:").replaceAll("Visit this Link", "")));
					numEvents++;
					if(Math.abs(dayOfMonth - Integer.parseInt(day)) < Math.abs(dayOfMonth - closestDay)) {
						closestItem = numEvents + 1;
						closestDay = Integer.parseInt(day);
					}
				}
				
				events.add(new SectionItem(monthToString((month % 12) + 1) + " " + (month == 12 ? year + 1 : year)));
				doc = Jsoup.connect(calUrl2).get();
				allDays = doc.select("dl.calendar-day");
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
					events.add(new Event(month, day, dayOfWeek, 
							textFull.replaceAll("Location:", "\nLocation:").replaceAll("Time:", "\nTime:").replaceAll("Visit this Link", "")));
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
	
	public void scrollToPosition(int position) {
		myList.getRefreshableView().smoothScrollToPosition(position);
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	public void refreshList() {
		myList = (PullToRefreshListView) findViewById(R.id.events_list);
		adapter = new EventsAdapter(this, R.layout.events_row, events);
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
		Crouton.makeText(CalendarActivity.this, text, style1).setConfiguration(config).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Scroll")
            .setIcon(R.drawable.calendar_today)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if(item.getTitle().equals("Scroll")) {
			scrollToPosition(closestItem);
			return true;
		}
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public String getStringMonth(int theMonth) {
		// Please don't judge me
		switch(theMonth) {
			case 1: return "JAN & FEB";
			case 2: return "FEB & MAR";
			case 3: return "MAR & APR";
			case 4: return "APR & MAY";
			case 5: return "MAY & JUNE";
			case 6: return "JUNE & JULY";
			case 7: return "JULY & AUG";
			case 8: return "AUG & SEPT";
			case 9: return "SEPT & OCT";
			case 10: return "OCT & NOV";
			case 11: return "NOV & DEC";
			case 12: return "DEC & JAN";
		}
		return "";
	}
	
	public String formatEventText(String theText) {
		String newText = theText;
		newText.replaceAll("Location:", "\nLocation:").replaceAll("Time:", "\nTime:").replaceAll("Visit this Link", "");
		return newText;
	}
	
	public String monthToString(int month) {
		switch(month) {
			case 1: return "January";
			case 2: return "February";
			case 3: return "March";
			case 4: return "April";
			case 5: return "May";
			case 6: return "June";
			case 7: return "July";
			case 8: return "August";
			case 9: return "September";
			case 10: return "October";
			case 11: return "November";
			case 12: return "December";
			default: return "ERROR";
		}
	}
	
	public class Event implements Item {
		public String month;
		public String day;
		public String dayOfWeek;
		public String text;
		
		public Event(String month, String day, String dayOfWeek, String text) {
			this.month = month;
			this.day = day;
			this.dayOfWeek = dayOfWeek;
			this.text = text;
		}
		
		public String toString() {
			return dayOfWeek + ", " +  month + " " + day + ": " + text;
		}

		@Override
		public boolean isSection() {
			return false;
		}
	}
}
