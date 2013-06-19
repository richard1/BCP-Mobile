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
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import bcp.web.bcpgradebook.R;
import bcp.web.bcpgradebook.lib.Grade;
import bcp.web.bcpgradebook.lib.GradeAdapter;

public class GradeViewActivity extends ListActivity {
	
	private String gradesUrl;
	private PullToRefreshListView myList;
	GradeAdapter adapter;
	private ArrayList<Grade> listContent = new ArrayList<Grade>();
	private ArrayList<Grade> tempContent = new ArrayList<Grade>();
	public static final String COURSE_ID = "bcp.web.bcpgradebook.courseid";
	OnItemClickListener listener;
	ProgressDialog progress;
	public boolean isRefreshing = false;
	PullToRefreshListView pullToRefreshView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grade_view);
		
		//listContent.add(new Grade(R.drawable.grade_bplus, "ehhh", "hello"));
		//listContent.add(new Grade(R.drawable.grade_dplus, "merr", "ewqe"));
		
		pullToRefreshView = (PullToRefreshListView) findViewById(R.id.listView1);
		pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
		    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Toast.makeText(getApplicationContext(), "on refresh", Toast.LENGTH_SHORT).show();
				new DownloadGradesTask().execute(gradesUrl);
		    }
		});
		
		myList = (PullToRefreshListView)findViewById(R.id.listView1);
		adapter = new GradeAdapter(this, R.layout.grade_item_row, listContent);
		myList.setAdapter(adapter);
		setListAdapter(adapter);
		myList.setOnItemClickListener(listener);
		
		setTitleColor(Color.WHITE); // actually doesn't work
		setTitle("My Courses");
		
		Intent intent = this.getIntent();
		String username = intent.getStringExtra("username");
		String encryptedPassword = intent.getStringExtra("encryptedPassword");
		System.out.println("user: " + username + ", pass: " + encryptedPassword);
		
		gradesUrl = "http://didjem.com/bell_api/grades.php?username=" + username + "&password=" + encryptedPassword;
			
		progress = new ProgressDialog(this);
		progress.setTitle("Welcome");
		progress.setMessage("Fetching your grades...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		
		listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), "Item #: " + position, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getBaseContext(), CourseDetailActivity.class);
				intent.putExtra(COURSE_ID, "Item #: " + position);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		};
	}
	
	@Override
	public void onStart() {
		super.onStart();
		new DownloadGradesTask().execute(gradesUrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_grade_view, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_refresh:
		    	Toast.makeText(getApplicationContext(), "Deprecated :(", Toast.LENGTH_SHORT).show();
		        return true;
		    case R.id.menu_about:
		    	Toast.makeText(getApplicationContext(), "About (doesn't do anything)!", Toast.LENGTH_SHORT).show();
		        return true;
		    case R.id.menu_logout:
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
	
	private class DownloadGradesTask extends AsyncTask<String, Void, String> {
		ArrayList<Grade> newData;
		@Override
		protected String doInBackground(String... urls) {
			try {
				newData = loadGradesFromNetwork(urls[0]);
				return "Success";
			} catch(IOException e) {
				e.printStackTrace();
				return getResources().getString(R.string.connection_error);
			} catch(JSONException e) {
				e.printStackTrace();
				return getResources().getString(R.string.json_error) + e.toString();
			} catch(Exception e) {
				return "Unknown Exception!: " + e.toString();
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			myList = (PullToRefreshListView) findViewById(R.id.listView1);
			listContent.clear();
			listContent.addAll(newData);
			myList = (PullToRefreshListView)findViewById(R.id.listView1);
			adapter = new GradeAdapter(GradeViewActivity.this, R.layout.grade_item_row, listContent);
			myList.setAdapter(adapter);
			setListAdapter(adapter);
			myList.setOnItemClickListener(listener);
			Toast.makeText(getApplicationContext(), "Up to date.", Toast.LENGTH_SHORT).show();
			progress.dismiss();
			((PullToRefreshListView) findViewById(R.id.listView1)).onRefreshComplete();
            super.onPostExecute(result);
		}
	}
	
	private ArrayList<Grade> loadGradesFromNetwork(String urlString) throws IOException, JSONException {
		InputStream stream = null;
		String rawJson = "";
		try {
			stream = downloadUrl(urlString);
			rawJson = convertStreamToString(stream);
		} finally {
			if(stream != null)
				stream.close();
		}
		tempContent.clear();
		JSONObject result = new JSONObject(rawJson);
		int error = result.getInt("error");
		if(error != 0) {
			Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
			return null;
		}
		JSONObject data = result.getJSONObject("data");
		JSONArray sem1 = data.getJSONArray("semester1");
		for(int i = 0; i < sem1.length(); i++) {
			JSONObject row = sem1.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				tempContent.add(new Grade(getIdFromGrade(row.getString("grade")), row.getString("class"), row.getString("percentage")));
			}
		}
		
		JSONArray sem2 = data.getJSONArray("semester2");
		for(int i = 0; i < sem2.length(); i++) {
			JSONObject row = sem2.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				tempContent.add(new Grade(getIdFromGrade(row.getString("grade")), row.getString("class"), row.getString("percentage")));
			}
		}
		return tempContent;
	}
	
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
	    conn.setConnectTimeout(15000 /* milliseconds */);
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);
	    conn.connect();
	    return conn.getInputStream();
	}
	
	private static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
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
}
