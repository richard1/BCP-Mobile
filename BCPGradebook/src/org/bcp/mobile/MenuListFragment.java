package org.bcp.mobile;

import java.util.Calendar;

import org.bcp.mobile.lib.DatabaseHandler;

import org.bcp.mobile.R;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MenuListFragment extends ListFragment {
	
	private SidebarMenuAdapter adapter;
	private Toast toast;
	private int easterEgg = 0;
	private static AlertDialog.Builder builder;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new SidebarMenuAdapter(getActivity());
		SharedPreferences userPref = getActivity().getSharedPreferences("username", Context.MODE_PRIVATE);
		String savedUsername = userPref.getString("username", "");
		String[] userInfo = getNameFromUsername(savedUsername);
		adapter.add(new SampleItem(userInfo[0] + " " + userInfo[1] + " '" + userInfo[2], R.drawable.bell));
		adapter.add(new SampleItem("Grades", R.drawable.book));
		adapter.add(new SampleItem("Announcements", R.drawable.speaker));
		adapter.add(new SampleItem("Calendar", R.drawable.calendar));
		adapter.add(new SampleItem("News", R.drawable.world));
		adapter.add(new SampleItem("About", R.drawable.info));
		adapter.add(new SampleItem("Contact Us", R.drawable.mail));
		adapter.add(new SampleItem("Rate", R.drawable.heart));
		adapter.add(new SampleItem("Settings", R.drawable.gear));
		adapter.add(new SampleItem("Log Out", R.drawable.directional_left));
		setListAdapter(adapter);
	}
	
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent;
        String about;
        
        SharedPreferences frequencyPref = getActivity().getSharedPreferences("frequency", Context.MODE_PRIVATE);
        
		switch (position) {
			case 0:
				easterEgg++;
				if(easterEgg >= 7) { // tap name 7 times for easter egg.
					displayToast();
					easterEgg = 0;
				}
				break;
			case 1: // Grades
				intent = new Intent(getActivity(), GradeViewActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case 2: // Announcements
				intent = new Intent(getActivity(), AnnouncementsActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case 3: // Calendar
				intent = new Intent(getActivity(), CalendarActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case 4: // News
				intent = new Intent(getActivity(), NewsRssActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case 5: // About
	        	about = "Made with ♥ by Richard Lin '13, with help from Jonathan Chang '13\n\nInspired by Bryce Pauken '14\n\n" +
		    			"Based on Bryce's BCP Mobile app for iOS, this app was created to provide Android-loving " +
		    			"Bellarmine students a convenient way to check their grades, view announcements, and more.\n\n" +
		    			"If you're enjoying this app, please share this with your friends!";
		        builder = new AlertDialog.Builder(getActivity());
		        builder.setTitle("BCP Mobile v1.3");
		        builder.setMessage(about);
		        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            }
		        });
		        builder.setNegativeButton("View on GitHub", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/richard1/BCP-Mobile"));
		            	startActivity(browserIntent);
		            }
		        });
		        builder.show();
		        break;
			case 6:
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
						"me@richardgl.in", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BCP-Mobile Feedback");
				startActivity(Intent.createChooser(emailIntent, "Send email..."));
				break;
			case 7: // Rate
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
            			Uri.parse("https://play.google.com/store/apps/details?id=org.bcp.mobile"));
            	startActivity(browserIntent);
				break;
			case 8: // Settings
				AlertDialog levelDialog;
				int currentFrequency = frequencyPref.getInt("frequency", NotificationService.GUN_TWELVE_HOUR);
				
                // Creating and Building the Dialog 
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Grades Check Frequency");
                builder.setSingleChoiceItems(NotificationService.GUN_OPTIONS, currentFrequency, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int item) {
	                	SharedPreferences frequencyPref = getActivity().getSharedPreferences("frequency", Context.MODE_PRIVATE);
	                    switch(item) {
	                        case NotificationService.GUN_FIFTEEN_MIN:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_FIFTEEN_MIN).apply();
	                        	Toast.makeText(getActivity(), "Yes sir!  We'll check every 15 minutes.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        case NotificationService.GUN_THIRTY_MIN:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_THIRTY_MIN).apply();
	                        	Toast.makeText(getActivity(), "Aye, captain!  We'll check every 30 minutes.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        case NotificationService.GUN_ONE_HOUR:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_ONE_HOUR).apply();
	                        	Toast.makeText(getActivity(), "Roger that!  We'll check every hour.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        case NotificationService.GUN_TWELVE_HOUR:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_TWELVE_HOUR).apply();
	                        	Toast.makeText(getActivity(), "Got it!  We'll check every morning and evening.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        case NotificationService.GUN_ONE_DAY:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_ONE_DAY).apply();
	                        	Toast.makeText(getActivity(), "Gotcha!  We'll check every day at noon.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        case NotificationService.GUN_NEVER:
	                        	frequencyPref.edit().putInt("frequency", NotificationService.GUN_NEVER).apply();
	                        	Toast.makeText(getActivity(), "Affirmative!  We won't automatically check for grade updates.", Toast.LENGTH_SHORT).show();
	                        	break;
	                        default:
	                        	break;
	                    }
	                    dialog.dismiss();
	                    setNotificationAlarm();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();
				break;
	        case 9: // Log out
	    		new DatabaseHandler(getActivity()).deleteAll();
	    		
	    		frequencyPref.edit().putInt("frequency", NotificationService.GUN_NEVER).apply();
	    		setNotificationAlarm();
	    		getActivity().getSharedPreferences("hasLoggedIn", Context.MODE_PRIVATE).edit().putInt("hasLoggedIn", 0).apply();
	    		
	    		getActivity().getSharedPreferences("username", Context.MODE_PRIVATE).edit().clear().commit();
	    		getActivity().getSharedPreferences("password", Context.MODE_PRIVATE).edit().clear().commit();
		        intent = new Intent(getActivity(), LoginActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		        startActivity(intent);
		        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		        break;
		}
    }

	private class SampleItem {
		public String tag;
		public int iconRes;
		public SampleItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	public class SidebarMenuAdapter extends ArrayAdapter<SampleItem> {

		public SidebarMenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

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
	
	public void displayToast() {
		Toast ImageToast = new Toast(getActivity());
        LinearLayout toastLayout = new LinearLayout(getActivity());
        toastLayout.setOrientation(LinearLayout.HORIZONTAL);
        ImageView image = new ImageView(getActivity());
        image.setImageResource(R.drawable.bell_shield);
        image.setLayoutParams(new TableRow.LayoutParams(254, 254)); // :)
        toastLayout.addView(image);
        ImageToast.setView(toastLayout);
        ImageToast.setDuration(Toast.LENGTH_SHORT);
        ImageToast.setGravity(Gravity.CENTER, 0, 0);
        ImageToast.show();
	    if(toast != null) {
	        toast.cancel();
	    }
	    toast = ImageToast;
	    toast.show();
	}
	
	public void onPause() {
	    if(toast != null) {
	        toast.cancel();
	    }
	    super.onPause();
	}
	
	private boolean setNotificationAlarm() {
		SharedPreferences frequencyPref = getActivity().getSharedPreferences("frequency", Context.MODE_PRIVATE);
		int currentFrequency = frequencyPref.getInt("frequency", NotificationService.GUN_TWELVE_HOUR);
		
		NotificationService.resetNotificationInfo();
		
		try {
			Intent broadcastIntent = new Intent(getActivity(), NotificationReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), GradeViewActivity.NOTIF_BROADCAST_ID,
					broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarms = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
			
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
						
			//Toast.makeText(getActivity(), new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(calendar.getTime()), Toast.LENGTH_LONG).show();
			
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
}