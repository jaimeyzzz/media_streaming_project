package com.example.dashplayer.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.widget.TextView;

public class LoggerTextviewMatlab implements OnEventListener {

	Activity act;
	TextView textView;
	OutputStream fi_m, fi_dat;
	Writer fout_m, fout_dat;
	Timer tFlush;
	
	public LoggerTextviewMatlab(Activity act, TextView textView)
	{
		this.act = act;
		this.textView = textView;
		restartLog();
	}
	
	public void writem(String s) throws IOException
	{
		fout_m.write(s);
		fout_m.flush();
	}
	
	public void writedat(String s) throws IOException
	{
		fout_dat.write(s);
		fout_dat.flush();
	}
	
	public void restartLog()
	{
		try {
			File dir = new File("/sdcard/sample/");
			dir.mkdir();
			fout_m = func.openWriteFile(fi_m, fout_m, "/sdcard/sample/log.m");
			fout_dat = func.openWriteFile(fi_dat, fout_dat, "/sdcard/sample/log.dat");
			writem("figure;\n");
			writem("subplot(1,2,1);\n");
			writem("dat = load(\'log.dat');\n");
			writem("x = dat(:,1);\n");
			writem("yBitrate = dat(:,2);\n");
			writem("yBandwidth = dat(:,3);\n");
			writem("batteryPct = dat(:,4);\n");
			writem("hold on;\n");
			writem("plot(x,yBitrate,\'-o\',x,yBandwidth,\':*\');\n");
			writem("xlabel('playback time(sec)');\n");
			writem("ylabel('bitrate(kilobit)');\n");
			writem("title('Selected Bitrate Compared with Estimated Bandwidth.');\n");
			writem("hold off;\n");
			writem("subplot(1,2,2);");
			writem("plot(x,batteryPct,\'-o\');\n");
			writem("xlabel('playback time(sec)');\n");
			writem("ylabel('battery level(%)');\n");
			writem("title('Battery level.');\n");
			
			if(tFlush!=null)
			{
				tFlush.cancel();
				tFlush.purge();
			}
			tFlush = new Timer();
			tFlush.schedule(new TimerTask(){
				@Override
				public void run() {
					if( fout_m == null || fout_dat == null )
					{
						tFlush.cancel();
						return;
					}
					try {
						fout_m.flush();
						fout_dat.flush();
					} catch (IOException e) {
						work("写入日志失败。");
						tFlush.cancel();
						e.printStackTrace();
					}
				}
			}, 0, 10000);
	        
		} catch (Exception e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void work(Object _text) {
		try
		{
			final String text = (String)_text;
			if( text.startsWith("[matlab_m]"))
			{
				// 输出到matlab_m中
				String s = text.substring("[matlab_m]".length());
				writem(s+"\n");
				fout_m.flush();
			}else
			if( text.startsWith("[matlab_dat]"))
			{
				//输出到matlab_dat中
				String s = text.substring("[matlab_dat]".length());
				writedat(s+"\n");
				fout_dat.flush();
			}else
			{
				// 在TextView中显示
				act.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						textView.append(text+"\n");
					}
				});
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
}
