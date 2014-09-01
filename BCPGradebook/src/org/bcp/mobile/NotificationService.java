package org.bcp.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bcp.mobile.lib.DatabaseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationService extends Service {
	
	// GUN = Grades Update Notification
	public static final int GUN_FIFTEEN_MIN = 0;
	public static final int GUN_THIRTY_MIN = 1;
	public static final int GUN_ONE_HOUR = 2;
	public static final int GUN_TWELVE_HOUR = 3;
	public static final int GUN_ONE_DAY = 4;
	public static final int GUN_NEVER = 5;
	
	public static final CharSequence[] GUN_OPTIONS = {
			"Every 15 minutes",
			"Every 30 minutes",
			"Every hour",
			"Every morning and evening",
			"Every noon",
			"Turn off notifications"
	};
	
	private static final int GRADES_NOTIF_ID = 254;
	
	private static HashSet<CourseUpdate> coursesUpdated = new HashSet<CourseUpdate>();
	private static NotificationManager notificationManager;
	private static DecimalFormat decimalFormat = new DecimalFormat("##0.00");
	private static DatabaseHandler db;
	
	private static boolean shouldSendNotification = false;
	
	private static String username;
	private static String encryptedPassword;
	private static String gradesUrl;
	
	private static ArrayList<CourseUpdate> newlyUpdatedCourses = new ArrayList<CourseUpdate>();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onCreate() {
		//Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        Log.d("notif", "onCreate");
        
		username = (username == null) ? getSharedPreferences("username", MODE_PRIVATE).getString("username", "") : username;
		encryptedPassword = (encryptedPassword == null) ? getSharedPreferences("password", MODE_PRIVATE).getString("password", "") : 
			encryptedPassword;
		
		db = new DatabaseHandler(this);
		
		gradesUrl = "http://brycepauken.com/api/3539/grades.php?username=" + username + "&password=" + encryptedPassword;
	}
	
	public void onStart(Intent intent, int startId) {
        //Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        Log.d("notif", "onStart");
                
        shouldSendNotification = false;
        new DownloadGradesTask().execute(gradesUrl);   
    }
		
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private boolean sendNotification(CourseUpdate[] newCourses) {
        
		if(newCourses == null || newCourses.length == 0) {
			return false;
		}
		
		for(CourseUpdate newCourse : newCourses) {
			if(!coursesUpdated.contains(newCourse)) {
				coursesUpdated.add(newCourse);
			}
		}
		
		String coursesUpdatedNewline = "";
		String coursesUpdatedComma = "";
		int coursesCount = 0;
		
		for(CourseUpdate course : coursesUpdated) {
			coursesCount++;
	        if(coursesCount == 1) {
	        	coursesUpdatedNewline = course.getName() + ": " + course.getPercent();
	        	coursesUpdatedComma = course.getName();
	        }
	        else {
	        	coursesUpdatedNewline += "\n" + course.getName() + ": " + course.getPercent();
	        	coursesUpdatedComma += ", " + course.getName();
	        }
		}
        
        Bitmap bcpLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.bell_no_shield);

        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        bcpLargeIcon = Bitmap.createScaledBitmap(bcpLargeIcon, width, height, false); 
        
        Intent notificationIntent = new Intent(this, GradeViewActivity.class);
        
        PendingIntent viewGradesIntent = PendingIntent.getActivity(
    	    this,
    	    0,
    	    notificationIntent,
    	    PendingIntent.FLAG_UPDATE_CURRENT
    	);
        
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder nb;
        Notification notification;
        
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
        	nb = new Notification.Builder(this)
	        .setContentTitle("Grades updated")
	        .setContentText(coursesUpdatedComma)
	        .setStyle(new Notification.BigTextStyle()
	        .bigText(coursesUpdatedNewline))
	        .setSmallIcon(R.drawable.bell_no_shield)
	        .setLargeIcon(bcpLargeIcon)
	        .setContentIntent(viewGradesIntent)
	        .setAutoCancel(true)
	        .setNumber(coursesCount)
	        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        	
        	notification = nb.build();
        }
        else {
        	nb = new Notification.Builder(this)
	        .setContentTitle("Grades updated")
	        .setContentText(coursesUpdatedComma)
	        .setSmallIcon(R.drawable.bell_no_shield)
	        .setLargeIcon(bcpLargeIcon)
	        .setContentIntent(viewGradesIntent)
	        .setAutoCancel(true)
	        .setNumber(coursesCount)
	        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        	notification = nb.getNotification();
        }
    	notificationManager.notify(GRADES_NOTIF_ID, notification);
    	
    	return true;
	}
	
	public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        Log.d("notif", "onDestroy");
    }
	
	public static void resetNotificationInfo() {
		coursesUpdated.clear();
		if(notificationManager != null) {
			notificationManager.cancel(GRADES_NOTIF_ID);
		}
	}
	
	// slightly altered DownloadGradesTask from GradeViewActivity
	// mainly, no UI and no database updating
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
				Log.d("notif", "No internet connection, could not update");
			} else {
				if(result.equals("SUCCESS")) {
					Log.d("notif", "Update success");
				}
				else if(result.equals("STE")) {
					Log.d("notif", "Server time out");
				}
				else if(result.equals("IOE")) {
					Log.d("notif", "Something is down");
				}
				else {
					Log.d("notif", "Unexpected error");
				}
			}
			
			if(shouldSendNotification) {
				sendNotification(newlyUpdatedCourses.toArray(new CourseUpdate[newlyUpdatedCourses.size()]));
			}
			newlyUpdatedCourses.clear();
			stopSelf();
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

		JSONObject result = new JSONObject(rawJson);
		System.out.println("JSON GRES:" + result.toString());
		boolean error = result.has("error");
		if(error) {
			Log.d("notif-update", "Unexpected error");
			return;
		}
		
		for(int iter = 0; iter < 2; iter++)
		{
			HashMap<String, String> percentMap = db.getPercentTitleMap(iter+1);
			System.out.println("MAP: " + percentMap.toString());
	
			JSONArray sem = iter == 0 ? result.getJSONArray("semester1") : result.getJSONArray("semester2");
			for(int i = 0; i < sem.length(); i++) {
				JSONObject row = sem.getJSONObject(i);
				if(!row.getString("course").equals("Homeroom") && row.has("percent") && !row.getString("percent").equals("null")) {
					String percent = row.getString("percent");
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
						newPercent = 69.69; // TODO remove
						
						System.out.println(newPercent + " - " + oldPercent + " = " + (newPercent - oldPercent));
						String percentDiff = (newPercent < oldPercent ? "" : "+") 
								+ decimalFormat.format((newPercent - oldPercent)) + "%";
						
						if(newPercent != oldPercent) {
							shouldSendNotification = true;
							newlyUpdatedCourses.add(new CourseUpdate(courseName, percentDiff));
						}
					}
				}
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
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	private class CourseUpdate {
	
		private String courseName;
		private String percentDiff;
		
		public CourseUpdate(String courseName, String percentDiff) {
			this.courseName = courseName;
			this.percentDiff = percentDiff;
		}
		
		public String getName() {
			return courseName;
		}
		
		public String getPercent() {
			return percentDiff;
		}
		
		public boolean equals(Object o) {
			if(o instanceof CourseUpdate == false) {
				return false;
			}
			if(this == o) {
				return true;
			}
			
			CourseUpdate cu = (CourseUpdate) o;
			return getName().equals(cu.getName()) && getPercent().equals(cu.getPercent());
		}
		
		public int hashCode() {
			return (courseName + percentDiff).hashCode();
		}
	}
}
