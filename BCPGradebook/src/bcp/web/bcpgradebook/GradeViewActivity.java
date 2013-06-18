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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import bcp.web.bcpgradebook.R;

public class GradeViewActivity extends Activity {
	
	private String gradesUrl;
	private ListView myList;
	ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();
	public static final String COURSE_ID = "bcp.web.bcpgradebook.courseid";
	OnItemClickListener listener;
	ProgressDialog progress;
	public static boolean isRefreshing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("My Courses");
		
		Intent intent = this.getIntent();
		String username = intent.getStringExtra("username");
		String encryptedPassword = intent.getStringExtra("encryptedPassword");
		System.out.println("user: " + username + ", pass: " + encryptedPassword);
		
		gradesUrl = "http://didjem.com/bell_api/grades.php?username=" + username + "&password=" + encryptedPassword;
		setContentView(R.layout.activity_grade_view);
			
		progress = new ProgressDialog(this);
		progress.setTitle("Loading");
		progress.setMessage("Fetching your grades...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		
		populateList(R.id.listView1, listContent);
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
	
	public void populateList(int list, ArrayList<String> content) {
		myList = (ListView)findViewById(list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, content);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener); 
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
		    	Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
		    	isRefreshing = true;
		    	refreshGrades();
		        return true;
		    case R.id.menu_about:
		    	Toast.makeText(getApplicationContext(), "About (doesn't do anything)!", Toast.LENGTH_SHORT).show();
		        return true;
		    case R.id.menu_logout:
		        Toast.makeText(getApplicationContext(), "Logout!", Toast.LENGTH_SHORT).show();
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
	
	@Override
	public void onStart() {
		super.onStart();
		refreshGrades();
	}
	
	public void refreshGrades() {
		new DownloadGradesTask().execute(gradesUrl);		
		populateList(R.id.listView1, listContent);
		adapter.notifyDataSetChanged();
		if(isRefreshing) {
			Toast.makeText(getApplicationContext(), "All grades refreshed!", Toast.LENGTH_SHORT).show();
			isRefreshing = false;
		}
	}
	
	private class DownloadGradesTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			try {
				return loadGradesFromNetwork(urls[0]);
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
			adapter.notifyDataSetChanged();
			setContentView(R.layout.activity_grade_view);			
			populateList(R.id.listView1, listContent);
			progress.dismiss();
		}
	}
	
	private String loadGradesFromNetwork(String urlString) throws IOException, JSONException {
		InputStream stream = null;
		String rawJson = "";
		try {
			stream = downloadUrl(urlString);
			rawJson = convertStreamToString(stream);
		} finally {
			if(stream != null)
				stream.close();
		}
		System.out.println(rawJson);
		JSONObject result = new JSONObject(rawJson);
		int error = result.getInt("error");
		if(error != 0) {
			Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
			switch(error) {
				case 1: case 3: default:
					return "<h3>Failed to retrieve your grades.</h3> <p>The page might be down, please try again later. [" + error + "]</p>";
				case 2:
					return "<h3>Incorrect username or password.</h3>";
			}
		}
		listContent.clear();
		String output = "";
		JSONObject data = result.getJSONObject("data");
		JSONArray sem1 = data.getJSONArray("semester1");
		output += "<h2>Semester 1</h2>";
		for(int i = 0; i < sem1.length(); i++) {
			JSONObject row = sem1.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				String course = row.getString("class") + ": " + row.getString("grade") + " (" + row.getString("percentage") + ")";
				output += course + "<br />";
				listContent.add(course);
			}
		}
		
		output += "<br /><h2>Semester 2</h2>";
		JSONArray sem2 = data.getJSONArray("semester2");
		for(int i = 0; i < sem2.length(); i++) {
			JSONObject row = sem2.getJSONObject(i);
			if(!row.getString("class").equals("Homeroom")) {
				String course = row.getString("class") + ": " + row.getString("grade") + " (" + row.getString("percentage") + ")";
				output += course + "<br />";
				listContent.add(course);
			}
		}
		return output;
	}
	
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
	    conn.setConnectTimeout(15000 /* milliseconds */);
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);
	    // Starts the query
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
}
