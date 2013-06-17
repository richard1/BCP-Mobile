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
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import bcp.web.bcpgradebook.R;

public class GradeViewActivity extends Activity {
	
	private String url;
	private String gradesUrl;
	private ListView myList;
	ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();
	public static final String COURSE_ID = "bcp.web.bcpgradebook.courseid";
	OnItemClickListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String username = intent.getStringExtra(MainActivity.USERNAME);
		String password = intent.getStringExtra(MainActivity.PASSWORD);
		url = "http://didjem.com/bell_api/login.php?username=" + username + "&password=" + password;
		gradesUrl = "http://didjem.com/bell_api/grades.php?username=" + username;
		setContentView(R.layout.activity_grade_view);
		
		for(int i = 0; i<10; i++) {
			listContent.add("" + (int)(Math.random()*10000));
		}
		
		populateList(R.id.listView1, listContent);
		listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), "Item #: " + position, Toast.LENGTH_SHORT).show();
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
	public void onStart()
	{
		super.onStart();
		/*
		WebView wv = (WebView)findViewById(R.id.webView1);
		wv.loadData("Loading, please wait...", "text/html", null);
		System.out.println(url);
		*/
		new DownloadPasswordTask().execute(url);
		
		populateList(R.id.listView1, listContent);
		adapter.notifyDataSetChanged();
	}
	
	private class DownloadPasswordTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... urls)
		{
			try {
				return loadPasswordFromNetwork(urls[0]);
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
		protected void onPostExecute(String result)
		{
			System.out.println(result);
			gradesUrl += "&password=" + result;
			System.out.println(gradesUrl);
			new DownloadGradesTask().execute(gradesUrl);
		}
	}
	
	private class DownloadGradesTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... urls)
		{
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
		protected void onPostExecute(String result)
		{
			adapter.notifyDataSetChanged();
			setContentView(R.layout.activity_grade_view);
			/*
			WebView wv = (WebView)findViewById(R.id.webView1);
			wv.loadData(result, "text/html", null);
			*/
			
			populateList(R.id.listView1, listContent);
		}
	}
	
	private String loadPasswordFromNetwork(String urlString) throws IOException, JSONException
	{
		InputStream stream = null;
		String encryptPass = "";
		String json = "";
		try
		{
			stream = downloadUrl(urlString);
			json = convertStreamToString(stream);
		} finally {
			if(stream != null)
				stream.close();
		}
		System.out.println(json);
		JSONObject result = new JSONObject(json);
		JSONObject dat = result.getJSONObject("data");
		encryptPass = dat.getString("encryptedPass");
		return encryptPass;
	}
	
	private String loadGradesFromNetwork(String urlString) throws IOException, JSONException
	{
		InputStream stream = null;
		String rawJson = "";
		try
		{
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
	
	private InputStream downloadUrl(String urlString) throws IOException
	{
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
