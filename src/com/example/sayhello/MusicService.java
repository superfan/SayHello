package com.example.sayhello;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import java.util.Timer;  
import java.util.TimerTask;
import android.text.format.Time;
import android.widget.Toast;
import android.provider.Settings;
import android.content.Context;
import android.net.ConnectivityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.content.IntentFilter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

public class MusicService extends Service {
	public class SimpleBinder extends Binder{
		/**
		 * 获取 Service 实例
		 * @return
		 */
		public MusicService getService(){
			return MusicService.this;
		}
		
		public int add(int a, int b){
			return a + b;
		}
	}
	
	public SimpleBinder sBinder = null;
	
	//MediaPlayer对象
	//private MediaPlayer	player = null;
	
	private boolean isStarted = false;
	
	private Timer mTimer = null;  
	
    private TimerTask mTimerTask = null;
    
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
		// 创建 SimpleBinder
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
		 * 我们并不需要为 notification.flags 设置 FLAG_ONGOING_EVENT，因为
		 * 前台服务的 notification.flags 总是默认包含了那个标志位
		 */
        Notification notification = new Notification(R.drawable.ic_launcher, "Foreground Service Started.",
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        notification.setLatestEventInfo(this, "Foreground Service",
        		"Foreground Service Started.", contentIntent);
        
        /*
         * 注意使用  startForeground ，id 为 0 将不会显示 notification
         */
        startForegroundCompat(0, notification);
	}
	
	public IBinder onBind(Intent arg0)
	{
		return sBinder;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		
		//这里可以理解为装载音乐文件
		//if (player == null)
		//{
		//	player = MediaPlayer.create(this, R.raw.test);
		//	
		//	Toast.makeText(this, "create player", Toast.LENGTH_LONG).show();
		//}
		
		//开始播放
		if (!isStarted)
		{
			isStarted = true;
			
			//player.start();
			startTimer();
			
			Toast.makeText(this, "startTimer", Toast.LENGTH_LONG).show();
			
			//registerIt();
		}
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		//开始播放
		if (!isStarted)
		{
			isStarted = true;
			//player.start();
			startTimer();
					
			Toast.makeText(this, "startTimer", Toast.LENGTH_LONG).show();
					
			//registerIt();
		}		
		return START_STICKY;
	}
	

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		//停止音乐-停止Service
		if (isStarted)
		{
			isStarted = false;
			//player.stop();
			stopTimer();
			
			Toast.makeText(this, "stopTimer", Toast.LENGTH_LONG).show();
			//unregisterIt();
		}
		
