package bcp.web.bcpgradebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;
import bcp.web.bcpgradebook.lib.DatabaseHandler;
import bcp.web.bcpgradebook.lib.Grade;
import bcp.web.bcpgradebook.lib.GradeAdapter;

import com.actionbarsherlock.app.ActionBar;
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

	private String gradesUrl;
	private ArrayList<Grade> semesterList1 = new ArrayList<Grade>();
	private ArrayList<Grade> semesterList2 = new ArrayList<Grade>();
	public final int SEMESTER_ONE_POSITION = 0;
	public final int SEMESTER_TWO_POSITION = 1;
	public static final String COURSE_ID = "bcp.web.bcpgradebook.courseid";

	ProgressDialog progress;
	DatabaseHandler db;
	DecimalFormat decimalFormat = new DecimalFormat("##0.00");
	ViewPager mViewPager;
	TitlePageIndicator mIndicator;
	GradePagerAdapter mAdapter;
	SlidingMenu sm;

	HttpURLConnection conn;

	GradeFragment semesterOneFragment;
	GradeFragment semesterTwoFragment;
	
	MenuListFragment mFrag;
	Fragment mContent;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main); // was activity_grade_view
		
		//getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		//getSupportActionBar().setCustomView(R.layout.action_bar);
		
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
		
		
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		
		setContentView(R.layout.main);
		//getSupportFragmentManager().beginTransaction().add(R.id.main, (Fragment) mContent, "main");
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

		setTitle("Grades");

		semesterList1.addAll((ArrayList<Grade>) db.getAllWithSemester(1));
		semesterList2.addAll((ArrayList<Grade>) db.getAllWithSemester(2));

		progress = new ProgressDialog(this);
		progress.setTitle("Welcome");
		progress.setMessage("Fetching your grades...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		Calendar c = Calendar.getInstance(); 
		int month = c.get(Calendar.MONTH) + 1; // January -> 0, December -> 11 ... this corrects it		
		if(month < 8) { // if in between January 1 and July 31, set default to Semester 2
			mIndicator.setCurrentItem(mAdapter.getCount() - 1);
		}

		Intent intent = this.getIntent();
		String username = intent.getStringExtra("username");
		String encryptedPassword = intent.getStringExtra("encryptedPassword");

		gradesUrl = "http://didjem.com/bell_api/grades.php?username=" + username + "&password=" + encryptedPassword;

		if(!isOnline()) {
			progress.dismiss();
		} else {
			displayCrouton("CONNECTED", 3000, Style.CONFIRM);
		}

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
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_grade_view, menu);
		return true;
	}*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add("Search").setIcon(R.drawable.world).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case android.R.id.home:
				toggle();
				return true;
		    case R.id.menu_about:	    	
		    	String about = "By Richard Lin '13\n\nWith help from Jonathan Chang '13, Bryce Pauken '14\n\n" +
		    			"Based on Bryce's BCP Mobile app for iOS, this app was created to provide Android-loving " +
		    			"Bellarmine students a convenient way to check their grades, view announcements, and more.\n\n" +
		    			"If you're enjoying this app, please share this with your friends!";
		        AlertDialog.Builder builder = new AlertDialog.Builder(GradeViewActivity.this);
		        builder.setTitle("About");
		        builder.setMessage(about);
		        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            }
		        });
		        builder.setNegativeButton("View on GitHub", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/richard1/"));
		            	startActivity(browserIntent);
		            }
		        });
		        builder.show();
		        return true;
		    case R.id.menu_logout:
		    	db.deleteAll();
		        getSharedPreferences("username", MODE_PRIVATE).edit().clear().commit();
		        getSharedPreferences("password", MODE_PRIVATE).edit().clear().commit();
		        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		        startActivity(intent);
		        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		        return true;
		    case R.id.menu_settings:
		    	Toast.makeText(getApplicationContext(), "Settings (doesn't do anything)!", Toast.LENGTH_SHORT).show();
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
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
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
		return R.drawable.grade_f;
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
					Toast.makeText(getApplicationContext(), "Item #: " + position, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getBaseContext(), CourseDetailActivity.class);
					intent.putExtra(COURSE_ID, "Item #: " + position);
					intent.putExtra("title", title);
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
			listView = (PullToRefreshListView) getView().findViewById(R.id.listView1);
			adapter = new GradeAdapter(getActivity(), R.layout.grade_item_row, isSemesterOne ? semesterList1 : semesterList2); // was mainlist
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(listener);
		}
	}

	private class GradePagerAdapter extends FragmentStatePagerAdapter {
		public GradePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			if (PageInfo.Crouton.ordinal() == position) {
				return semesterOneFragment;
			} else if (PageInfo.About.ordinal() == position) {
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
				return "Success";
			} catch(IOException e) {
				e.printStackTrace();
				return getResources().getString(R.string.connection_error);
			} catch(JSONException e) {
				e.printStackTrace();
				return getResources().getString(R.string.json_error) + e.toString();
			} catch(Exception e) {
				System.out.println("ATTENTION\nERROR\nHEREITIS\n"+e.toString());
				e.printStackTrace();
				return "Unknown Exception!: " + e.toString();
			}
		}

		@Override
		protected void onPostExecute(String result) {

			if(!isOnline()) {
				displayCrouton("NO INTERNET CONNECTION", 3000, Style.ALERT);
			} else {
				semesterOneFragment.refreshList();
				semesterTwoFragment.refreshList();
				displayCrouton("UPDATED", 1000, Style.INFO);
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
			stream = downloadUrl(urlString);
			rawJson = convertStreamToString(stream);
		} finally {
			if(stream != null)
				stream.close();
		}
		semesterList1.clear();
		semesterList2.clear();
		JSONObject result = new JSONObject(rawJson);
		int error = result.getInt("error");
		if(error != 0) {
			displayCrouton("AN ERROR OCCURRED, PLEASE TRY AGAIN LATER [" + error + "]", 3000, Style.ALERT);
			return;
		}

		HashMap<String, String> percentMap = db.getPercentTitleMap(1);
		System.out.println("MAP: " + percentMap.toString()); // TODO: check if key exists

		JSONObject data = result.getJSONObject("data");
		JSONArray sem1 = data.getJSONArray("semester1");
		for(int i = 0; i < sem1.length(); i++) {
			JSONObject row = sem1.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				String percent = row.getString("percentage");
				String extraText = "";
				if(!percentMap.isEmpty()) {
					String courseName = row.getString("class");
					System.out.println("looking for: " + courseName + "\n" + percentMap.get(courseName));
					double oldPercent = Double.parseDouble(percentMap.get(courseName).replaceAll("%", ""));
					double newPercent = Double.parseDouble(percent.replaceAll("%", ""));
					System.out.println(newPercent + " - " + oldPercent + " = " + (newPercent - oldPercent));
					extraText += "   " + (newPercent < oldPercent ? "" : "+") 
							+ decimalFormat.format((newPercent - oldPercent)) + "%";
				}

				Grade grade = new Grade(getIdFromGrade(row.getString("grade")), row.getString("class"), percent, 1);
				grade.addExtraText(extraText);
				semesterList1.add(grade);
			}
		}

		percentMap = db.getPercentTitleMap(2);
		System.out.println("MAP: " + percentMap.toString());

		JSONArray sem2 = data.getJSONArray("semester2");
		for(int i = 0; i < sem2.length(); i++) {
			JSONObject row = sem2.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				String percent = row.getString("percentage");
				String extraText = "";
				if(!percentMap.isEmpty()) {
					String courseName = row.getString("class");
					System.out.println("looking for: " + courseName + "\n" + percentMap.get(courseName));
					double oldPercent = Double.parseDouble(percentMap.get(courseName).replaceAll("%", ""));
					double newPercent = Double.parseDouble(percent.replaceAll("%", ""));
					System.out.println(newPercent + " - " + oldPercent + " = " + (newPercent - oldPercent));
					extraText += "   " + (newPercent < oldPercent ? "" : "+") 
							+ decimalFormat.format((newPercent - oldPercent)) + "%";
				}

				Grade grade = new Grade(getIdFromGrade(row.getString("grade")), row.getString("class"), percent, 2);
				grade.addExtraText(extraText);
				semesterList2.add(grade);
			}
		}
		db.deleteAll();
		for(Grade g : semesterList1) {
			db.add(g);
		}
		for(Grade g : semesterList2) {
			db.add(g);
		}
	}

	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		InputStream stream = conn.getInputStream(); // TODO: use better server, this thing takes 5 seconds :(
		return stream;
	}

	private String convertStreamToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
				conn.disconnect();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}