package com.example.sayhello;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import com.example.sayhello.MusicService.SimpleBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlarmManager;
import java.util.Calendar;

public class AlarmService extends Service {
	public class SimpleBinder extends Binder{
		/**
		 * ��ȡ Service ʵ��
		 * @return
		 */
		public AlarmService getService(){
			return AlarmService.this;
		}
		
		public int add(int a, int b) {
			return a + b;
		}
	}
	
	public SimpleBinder sBinder = null;
	
	private boolean isStarted = false;
    
    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

	@Override
	public void onCreate() {
		super.onCreate();
		// ���� SimpleBinder
		if (sBinder == null)
		{
			sBinder = new SimpleBinder();
		}
		
		mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			mStartForeground = MusicService.class.getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = MusicService.class.getMethod("stopForeground", mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			mStartForeground = mStopForeground = null;
		}

		/*
		 * ���ǲ�����ҪΪ notification.flags ���� FLAG_ONGOING_EVENT����Ϊ
		 * ǰ̨����� notification.flags ����Ĭ�ϰ������Ǹ���־λ
		 */
        Notification notification = new Notification(R.drawable.ic_launcher, "Foreground Service Started.",
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        notification.setLatestEventInfo(this, "Foreground Service",
        		"Foreground Service Started.", contentIntent);
        
        /*
         * ע��ʹ��  startForeground ��id Ϊ 0 ��������ʾ notification
         */
        startForegroundCompat(0, notification);
        
        Toast.makeText(this, "create", Toast.LENGTH_LONG).show();
        
        Common.writeFileSdcardFile("<1>:create!!!\n");
	}
	
	public IBinder onBind(Intent arg0)
	{
		return sBinder;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		
		//��ʼ
		if (!isStarted)
		{
			isStarted = true;

			start();
			
			Toast.makeText(this, "start", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		//��ʼ
		if (!isStarted)
		{
			isStarted = true;
			
			start();
			
			Toast.makeText(this, "start", Toast.LENGTH_LONG).show();
		}		
		return START_STICKY;
	}
	

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		//ֹͣ
		if (isStarted)
		{
			isStarted = false;
			
			stop();
			
			Toast.makeText(this, "stop", Toast.LENGTH_LONG).show();
		}
		
		stopForegroundCompat(1);
	}
	
	// call by starting or restarting service
	private void start() {
        // alarm time
		SharedPreferences settings = getSharedPreferences("SayHello", MODE_PRIVATE);	
		int airplaneOnYear = settings.getInt("airplaneOnYear", 2013);
		int airplaneOnMonth = settings.getInt("airplaneOnMonth", 7);
		int airplaneOnDay = settings.getInt("airplaneOnDay", 5);
		int airplaneOnHour = settings.getInt("airplaneOnHour", 0);
		int airplaneOnMinute = settings.getInt("airplaneOnMinute", 0);
		int airplaneOnSec = settings.getInt("airplaneOnSec", 0);
		boolean airplaneOn = settings.getBoolean("airplaneOn", false);
		
		// current time
		Calendar calendar = Calendar.getInstance();
		long currentTimeMillis = System.currentTimeMillis();
		calendar.setTimeInMillis(currentTimeMillis);
		
		int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        int curSec = calendar.get(Calendar.SECOND);
		
		// set alarm time to the calendar	
		calendar.set(Calendar.YEAR, airplaneOnYear);
		calendar.set(Calendar.MONTH, airplaneOnMonth);
		calendar.set(Calendar.DAY_OF_MONTH, airplaneOnDay);
		calendar.set(Calendar.HOUR_OF_DAY, airplaneOnHour);
        calendar.set(Calendar.MINUTE, airplaneOnMinute);
        calendar.set(Calendar.SECOND, 0);
        
        long alarmTimeMillis = calendar.getTimeInMillis();
		
		// reset alarm time
		if ((curYear > airplaneOnYear)
			|| (curMonth > airplaneOnMonth)
			|| (curDay > airplaneOnDay)) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("airplaneOnYear", curYear);
			editor.putInt("airplaneOnMonth", curMonth);
			editor.putInt("airplaneOnDay", curDay);
			editor.putBoolean("airplaneOn", false);
			editor.commit();
			airplaneOn = false;
			Common.writeFileSdcardFile("<2.1>:Start!!!\n");
		}
		
		long triggerAtMillis = 0;
		if (airplaneOn) { // already alarmed
			if (currentTimeMillis <= alarmTimeMillis) {
				Common.writeFileSdcardFile("<2.2>:Start!!!\n");
	        	return;
	        }
	        else {
	        	triggerAtMillis = alarmTimeMillis + 24 * 3600 * 1000;
	        	Common.writeFileSdcardFile("<2.3>:Start!!!\n");
	        }
		}
		else { // need to alarm
			if (currentTimeMillis <= alarmTimeMillis) {
				triggerAtMillis = alarmTimeMillis;
	        	Common.writeFileSdcardFile("<2.4>:Start!!!\n");
	        }
	        else {
	        	if (currentTimeMillis - alarmTimeMillis < 60000) {
	        		triggerAtMillis = currentTimeMillis;
		        	Common.writeFileSdcardFile("<2.5>:Start!!!\n");
	        	}
	        	else {
	        		triggerAtMillis = alarmTimeMillis + 24 * 3600 * 1000;
		        	Common.writeFileSdcardFile("<2.6>:Start!!!\n");
	        	}
	        }
		}
		
		/* ����Intent��PendingIntent��������Ŀ����� */
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0, intent, 0);
        
        /* ��ȡ���ӹ����ʵ�� */
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        /* �������� */
        //am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        /* ���������� */
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, (24 * 3600 * 1000), pendingIntent);
        
        Common.writeFileSdcardFile("<2.7>:Start!!!\n");
    }
  
    private void stop() {
    	Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, intent, 0);
        
        /* ��ȡ���ӹ����ʵ�� */
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        /* ȡ�� */
        am.cancel(pendingIntent);
        
        Common.writeFileSdcardFile("<3>:Stop!!!\n");
    }
    
    /**
	 * �Լ����Է�ʽ��ʼǰ̨����
	 */
	private void startForegroundCompat(int id, Notification n){
		if(mStartForeground != null){
			mStartForegroundArgs[0] = id;
			mStartForegroundArgs[1] = n;
			
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
			return;
		}
		//setForeground(true);
		mNM.notify(id, n);
	}
	
	/**
	 * �Լ����Է�ʽֹͣǰ̨����
	 */
	private void stopForegroundCompat(int id){
		if(mStopForeground != null){
			mStopForegroundArgs[0] = Boolean.TRUE;
			
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return;
		}
		
		/*
		 *  �� setForeground ֮ǰ���� cancel����Ϊ�����п�����ȡ��ǰ̨����֮��
		 *  ����һ˲�䱻kill�������ʱ�� notification ����Զ�����֪ͨһ���Ƴ�
		 */
		mNM.cancel(id);
		//setForeground(false);
	}
}
