package com.example.sayhello;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.text.format.Time;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TimePicker;
import android.app.TimePickerDialog;


public class MainActivity extends Activity {
	private Button mStart = null;
	private Button mStop = null;
	private Calendar calendar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView myText = (TextView)findViewById(R.id.mytext);
		myText.setText("Hello. I started!");
		
		TimePicker timePicker = (TimePicker)findViewById(R.id.time_picker);
		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		mStart = (Button)findViewById(R.id.start);
		mStart.setEnabled(false);
		mStart.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//¿ªÆôService
	            startService(new Intent(MainActivity.this, AlarmService.class));
			}
		});
		
		mStop = (Button)findViewById(R.id.stop);
		mStop.setEnabled(false);
		mStop.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//Í£Ö¹Service
	            stopService(new Intent(MainActivity.this, AlarmService.class));
			}
		});
		
		calendar = Calendar.getInstance();
		Button setTime = (Button)findViewById(R.id.set_time);
		setTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				calendar.setTimeInMillis(System.currentTimeMillis());
				new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {			
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						// TODO Auto-generated method stub
						//Í£Ö¹Service
			            stopService(new Intent(MainActivity.this, AlarmService.class));
			            
						/////////////////////////////////////////
						int airplaneOnHour = hourOfDay;
						int airplaneOnMinute = minute;
						int airplaneOnSec = 0;
						
						calendar.setTimeInMillis(System.currentTimeMillis());
						
						int curYear = calendar.get(Calendar.YEAR);
				        int curMonth = calendar.get(Calendar.MONTH);
				        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
				        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
				        int curMinute = calendar.get(Calendar.MINUTE);
				        int curSec = calendar.get(Calendar.SECOND);
						
						int curTime = curHour * 3600 + curMinute * 60 + curSec;
						int airplaneOnTime = airplaneOnHour * 3600 + airplaneOnMinute * 60 + airplaneOnSec;
						
						boolean airplaneOn = true;
						if (curTime <= airplaneOnTime) {
							airplaneOn = false;
						}
						else {
							airplaneOn = true;
						}
						/////////////////////////////////////////
						SharedPreferences settings = getSharedPreferences("SayHello", MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putInt("airplaneOnYear", curYear);
						editor.putInt("airplaneOnMonth", curMonth);
						editor.putInt("airplaneOnDay", curDay);
						editor.putInt("airplaneOnHour", airplaneOnHour);
						editor.putInt("airplaneOnMinute", airplaneOnMinute);
						editor.putInt("airplaneOnSec", airplaneOnSec);
						editor.putBoolean("airplaneOn", airplaneOn);
						editor.commit();
						
						mStart.setEnabled(true);
						mStop.setEnabled(true);
						
						String str = String.format("<0>:Set time[%d-%d-%d %d:%d:%d]!!!\n", 
								curYear, curMonth, curDay, airplaneOnHour, airplaneOnMinute, airplaneOnSec);
						Common.writeFileSdcardFile(str);
					}
				}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
