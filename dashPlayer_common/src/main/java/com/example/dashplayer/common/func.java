package com.example.dashplayer.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class func 
{
	static Activity act;
	public static void bindActivity(Activity act)
	{
		func.act = act;
	}
	
	public static void showMessage(String title,String msg)
	{
		Dialog dlg = new AlertDialog.Builder(act). 
			    setTitle(title). 
			    setMessage(msg).
			    setPositiveButton("嗯。", new OnClickListener()
			    {
					@Override
					public void onClick(DialogInterface Dlg, int arg1) 
					{
						Dlg.dismiss();
					}
			    	
			    }).create(); 
			dlg.show(); 
	}
	
	public static String[] objArrToStrArr(Object[] objectArray){
       String[] strArray = new String[objectArray.length];
       for (int i = 0; i < objectArray.length; i++)
           strArray[i] = new String((String) objectArray[i]);
       return strArray;
    }
	
	public static void createPath(String path) {
	    File file = new File(path);
	    if (!file.exists()) {
	        file.mkdir();
	    }
	}
	
	public static byte[] getBytes(String s)
	{
		char[] ret;
		ret = s.toCharArray();
		/*
		byte[] bRet = new byte[ret.length];
		for(int i=0;i<ret.length;++i)
			bRet[i] = ret[i];
		*/
		return new String(ret).getBytes();
	}
	
	public static FileOutputStream openFile(String s) throws IOException
	{
		int en = s.length();
		while(en>0 && s.charAt(en-1)==0)
			en--;
		s = s.substring(0, en);
		File fi = new File(s);
		if(!fi.exists())
			fi.createNewFile();
		return new FileOutputStream(s);
	}
	
	public static boolean beginWith(byte[] s,byte[] pre)
	{
		if(s.length<pre.length)
			return false;
		for(int i=0;i<pre.length;++i)
		if(s[i]!=pre[i])
			return false;
		return true;
	}
	
	public static int TimeintervalToInt(String s)
	{
    	/*
    	 * TODO TimeInterval To Int的一个转换
    	 */
		int ret = 0;
		int h = 0,m = 0, sec = 0;
		boolean ignore = false;
		for(int i=0;i<s.length();++i)
		{
			char ch = s.charAt(i);
			if( '0'<=ch && ch<='9' && !ignore )
				ret = ret*10+(s.charAt(i)-'0');
			if( ch == '.' )
				ignore = true;
			if( ch == 'M' )
			{
				m = ret;
				ret = 0;
			}
			if( ch == 'S' )
			{
				sec = ret;
				ret = 0;
			}
			if( ch == 'H' )
			{
				h = ret;
				ret = 0;
			}
		}
		return h*3600+m*60+sec;
	}
	
	public static boolean checkMpdAvailable(ArrayList<VideoFragmentsInfo> videoInfo)
	{
		if(videoInfo.size()==0)
			return false;
		return true;
	}
	
	public static Writer openWriteFile( OutputStream fi, Writer fout, String path) throws IOException
	{	
		if( fi != null )	{	fi.close(); fi = null;	}
		if( fout != null )	{	fout.close(); fout = null;	}
		fi = new FileOutputStream(path);
		fout = new OutputStreamWriter(fi);
		return fout;
	}
	
}
