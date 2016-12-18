package com.example.dashplayer.common.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.func;

/*
 * 这个模块用来控制通过http进行文件传输的过程
 */

public class HttpDownloadModule {

	static int speedEstimatePackNum = 20;	// 估计速度的时候考虑的包数
	
	class DownloadPackInfo
	{
		// 用来记录每一个下载的数据包大小、到达时间
		int dat;	//可用来记录数据包的前缀和
		long enTime;	//完成的时间
		long stTime;	//开始的时间
		public DownloadPackInfo(long stTime)
		{
			this.stTime = stTime;
		}
	}
	
	int status = 0;
	public int downCount = 0;
	long stTime = 0;
	long enTime = 0;
	ArrayDeque<DownloadPackInfo> q = new ArrayDeque<DownloadPackInfo>();
	public int tag = -1;
	Thread thd;
	int intervalDat = 0;
	Timer tim = new Timer();
	
	public void setTag(int p)
	{
		tag = p;
	}
	
	public void stop()
	{
		enTime = System.currentTimeMillis();
		status = 0;
	}
	
	public void pause()
	{
		try {
			if(thd.isAlive())
				thd.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resume()
	{
		thd.resume();
	}
	
	public int getTotSpeed()
	{
		long now = System.currentTimeMillis();
		if(status==0)	// still working
			now = enTime;
		return (int)(downCount*1000/(now-stTime));
	}
	
	int spdLimTim = -1;
	int nowSpdLimTim = -1;
	
	public void setSpeedLim(int tim)
	{
		spdLimTim = tim;
		nowSpdLimTim = tim;
	}
	
	public int getNowSpeed()
	{ 
		long now = System.currentTimeMillis();
		if(status == 0)
			now = enTime;
		long sttime = stTime;
		int cnt = 0;
		ArrayDeque<DownloadPackInfo> tmp = q.clone();
		Iterator<DownloadPackInfo> i = tmp.descendingIterator();
		for(int j=0;j<speedEstimatePackNum&&i.hasNext();++j)
		{
			DownloadPackInfo p = i.next();
			cnt += p.dat;
			sttime = p.stTime;
		}
		return (int)(cnt*1000/(Math.max(1,now-sttime)));
	}
	
	public int getIntervalDat()
	{
		int tmp = intervalDat;
		intervalDat = 0;
		return tmp;
	}
	
	Random rand = new Random();
	
	public void downFile(final String url, final String fileName, final OnEventListener evt,boolean syn) 
	{
		status = 1;
		final InfoPack ret = new InfoPack();
		ret.put("location", fileName);
		thd = new Thread(new Runnable()
		{
			void err(String tex)
			{
				ret.put("error", tex);
				evt.work(ret);
				status = 0;
			}
			@Override
			public void run() {
				try
				{
					stTime = System.currentTimeMillis();  
					File fi = new File(fileName);
					if(!fi.exists())
						fi.createNewFile();
					OutputStream outp  = new FileOutputStream(fi);
					if(tag!=-1)
						ret.put("tag", String.valueOf(tag));
					URL Url = new URL(url);
					URLConnection conn = Url.openConnection();
					conn.connect();
					InputStream is = null;
					is = conn.getInputStream();
					int fileSize = conn.getContentLength();// 根据响应获	取文件大小
					if (fileSize <= 0) { // 获取内容长度为0
						err("无法获取文件大小");
						return;
					}
						if (is == null) { // 没有下载流
							err("无法打开下载流");
							return;
					}
		
					byte buf[] = new byte[1024];
		
					int numread;
					stTime = System.currentTimeMillis();
					ret.put("stTime", String.valueOf(stTime));
					q.push(new DownloadPackInfo(stTime));
					
					while ((numread = is.read(buf)) != -1)
					{
						if( status!=1 )
						{
							err("下载中断。");
							return;
						}
						downCount += numread;
						outp.write(buf,0,numread);
						DownloadPackInfo tmp = q.getLast();
						tmp.enTime = System.currentTimeMillis();
						tmp.dat = numread;
						q.add(new DownloadPackInfo(System.currentTimeMillis()));
						intervalDat += numread;
						if(nowSpdLimTim >0)
							thd.sleep(nowSpdLimTim);
//						thd.sleep(16);
					}
					outp.flush();
					is.close();
					outp.close();
					status = 0;
					enTime = System.currentTimeMillis();
					ret.put("error", "false");
					ret.put("speed", String.valueOf(getNowSpeed()));
					ret.put("byte", String.valueOf(downCount));
					evt.work(ret);
				}
				catch(Exception e)
				{
					err("未截获的错误");
				}
			}
		});
		if(syn)
			thd.run();
		else
			thd.start();
	}
	
}
