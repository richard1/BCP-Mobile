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

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.webkit.WebView;

public class GradeViewActivity extends Activity {
	
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String username = intent.getStringExtra(MainActivity.USERNAME);
		String password = intent.getStringExtra(MainActivity.PASSWORD);
		url = "https://brycepauken.com/api/3541/login.php?username=" + username + "&password=" + password;
		setContentView(R.layout.activity_grade_view);
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
		new DownloadPasswordTask().execute(url);
	}
	
	private class DownloadPasswordTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... urls)
		{
			try {
				return loadPasswordFromNetwork(urls[0]);
			} catch(IOException e) {
				return getResources().getString(R.string.connection_error);
			} catch(JSONException e) {
				return getResources().getString(R.string.json_error) + e.toString();
			}
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			setContentView(R.layout.activity_main);
			WebView wv = (WebView)findViewById(R.id.webView1);
			wv.loadData(result, "text/html", null);
		}
	}
	
	private String loadPasswordFromNetwork(String urlString) throws IOException, JSONException
	{
		InputStream stream = null;
		String json = "";
		try
		{
			stream = downloadUrl(urlString);
			json = convertStreamToString(stream);
		} finally {
			if(stream != null)
				stream.close();
		}
		JSONObject result = new JSONObject(json);
		JSONObject dat = result.getJSONObject("data");
		String encryptPass = dat.getString("encryptedPass");
		return encryptPass;
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
