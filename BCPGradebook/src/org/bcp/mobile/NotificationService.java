package org.bcp.mobile;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	private static String coursesUpdatedNewline = "";
	private static String coursesUpdatedComma = "";
	private static int coursesCount = 0;
	private static NotificationManager notificationManager;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onCreate() {
		Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        Log.d("notif", "onCreate");
	}
	
	public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        Log.d("notif", "onStart");
        
        String[] courses = {"APUSH", "Physics B AP", "WHAP", "Mandarin Chinese 3 Honors"};
        sendNotification(courses);
        
        stopSelf();
    }
	
	private boolean sendSingleNotification(String newCourse) {
		if(newCourse == null || newCourse.length() == 0) {
			return false;
		}
		String[] courses = {newCourse};
		return sendNotification(courses);
	}
		
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private boolean sendNotification(String[] newCourses) {
        
		if(newCourses == null || newCourses.length == 0) {
			return false;
		}
		
		for(String course : newCourses) {
			coursesCount++;
	        if(coursesCount == 1) {
	        	coursesUpdatedNewline = course;
	        	coursesUpdatedComma = course;
	        }
	        else {
	        	coursesUpdatedNewline += "\n" + course;
	        	coursesUpdatedComma += ", " + course;
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
		coursesUpdatedNewline = "";
		coursesUpdatedComma = "";
		coursesCount = 0;
		if(notificationManager != null) {
			notificationManager.cancel(GRADES_NOTIF_ID);
		}
	}
}
