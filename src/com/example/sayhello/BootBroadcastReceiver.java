package com.example.sayhello;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	 
	 @Override
	 public void onReceive(Context context, Intent intent) {
		 if (intent.getAction().equals(ACTION)){
			 //Intent sayHelloIntent = new Intent(context, MainActivity.class);
			 //sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			 //context.startActivity(sayHelloIntent);
			 
			 context.startService(new Intent("com.example.sayhello.Alarm"));
			 Toast.makeText(context, "boot completed actiont", Toast.LENGTH_LONG).show();
			 
			 Common.writeFileSdcardFile("<5>:Broadcast receiver!!!\n");
		 }
	}
}
