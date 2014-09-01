package org.bcp.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bcp.mobile.lib.Assignment;
import org.bcp.mobile.lib.AssignmentsDatabase;
import org.bcp.mobile.lib.DatabaseHandler;
import org.bcp.mobile.lib.Grade;
import org.bcp.mobile.lib.GradeAdapter;
import org.bcp.mobile.lib.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import org.bcp.mobile.R;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class GradeViewActivity extends SlidingFragmentActivity {

	public static final int SEMESTER_ONE_POSITION = 0;
	public static final int SEMESTER_TWO_POSITION = 1;
	public static final String COURSE_ID = "bcp.web.bcpgradebook.courseid";
	
	public static final int NOTIF_BROADCAST_ID = 254254;
	
	private String gradesUrl;
	private ArrayList<Grade> semesterList1 = new ArrayList<Grade>();
	private ArrayList<Grade> semesterList2 = new ArrayList<Grade>();
	private ArrayList<Item> assignmentList = new ArrayList<Item>();
	
	private String username;
	private String encryptedPassword;

	private ProgressDialog progress;
	private DatabaseHandler db;
	private AssignmentsDatabase adb;
	private DecimalFormat decimalFormat = new DecimalFormat("##0.00");
	private ViewPager mViewPager;
	private TitlePageIndicator mIndicator;
	private GradePagerAdapter mAdapter;
	private SlidingMenu sm;

	private GradeFragment semesterOneFragment;
	private GradeFragment semesterTwoFragment;
	
	private MenuListFragment mFrag;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		NotificationService.resetNotificationInfo();
				
		Bundle humbleBundle = new Bundle();
		humbleBundle.putBoolean("showSemesterOne", true);
		semesterOneFragment = new GradeFragment();
		semesterOneFragment.setArguments(humbleBundle);

		humbleBundle = new Bundle();
		humbleBundle.putBoolean("showSemesterOne", false);
		semesterTwoFragment = new GradeFragment();
		semesterTwoFragment.setArguments(humbleBundle);
		
		setBehindContentView(R.layout.menu_frame);
		
		if (savedInstanceState == null) {
			mFrag = new MenuListFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, mFrag).commit();
		} else {
			mFrag = (MenuListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}
		
		setContentView(R.layout.main);
		sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE); // fullscreen is bad

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mViewPager = (ViewPager) findViewById(R.id.crouton_pager);
		mAdapter = new GradePagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mIndicator = (TitlePageIndicator) findViewById(R.id.titles);
		mIndicator.setViewPager(mViewPager);
		//mIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
				
		Crouton.cancelAllCroutons();
		db = new DatabaseHandler(this);
		adb = new AssignmentsDatabase(this);

		setTitle("Grades");

		semesterList1.addAll((ArrayList<Grade>) db.getAllWithSemester(1));
		semesterList2.addAll((ArrayList<Grade>) db.getAllWithSemester(2));
		assignmentList.addAll((ArrayList<Item>) adb.getAll());

		Calendar c = Calendar.getInstance(); 
		int month = c.get(Calendar.MONTH) + 1; // January -> 0, December -> 11 ... this corrects it		
		if(month < 8) { // if in between January 1 and July 31, set default to Semester 2
			mIndicator.setCurrentItem(mAdapter.getCount() - 1);
		}

		Intent intent = this.getIntent();
		username = intent.getStringExtra("username");
		username = (username == null) ? getSharedPreferences("username", MODE_PRIVATE).getString("username", "") : username;
		encryptedPassword = intent.getStringExtra("encryptedPassword");
		encryptedPassword = (encryptedPassword == null) ? getSharedPreferences("password", MODE_PRIVATE).getString("password", "") : 
			encryptedPassword;

		gradesUrl = "http://brycepauken.com/api/3539/grades.php?username=" + username + "&password=" + encryptedPassword;
		
		final AlertDialog.Builder builderConfirm = new AlertDialog.Builder(this);
		builderConfirm.setTitle("Great!");
		builderConfirm.setMessage("To change your notification settings, tap on Settings in the sidebar.");
		builderConfirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        	}
        });
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Welcome" + (username != null && username.length() > 0 ? ", " + getNameFromUsername(username)[0] : ""));
        builder.setMessage("Want notifications?\n\nWe'll automatically check for grade updates throughout the day " +
        		"and let you know of any changes.");
        builder.setPositiveButton("Sign me up!", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		SharedPreferences frequencyPref = getSharedPreferences("frequency", Context.MODE_PRIVATE);
        		frequencyPref.edit().putInt("frequency", NotificationService.GUN_TWELVE_HOUR).apply();
        		setNotificationAlarm();
        		builderConfirm.show();
        	}
        });
        builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		SharedPreferences frequencyPref = getSharedPreferences("frequency", Context.MODE_PRIVATE);
        		frequencyPref.edit().putInt("frequency", NotificationService.GUN_NEVER).apply();
        		setNotificationAlarm();
	       	}
	   	});
        
		progress = new ProgressDialog(this);
		progress.setTitle("Welcome" + (username != null && username.length() > 0 ? ", " + getNameFromUsername(username)[0] : ""));
		progress.setMessage("Fetching your grades...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		
		SharedPreferences hasLoggedInPref = getSharedPreferences("hasLoggedIn", MODE_PRIVATE);

		if(hasLoggedInPref.getInt("hasLoggedIn", 0) == 0) {
			builder.show();
			getSharedPreferences("hasLoggedIn", MODE_PRIVATE).edit().putInt("hasLoggedIn", 1).apply();
		}
		else {
			progress.show();
		}
		
		if(!isOnline()) {
			progress.dismiss();
		} else {
			System.out.println("CONNECTED!");
		}
		
		setNotificationAlarm();

		new DownloadGradesTask().execute(gradesUrl);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	protected void onResume() {
		super.onResume();
		if(sm.isMenuShowing()) {
			toggle();
		}
		NotificationService.resetNotificationInfo();
	}
	
	private boolean setNotificationAlarm() {
		SharedPreferences frequencyPref = getSharedPreferences("frequency", Context.MODE_PRIVATE);
		int currentFrequency = frequencyPref.getInt("frequency", NotificationService.GUN_TWELVE_HOUR);
		
		NotificationService.resetNotificationInfo();
		
		try {
			Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, GradeViewActivity.NOTIF_BROADCAST_ID,
					broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			
			if(currentFrequency == NotificationService.GUN_NEVER) {
				alarms.cancel(pendingIntent);
				return true;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			int unroundedMinutes = calendar.get(Calendar.MINUTE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int mod;
			
			long alarmInterval;
			
			switch(currentFrequency) {
				case NotificationService.GUN_FIFTEEN_MIN:
					alarmInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
					mod = unroundedMinutes % 15;
					calendar.add(Calendar.MINUTE, 15 - mod);
					break;
				case NotificationService.GUN_THIRTY_MIN:
					alarmInterval = AlarmManager.INTERVAL_HALF_HOUR;
					mod = unroundedMinutes % 30;
					calendar.add(Calendar.MINUTE, 30 - mod);
					break;
				case NotificationService.GUN_ONE_HOUR:
					alarmInterval = AlarmManager.INTERVAL_HOUR;
					mod = unroundedMinutes % 60;
					calendar.add(Calendar.MINUTE, 60 - mod);
					break;
				case NotificationService.GUN_TWELVE_HOUR:
					alarmInterval = AlarmManager.INTERVAL_HALF_DAY;
					calendar.set(Calendar.MINUTE, 0);
					if(hour < 8) {	// 8:00 am
						calendar.set(Calendar.HOUR_OF_DAY, 8);
					}
					else if(hour < 20) {  // 8:00pm
						calendar.set(Calendar.HOUR_OF_DAY, 20);
					}
					else {
						calendar.set(Calendar.HOUR_OF_DAY, 8);
						calendar.add(Calendar.DAY_OF_MONTH, 1);
					}				
					break;
				case NotificationService.GUN_ONE_DAY:
					alarmInterval = AlarmManager.INTERVAL_DAY;
					calendar.set(Calendar.MINUTE, 0);
					if(hour >= 12) {
						calendar.add(Calendar.DAY_OF_MONTH, 1);
					}
					calendar.set(Calendar.HOUR_OF_DAY, 12);
					
					break;
				default:
					alarmInterval = AlarmManager.INTERVAL_HALF_DAY;
					calendar.set(Calendar.MINUTE, 0);
					if(hour < 8) {	// 8:00 am
						calendar.set(Calendar.HOUR_OF_DAY, 8);
					}
					else if(hour < 20) {  // 8:00pm
						calendar.set(Calendar.HOUR_OF_DAY, 20);
					}
					else {
						calendar.set(Calendar.HOUR_OF_DAY, 8);
						calendar.add(Calendar.DAY_OF_MONTH, 1);
					}
					break;
			}
						
			//Toast.makeText(this, new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(calendar.getTime()), Toast.LENGTH_LONG).show();

			alarms.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
					alarmInterval, pendingIntent); 
		} 
		catch(Exception e) {
			System.out.println("Failed to set alarm");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	@SuppressLint("DefaultLocale")
	public String[] getNameFromUsername(String username) {
		String[] names = new String[3];
		if(username.contains("@")) { // if people log in with email address
			username = username.substring(0, username.indexOf("@"));
		}
		names[0] = username.substring(0, 1).toUpperCase() + username.substring(1, username.indexOf("."));
		names[1] = username.substring(username.indexOf(".") + 1, username.indexOf(".") + 2).toUpperCase() 
				+ username.substring(username.indexOf(".") + 2, username.length() - 2);
		names[2] = username.substring(username.length() - 2, username.length());
		return names;
	}

	public int getIdFromGrade(String grade) {
		// Please don't judge me.
		if(grade.equals("A+")) return R.drawable.grade_aplus;
		else if(grade.equals("A")) return R.drawable.grade_a;
		else if(grade.equals("A-")) return R.drawable.grade_aminus;
		else if(grade.equals("B+")) return R.drawable.grade_bplus;
		else if(grade.equals("B")) return R.drawable.grade_b;
		else if(grade.equals("B-")) return R.drawable.grade_bminus;
		else if(grade.equals("C+")) return R.drawable.grade_cplus;
		else if(grade.equals("C")) return R.drawable.grade_c;
		else if(grade.equals("C-")) return R.drawable.grade_cminus;
		else if(grade.equals("D+")) return R.drawable.grade_dplus;
		else if(grade.equals("D")) return R.drawable.grade_d;
		else if(grade.equals("D-")) return R.drawable.grade_dminus;
		else if(grade.equals("F")) return R.drawable.grade_f;
		return R.drawable.error;
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
		Crouton.makeText(GradeViewActivity.this, text, style1).setConfiguration(config).show();
	}

	@SuppressLint("ValidFragment")
	public class GradeFragment extends SherlockFragment {
		private boolean isSemesterOne;
		public PullToRefreshListView listView;
		OnItemClickListener listener;	
		GradeAdapter adapter;
		
		public GradeFragment() {
			// intentionally left empty
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			isSemesterOne = getArguments().getBoolean("showSemesterOne");
			return inflater.inflate(R.layout.activity_grade_view, null);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			listener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String title = ((Grade)adapter.getItem(position - 1)).title; // subtracting 1 to get correct index
					int semester = ((Grade)adapter.getItem(position - 1)).semester;
					Intent intent = new Intent(getBaseContext(), CourseDetailActivity.class);
					intent.putExtra(COURSE_ID, "Item #: " + position);
					intent.putExtra("title", title);
					intent.putExtra("semester", semester);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			};

			listView = (PullToRefreshListView) getView().findViewById(R.id.listView1);
			listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
				@Override
				public void onRefresh(PullToRefreshBase<ListView> refreshView) {
					new DownloadGradesTask().execute(gradesUrl);
				}
			});

			listView = (PullToRefreshListView) getView().findViewById(R.id.listView1);
			adapter = new GradeAdapter(getActivity(), R.layout.grade_item_row, isSemesterOne ? semesterList1 : semesterList2); // was mainlist
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(listener);
		}

		public void refreshList() {
			View view = getView();
			if(view != null) {
				listView = (PullToRefreshListView) getView().findViewById(R.id.listView1);
				adapter = new GradeAdapter(getActivity(), R.layout.grade_item_row, isSemesterOne ? semesterList1 : semesterList2); // was mainlist
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(listener);
			}
		}
	}

	private class GradePagerAdapter extends FragmentStatePagerAdapter {
		public GradePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			if (PageInfo.Crouton.ordinal() == position) {
				semesterOneFragment.setRetainInstance(true);
				return semesterOneFragment;
			} else if (PageInfo.About.ordinal() == position) {
				semesterTwoFragment.setRetainInstance(true);
				return semesterTwoFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return PageInfo.values().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if(position == 0) return "Semester 1";
			else if(position == 1) return "Semester 2";
			else return "What are you even doing I don't know how you got here please turn back now thanks";
		}
		
		@Override
	    public int getItemPosition(Object object) {
	        return PagerAdapter.POSITION_NONE;
	    }
	}

	enum PageInfo {
		Crouton(R.string.hello_world), About(R.string.hello_world); // random strings
		int titleResId;
		PageInfo(int titleResId) {
			this.titleResId = titleResId;
		}
	}

	private class DownloadGradesTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			System.out.println("starting download");
			try {
				if(!isOnline()) {
					return "No connection";
				}
				loadGradesFromNetwork(urls[0]);
				return "SUCCESS";
			} catch(SocketTimeoutException e) {
				e.printStackTrace();
				return "STE";
			} catch(IOException e) {
				e.printStackTrace();
				return "IOE";
			} catch(JSONException e) {
				e.printStackTrace();
				return "JSONE";
			} catch(Exception e) {
				System.out.println("ATTENTION\nERROR\nHEREITIS\n"+e.toString());
				e.printStackTrace();
				return "E";
			}
		}

		@Override
		protected void onPostExecute(String result) {

			if(!isOnline()) {
				displayCrouton("NO INTERNET CONNECTION", 3000, Style.ALERT);
			} else {
				semesterOneFragment.refreshList();
				semesterTwoFragment.refreshList();
				if(result.equals("SUCCESS")) {
					displayCrouton("UPDATED", 1000, Style.INFO);
				}
				else if(result.equals("STE")) {
					displayCrouton("SERVER TIMED OUT - PLEASE TRY AGAIN LATER", 3000, Style.ALERT);
				}
				else if(result.equals("IOE")) {
					displayCrouton("SOMETHING IS DOWN - PLEASE TRY AGAIN LATER", 3000, Style.ALERT);
				}
				else {
					displayCrouton("UNEXPECTED ERROR - " + result, 3000, Style.ALERT);
				}
			}
			progress.dismiss();
			semesterOneFragment.listView.onRefreshComplete();
			semesterTwoFragment.listView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	private void loadGradesFromNetwork(String urlString) throws IOException, JSONException {
		InputStream stream = null;
		String rawJson = "";
		try {
			rawJson = downloadUrl(urlString);
		} finally {
			if(stream != null)
				stream.close();
		}
		semesterList1.clear();
		semesterList2.clear();
		assignmentList.clear();
		JSONObject result = new JSONObject(rawJson);
		System.out.println("JSON GRES:" + result.toString());
		boolean error = result.has("error");
		if(error) {
			displayCrouton("AN ERROR OCCURRED, PLEASE TRY AGAIN LATER [" + result.getString("error") + "]", 3000, Style.ALERT);
			return;
		}
		
		for(int iter = 0; iter < 2; iter++)
		{
			HashMap<String, String> percentMap = db.getPercentTitleMap(iter+1);
			System.out.println("MAP: " + percentMap.toString());
	
			JSONArray sem = iter == 0 ? result.getJSONArray("semester1") : result.getJSONArray("semester2");
			ArrayList<Grade> activeList = iter == 0 ? semesterList1 : semesterList2;
			for(int i = 0; i < sem.length(); i++) {
				JSONObject row = sem.getJSONObject(i);
				if(!row.getString("course").equals("Homeroom") && row.has("percent") && !row.getString("percent").equals("null")) {
					String percent = row.getString("percent");
					String extraText = "";
					if(!percentMap.isEmpty()) {
						String courseName = row.getString("course");
						double oldPercent, newPercent;
						System.out.println("looking for: " + courseName + "\n" + percentMap.get(courseName));
						if(percentMap.get(courseName) == null) { // class not found - classes were changed since last refresh
							newPercent = Double.parseDouble(percent.replaceAll("%", ""));
							oldPercent = newPercent;
						}
						else {
							oldPercent = Double.parseDouble(percentMap.get(courseName).replaceAll("%", ""));
							newPercent = Double.parseDouble(percent.replaceAll("%", ""));
						}
						System.out.println(newPercent + " - " + oldPercent + " = " + (newPercent - oldPercent));
						extraText += "   " + (newPercent < oldPercent ? "" : "+") 
								+ decimalFormat.format((newPercent - oldPercent)) + "%";
					}
	
					Grade grade = new Grade(getIdFromGrade(row.getString("letter")), row.getString("course"), percent, iter + 1);
					grade.addExtraText(extraText);
					activeList.add(grade);
					
					JSONArray assignments = row.optJSONArray("assignments");
					if(assignments != null) {
						for(int j = 0; j < assignments.length(); j++) {
							JSONObject row2 = assignments.getJSONObject(j);
							String letter = row2.getString("letter");
							double score = 0.0;
							if(row2.getString("grade") != null && row2.getString("grade").length() > 0 && !row2.getString("grade").equals("X")) {
								score = Double.parseDouble(row2.getString("grade"));
							}
							
							double total = Double.parseDouble(row2.getString("max"));
							String percentage = "";
							if(total <= 0) {
								percentage = "Extra Credit";
								letter = "A+";
							}
							else {
								percentage = decimalFormat.format( ((double)score) / ((double)total) * 100.0) + "%";
							}
							String cleanedName = StringEscapeUtils.unescapeHtml4(row2.getString("name"));
							Assignment asg = new Assignment("Asg", row.getString("course"), cleanedName, 
									row2.getString("due"), row2.getString("category"), score,
									total, letter, percentage, iter + 1, "");
							assignmentList.add(asg);
						}
					}
					
					JSONArray cats = row.optJSONArray("categories");
					if(cats != null) {
						for(int j = 0; j < cats.length(); j++) {
							JSONObject row2 = cats.getJSONObject(j);
							String rawScore = row2.getString("points");
							String letter = row2.getString("letter");
							double score = Double.parseDouble(rawScore.substring(0, rawScore.indexOf(" ")));
							double total = Double.parseDouble(rawScore.substring(rawScore.indexOf("/") + 2, rawScore.length()));
							if(total <= 0) {
								letter = "A+";
							}
							String cleanedName = StringEscapeUtils.unescapeHtml4(row2.getString("category"));
							String percentage = row2.getString("percent") + "%";
							Assignment asg = new Assignment("Cat", row.getString("course"), cleanedName, 
									"", "", score, total, letter, percentage, iter + 1, row2.getString("weight"));
							assignmentList.add(asg);
						}
					}
				}
			}
		}
		db.deleteAll();
		adb.deleteAll();
		for(Grade g : semesterList1) {
			db.add(g);
		}
		for(Grade g : semesterList2) {
			db.add(g);
		}
		for(Item i : assignmentList) {
			if(!i.isSection()) {
				adb.add((Assignment) i);
			}
		}
	}

	private String downloadUrl(String urlString) throws IOException, SocketTimeoutException {
		String output = "null";
		DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
            		new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, encryptedPassword));
            System.out.println("mE: " + username +", mP: " + encryptedPassword);
 
            HttpGet httpget = new HttpGet("http://kingfi.sh/api/bcpmobile/v1/grades");
 
            System.out.println("executing request" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
 
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                String resp = EntityUtils.toString(entity);
                //System.out.println(resp);
                output = resp;
            }
        } catch(Exception e) {
        	e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return output;
	}
}