		stopForegroundCompat(1);
	}
	
	// call by starting or restarting service
	private void startTimer() {
		Time time = new Time();     
        time.setToNow();
        int curYear = time.year;
        int curMonth = time.month;
        int curDay = time.monthDay;
        int curHour = time.hour;
        int curMinute = time.minute;
        int curSec = time.second;
        
		SharedPreferences settings = getSharedPreferences("SayHello", MODE_PRIVATE);	
		int airplaneOnYear = settings.getInt("airplaneOnYear", 2013);
		int airplaneOnMonth = settings.getInt("airplaneOnMonth", 7);
		int airplaneOnDay = settings.getInt("airplaneOnDay", 5);
		int airplaneOnHour = settings.getInt("airplaneOnHour", 0);
		int airplaneOnMinute = settings.getInt("airplaneOnMinute", 0);
		int airplaneOnSec = settings.getInt("airplaneOnSec", 0);
		boolean airplaneOn = settings.getBoolean("airplaneOn", false);
		
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
			Common.writeFileSdcardFile("<2.1>:Start time!!!\n");
		}
		
        int curTime = curHour * 3600 + curMinute * 60 + curSec;
        int airplaneOnTime = airplaneOnHour * 3600 + airplaneOnMinute * 60 + airplaneOnSec;
        int delay = 0;
        int period = 0;
        
		if (airplaneOn) { // tomorrow
			if (curTime <= airplaneOnTime) {
				Common.writeFileSdcardFile("<2.2>:Start time!!!\n");
	        	return;
	        }
	        else {
	        	delay = 24 * 3600 * 1000 - curTime * 1000 + airplaneOnTime * 1000;
	        	period = 24 * 3600 * 1000;
	        	Common.writeFileSdcardFile("<2.3>:Start time!!!\n");
	        }
		}
		else { // today
			if (curTime <= airplaneOnTime) {
	        	delay = (airplaneOnTime - curTime) * 1000;
	        	period = 24 * 3600 * 1000;
	        	Common.writeFileSdcardFile("<2.4>:Start time!!!\n");
	        }
	        else {
	        	if (curTime - airplaneOnTime < 60000) {
	        		delay = 0;
		        	period = 24 * 3600 * 1000;
		        	Common.writeFileSdcardFile("<2.5>:Start time!!!\n");
	        	}
	        	else {
	        		delay = 24 * 3600 * 1000 - curTime * 1000 + airplaneOnTime * 1000;
		        	period = 24 * 3600 * 1000;
		        	Common.writeFileSdcardFile("<2.7>:Start time!!!\n");
	        	}
	        }
		}

		//period = 5 * 60 * 1000;
		
        if (mTimer == null) {  
            mTimer = new Timer();  
        }  
  
        if (mTimerTask == null) {  
            mTimerTask = new TimerTask() {  
                @Override  
                public void run() {
                    boolean isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                    if (!isEnabled) {
                    	Time time = new Time();     
                        time.setToNow();
                        int curYear = time.year;
                        int curMonth = time.month;
                        int curDay = time.monthDay;
                        
                		SharedPreferences settings = getSharedPreferences("SayHello", MODE_PRIVATE);	
                		int airplaneOnYear = settings.getInt("airplaneOnYear", 2013);
                		int airplaneOnMonth = settings.getInt("airplaneOnMonth", 7);
                		int airplaneOnDay = settings.getInt("airplaneOnDay", 5);
                		boolean airplaneOn = settings.getBoolean("airplaneOn", false);
                		
                		// today has been set
                		if (airplaneOn 
                		    && (curYear == airplaneOnYear)
                		    && (curMonth == airplaneOnMonth)
                		    && (curDay == airplaneOnDay)) {
                			Common.writeFileSdcardFile("<3.1>:run!!!\n");
                			return;
                		}
                		
                		// tomorrow needs to update states
                		SharedPreferences.Editor editor;
                		if ((curYear > airplaneOnYear)
                			|| (curMonth > airplaneOnMonth)
                			|| (curDay > airplaneOnDay)) {
                			editor = settings.edit();
                			editor.putInt("airplaneOnYear", curYear);
                			editor.putInt("airplaneOnMonth", curMonth);
                			editor.putInt("airplaneOnDay", curDay);
                			editor.putBoolean("airplaneOn", false);
                			editor.commit();
                			airplaneOn = false;
                			Common.writeFileSdcardFile("<3.2>:run!!!\n");
                		}
                		
                    	if (isWifiConnected()) {
                    		toggleWiFi(false);
                    	}
                    	
                    	if (getMobileDataStatus(MusicService.this))
                    	{
                    		toggleMobileData(MusicService.this, false);
                    	}
                    	
                    	Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);  
                    	Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
                    	i.putExtra("state", true);  
                    	sendBroadcast(i);
                    	
                    	editor = settings.edit();
        				editor.putBoolean("airplaneOn", true);
        				editor.commit();
        				
        				Common.writeFileSdcardFile("<3.3>:run!!!\n");
                    }
                    else {
                    	Common.writeFileSdcardFile("<3.4>:run!!!\n");
                    }
                }  
            };  
        }  
  
        if (mTimer != null && mTimerTask != null ) {
            mTimer.schedule(mTimerTask, delay, period);
            String str = String.format("<2.6>:Start time--delay:[%d(ms)]--period:[%d(ms)]!!!\n", delay, period);
            Common.writeFileSdcardFile(str);
        }
    }  
  
    private void stopTimer() {
        if (mTimer != null) {  
            mTimer.cancel();  
            mTimer = null;  
        }  
  
        if (mTimerTask != null) {  
            mTimerTask.cancel();  
            mTimerTask = null;  
        }
        
        Common.writeFileSdcardFile("<4>:Stop Timer!!!\n");
    }
    
    /** 
     * @return 网络是否连接可用 
     */  
    private boolean isNetworkConnected() {  
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connManager.getActiveNetworkInfo();  
  
        if (networkinfo != null) {  
            return networkinfo.isConnected();  
        }  
  
        return false;  
    }  
  
    /** 
     * @return wifi是否连接可用 
     */  
    private boolean isWifiConnected() {  
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
  
        if (mWifi != null) {  
            return mWifi.isConnected();  
        }  
  
        return false;  
    }
    
    /** 
     * 当wifi不能访问网络时，mobile才会起作用 
     * @return GPRS是否连接可用 
     */  
    private boolean isMobileConnected() {  
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
  
        if (mMobile != null) {  
            return mMobile.isConnected();  
        } 
        
        return false;  
    }  
    
    /** 
     * WIFI网络开关 
     *  
     * @param enabled 
     * @return 设置是否success 
     */  
    private boolean toggleWiFi(boolean enabled) {  
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);  
        return wm.setWifiEnabled(enabled);  
  
    }
  
    private boolean getMobileDataStatus(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Class<?> conMgrClass = null; // ConnectivityManager类
        Field iConMgrField = null; // ConnectivityManager类中的字段
        Object iConMgr = null; // IConnectivityManager类的引用
        Class<?> iConMgrClass = null; // IConnectivityManager类
        Method getMobileDataEnabledMethod = null; // setMobileDataEnabled方法
    
        try {
        	// 取得ConnectivityManager类
        	conMgrClass = Class.forName(conMgr.getClass().getName());
        	// 取得ConnectivityManager类中的对象mService
        	iConMgrField = conMgrClass.getDeclaredField("mService");
        	// 设置mService可访问
        	iConMgrField.setAccessible(true);
        	// 取得mService的实例化类IConnectivityManager
        	iConMgr = iConMgrField.get(conMgr);
	        // 取得IConnectivityManager类
	        iConMgrClass = Class.forName(iConMgr.getClass().getName());
	        // 取得IConnectivityManager类中的getMobileDataEnabled(boolean)方法
	        getMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("getMobileDataEnabled");
	        // 设置getMobileDataEnabled方法可访问
	        getMobileDataEnabledMethod.setAccessible(true);
	        // 调用getMobileDataEnabled方法
	        return (Boolean) getMobileDataEnabledMethod.invoke(iConMgr);
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	       } catch (NoSuchFieldException e) {
	        e.printStackTrace();
	       } catch (SecurityException e) {
	        e.printStackTrace();
	       } catch (NoSuchMethodException e) {
	        e.printStackTrace();
	       } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	       } catch (IllegalAccessException e) {
	        e.printStackTrace();
	       } catch (InvocationTargetException e) {
	                           // TODO Auto-generated catch block
	                           e.printStackTrace();
	    }
        
        return false;
    }
    
    private void toggleMobileData(Context context, boolean enabled) {
    	  ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    	  Class<?> conMgrClass = null; // ConnectivityManager类
    	  Field iConMgrField = null; // ConnectivityManager类中的字段
    	  Object iConMgr = null; // IConnectivityManager类的引用
    	  Class<?> iConMgrClass = null; // IConnectivityManager类
    	  Method setMobileDataEnabledMethod = null; // setMobileDataEnabled方法

    	  try {
	    	   // 取得ConnectivityManager类
	    	   conMgrClass = Class.forName(conMgr.getClass().getName());
	    	   // 取得ConnectivityManager类中的对象mService
	    	   iConMgrField = conMgrClass.getDeclaredField("mService");
	    	   // 设置mService可访问
	    	   iConMgrField.setAccessible(true);
	    	   // 取得mService的实例化类IConnectivityManager
	    	   iConMgr = iConMgrField.get(conMgr);
	    	   // 取得IConnectivityManager类
	    	   iConMgrClass = Class.forName(iConMgr.getClass().getName());
	    	   // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
	    	   setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    	   // 设置setMobileDataEnabled方法可访问
	    	   setMobileDataEnabledMethod.setAccessible(true);
	    	   // 调用setMobileDataEnabled方法
	    	   setMobileDataEnabledMethod.invoke(iConMgr, enabled);
    	  } catch (ClassNotFoundException e) {
    		  e.printStackTrace();
    	  } catch (NoSuchFieldException e) {
    		  e.printStackTrace();
    	  } catch (SecurityException e) {
    		  e.printStackTrace();
    	  } catch (NoSuchMethodException e) {
    		  e.printStackTrace();
    	  } catch (IllegalArgumentException e) {
    		  e.printStackTrace();
    	  } catch (IllegalAccessException e) {
    		  e.printStackTrace();
    	  } catch (InvocationTargetException e) {
    		  e.printStackTrace();
    	  }
    }
    
    /**
	 * 以兼容性方式开始前台服务
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
	 * 以兼容性方式停止前台服务
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
		 *  在 setForeground 之前调用 cancel，因为我们有可能在取消前台服务之后
		 *  的那一瞬间被kill掉。这个时候 notification 便永远不会从通知一栏移除
		 */
		mNM.cancel(id);
		//setForeground(false);
	}
}
