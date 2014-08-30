package org.bcp.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, NotificationService.class);
	    context.startService(serviceIntent);
	}
}
