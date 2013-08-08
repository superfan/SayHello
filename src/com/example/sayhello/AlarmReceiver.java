package com.example.sayhello;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.format.Time;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	 public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "你设置的闹钟时间到了!!!!", Toast.LENGTH_LONG).show();
		Common.writeFileSdcardFile("<6.1>:AlarmReceiver!!!\n");

		boolean isEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        if (!isEnabled) {
        	Calendar calendar = Calendar.getInstance();
    		calendar.setTimeInMillis(System.currentTimeMillis());
    		
    		int curYear = calendar.get(Calendar.YEAR);
            int curMonth = calendar.get(Calendar.MONTH);
            int curDay = calendar.get(Calendar.DAY_OF_MONTH);
            //int curHour = calendar.get(Calendar.HOUR_OF_DAY);
            //int curMinute = calendar.get(Calendar.MINUTE);
            //int curSec = calendar.get(Calendar.SECOND);
            
    		SharedPreferences settings = context.getSharedPreferences("SayHello", context.MODE_PRIVATE);	
    		int airplaneOnYear = settings.getInt("airplaneOnYear", 2013);
    		int airplaneOnMonth = settings.getInt("airplaneOnMonth", 7);
    		int airplaneOnDay = settings.getInt("airplaneOnDay", 5);
    		boolean airplaneOn = settings.getBoolean("airplaneOn", false);
    		
    		// today has been set
    		if (airplaneOn 
    		    && (curYear == airplaneOnYear)
    		    && (curMonth == airplaneOnMonth)
    		    && (curDay == airplaneOnDay)) {
    			Common.writeFileSdcardFile("<6.2>:AlarmReceiver!!!\n");
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
    			Common.writeFileSdcardFile("<6.3>:AlarmReceiver!!!\n");
    		}
    		
        	if (isWifiConnected(context)) {
        		toggleWiFi(context, false);
        	}
        	
        	if (getMobileDataStatus(context))
        	{
        		toggleMobileData(context, false);
        	}
        	
        	Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);  
        	Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
        	i.putExtra("state", true);  
        	context.sendBroadcast(i);
        	
        	editor = settings.edit();
			editor.putBoolean("airplaneOn", true);
			editor.commit();
			
			Common.writeFileSdcardFile("<6.4>:AlarmReceiver!!!\n");
        }
        else {
        	Common.writeFileSdcardFile("<6.5>:AlarmReceiver!!!\n");
        }
	}
	
	/** 
     * @return 网络是否连接可用 
     */  
    private boolean isNetworkConnected(Context context) {  
    	ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connManager.getActiveNetworkInfo();  
  
        if (networkinfo != null) {  
            return networkinfo.isConnected();  
        }  
  
        return false;  
    }  
  
    /** 
     * @return wifi是否连接可用 
     */  
    private boolean isWifiConnected(Context context) {  
    	ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    private boolean isMobileConnected(Context context) {  
    	ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    private boolean toggleWiFi(Context context, boolean enabled) {  
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);  
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
  	  ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
}
