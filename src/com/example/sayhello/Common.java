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
			//����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		 	{
				//��ȡSD����Ŀ¼
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetFile = new File(sdCardDir.getCanonicalPath() + FILE_NAME);
				//��ָ���ļ�����    RandomAccessFile����,��һ���������ļ����ƣ��ڶ��������Ƕ�дģʽ
				RandomAccessFile raf = new RandomAccessFile(targetFile , "rw");
				//���ļ���¼ָ���ƶ������
				raf.seek(targetFile.length());
				// ����ļ�����
				
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
