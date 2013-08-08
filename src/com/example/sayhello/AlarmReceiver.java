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
		Toast.makeText(context, "�����õ�����ʱ�䵽��!!!!", Toast.LENGTH_LONG).show();
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
     * @return �����Ƿ����ӿ��� 
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
     * @return wifi�Ƿ����ӿ��� 
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
     * ��wifi���ܷ�������ʱ��mobile�Ż������� 
     * @return GPRS�Ƿ����ӿ��� 
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
     * WIFI���翪�� 
     *  
     * @param enabled 
     * @return �����Ƿ�success 
     */  
    private boolean toggleWiFi(Context context, boolean enabled) {  
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);  
        return wm.setWifiEnabled(enabled);
    }
  
    private boolean getMobileDataStatus(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Class<?> conMgrClass = null; // ConnectivityManager��
        Field iConMgrField = null; // ConnectivityManager���е��ֶ�
        Object iConMgr = null; // IConnectivityManager�������
        Class<?> iConMgrClass = null; // IConnectivityManager��
        Method getMobileDataEnabledMethod = null; // setMobileDataEnabled����
    
        try {
        	// ȡ��ConnectivityManager��
        	conMgrClass = Class.forName(conMgr.getClass().getName());
        	// ȡ��ConnectivityManager���еĶ���mService
        	iConMgrField = conMgrClass.getDeclaredField("mService");
        	// ����mService�ɷ���
        	iConMgrField.setAccessible(true);
        	// ȡ��mService��ʵ������IConnectivityManager
        	iConMgr = iConMgrField.get(conMgr);
	        // ȡ��IConnectivityManager��
	        iConMgrClass = Class.forName(iConMgr.getClass().getName());
	        // ȡ��IConnectivityManager���е�getMobileDataEnabled(boolean)����
	        getMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("getMobileDataEnabled");
	        // ����getMobileDataEnabled�����ɷ���
	        getMobileDataEnabledMethod.setAccessible(true);
	        // ����getMobileDataEnabled����
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

  	  Class<?> conMgrClass = null; // ConnectivityManager��
  	  Field iConMgrField = null; // ConnectivityManager���е��ֶ�
  	  Object iConMgr = null; // IConnectivityManager�������
  	  Class<?> iConMgrClass = null; // IConnectivityManager��
  	  Method setMobileDataEnabledMethod = null; // setMobileDataEnabled����

  	  try {
	    	   // ȡ��ConnectivityManager��
	    	   conMgrClass = Class.forName(conMgr.getClass().getName());
	    	   // ȡ��ConnectivityManager���еĶ���mService
	    	   iConMgrField = conMgrClass.getDeclaredField("mService");
	    	   // ����mService�ɷ���
	    	   iConMgrField.setAccessible(true);
	    	   // ȡ��mService��ʵ������IConnectivityManager
	    	   iConMgr = iConMgrField.get(conMgr);
	    	   // ȡ��IConnectivityManager��
	    	   iConMgrClass = Class.forName(iConMgr.getClass().getName());
	    	   // ȡ��IConnectivityManager���е�setMobileDataEnabled(boolean)����
	    	   setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    	   // ����setMobileDataEnabled�����ɷ���
	    	   setMobileDataEnabledMethod.setAccessible(true);
	    	   // ����setMobileDataEnabled����
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
