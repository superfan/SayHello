package com.example.sayhello;

import java.io.File;
import java.io.RandomAccessFile;
import android.os.Environment;
import android.text.format.Time;

import java.io.FileOutputStream;
import java.io.IOException;


public class Common {
	private static String FILE_NAME = "/sayhello123.txt";
	
	public static void writeFileSdcardFile(String str) { 
		try
		{    
			//如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		 	{
				//获取SD卡的目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetFile = new File(sdCardDir.getCanonicalPath() + FILE_NAME);
				//以指定文件创建    RandomAccessFile对象,第一个参数是文件名称，第二个参数是读写模式
				RandomAccessFile raf = new RandomAccessFile(targetFile , "rw");
				//将文件记录指针移动到最后
				raf.seek(targetFile.length());
				// 输出文件内容
				
				Time time = new Time();     
		        time.setToNow();		        
		        String tmpStr = String.format("%d-%d-%d %d:%d:%d %s", 
		        					time.year, time.month + 1, time.monthDay, time.hour, time.minute, time.second, str);
				
				raf.writeUTF(tmpStr);
				raf.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	} 
}
