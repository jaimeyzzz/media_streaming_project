package com.lichao;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;

import android.util.Base64;
import android.util.Log;

public class NetworkUtility {
	
	/*
	 * 约定：
	 * 		cmdFileStart:
	 * 			0
	 * 		cmdFileTrans:
	 * 			1 {文件内容 : base64}
	 *		cmdFileEnd:
	 *			2 
	 *		cmdInfo:
	 *			3 {InfoPackStr : base64}
	 */
	public static enum Cmd
	{
		cmdFileStart(0), cmdFileTrans(1), cmdFileEnd(2), cmdInfo(3);
		private int code;
		private Cmd(int c) {
			code = c;
		} 
		public int toInt() {
			return code;
		}
	}
	
	///////////////////////////////////////////

	public String outPath;
	InputStream inp;
	OutputStream outp;
	OnEventListener evnt;
	public int id;
	ArrayDeque<DownloadPackInfo> q = new ArrayDeque<DownloadPackInfo>();
	
	class DownloadPackInfo
	{
		// 用来记录每一个下载的数据包大小、到达时间
		int byt;	//可用来记录数据包的前缀和
		long tim;	//完成的时间
		public DownloadPackInfo(long tim, int byt)
		{
			this.tim = tim;
			this.byt = byt;
		}
	}

	public void maintainQueue()
	{
		long now = System.currentTimeMillis();
		if(q.size() == 0)
			return;
		DownloadPackInfo p = q.getFirst();
		while(now - p.tim > 1000)
		{
			q.pop();
			if(q.size()>0)
				p = q.getFirst();
			else
				break;
		}
	}
	
	public NetworkUtility(InputStream _inp, OutputStream _outp, OnEventListener _evnt)
	{
		inp = _inp;
		outp = _outp;
		evnt = _evnt;
		outPath = "/storate/sdcard0/temporary/a.mp4";
		reset();
		tot = 0;
	}
	
	String recv;
	FileOutputStream fi;
	
	void putIntoQueue(int byt)
	{
		long now = System.currentTimeMillis();
		maintainQueue();
		q.push(new DownloadPackInfo(now, byt));
	}
	
	public int getSpeed()
	{
		maintainQueue();
		int sum = 0;
		ArrayDeque<DownloadPackInfo> tmp = q.clone();
		Iterator<DownloadPackInfo> i = tmp.iterator();
		while(i.hasNext())
		{
			DownloadPackInfo p = i.next();
			sum += p.byt;
		}
		return sum;
	}
	
	public void reset()
	{
		try {
			recv = "";
			if( fi != null )
				fi.close();
			
		} catch (IOException e) {
			InfoPack p = new InfoPack();
			p.put("cmd", Commands.notify);
			p.put("txt", "[错误]蓝牙重置时遇到了问题！");
			evnt.work(p);
			e.printStackTrace();
		}
	}
	
	// 什么东西都是手写的最靠谱
	public int find(byte[] buf, int num)
	{
		for(int i=0; i<buf.length; ++i)
		if(buf[i] == num)
			return i;
		return buf.length;
	}
	
	static int threshhold = 40960;
	int tot;
	
	// 对单条数据进行处理
	public void prework(String[] lst)
	{
		/*
		InfoPack pp = new InfoPack();
		pp.put("cmd", Commands.notify);
		pp.put("txt", "[调试]收到");
		evnt.work(pp);
		*/
		try {
			int cmd = Integer.valueOf(lst[0]);
			if(cmd == Cmd.cmdFileStart.code) {
				fi = new FileOutputStream(outPath);
				return;
			}
			if( cmd == Cmd.cmdFileTrans.code) {
				byte[] tmp = Base64.decode(lst[1], Base64.NO_WRAP);
				fi.write(tmp);
				tot += tmp.length;
				putIntoQueue(tmp.length);
				/*
				if(tot >= threshhold)
				{
					InfoPack p = new InfoPack();
					p.put("cmd", Commands.notify);
					p.put("txt", "收到："+ String.valueOf(tot) + ";" + System.currentTimeMillis() );
					evnt.work(p);
					tot = 0;
				}
				*/
				return;
			}
			if( cmd == Cmd.cmdFileEnd.code) {
				fi.close();
				InfoPack p = new InfoPack();
				p.put("cmd", Commands.bluetoothReceived);
				p.put("path", outPath);
				p.put("id", String.valueOf(id));
				evnt.work(p);
				return;
			}
			if( cmd == Cmd.cmdInfo.code) {
				// 传送的是一个键值对
				InfoPack p = InfoPack.fromBase64(lst[1]);
				if( p.containsKey("cmd") )
					p.put("cmd_", p.get("cmd"));
				p.put("cmd", Commands.bluetoothInfoReceived);
				evnt.work(p);
				return;
			}
		} catch (Exception e) {
			InfoPack p = new InfoPack();
			p.put("cmd", Commands.notify);
			p.put("txt", "[错误]蓝牙传输时遇到了问题！");
			evnt.work(p);
			e.printStackTrace();
		}
	}
	
	// 当收到数据的时候(raw)
	public synchronized void dataProcesser(byte[] buffer)
	{
		// synchronized(this) {
			recv += new String(buffer);
			while(recv.length()>0 && recv.charAt(0)!='{')
				recv = recv.substring(1);
			int pos;
			while((pos = recv.indexOf("}")) != -1)
			{
				String s = recv.substring(1, pos);
				recv = recv.substring(pos+1);
				String[] lst = s.split(" ");
				prework(lst);
			}
		// }
	}
	
	public void send(String tmp)
	{
		try {
			/*
			InfoPack pp = new InfoPack();
			pp.put("cmd", Commands.notify);
			pp.put("txt", "[调试]发送" + ";" + System.currentTimeMillis() );
			evnt.work(pp);
			*/
			tmp += "}";
			outp.write(tmp.getBytes());
		} catch (IOException e) {
			InfoPack p = new InfoPack();
			p.put("cmd", Commands.notify);
			p.put("txt", "[错误]蓝牙发送时遇到了问题！");
			evnt.work(p);
			e.printStackTrace();
		}	
	}
	
	public String appendInt(int p)
	{
		return Base64.encodeToString(String.valueOf(p).getBytes(), Base64.NO_WRAP) + " ";
	}

	public String appendCmd(Cmd p)
	{
		return "{" + String.valueOf(p.toInt()) + " ";
	}
	
	public String appendStr(String s)
	{
		return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP) + " ";
	}
	
	public String appendInfo(InfoPack p)
	{
		String info = p.toString();
		info = Base64.encodeToString(info.getBytes(), Base64.NO_WRAP);
		return info + " ";
	}

	public String appendByte(byte[] p)
	{
		String info = Base64.encodeToString(p, Base64.NO_WRAP);
		return info + " ";
	}
	
	public void startFileTrans()
	{
		String tmp = "";
		tmp += this.appendCmd(Cmd.cmdFileStart);
		// appendStr(fil);
		send(tmp);
	}
	
	public void endFileTrans()
	{
		String tmp = "";
		tmp += this.appendCmd(Cmd.cmdFileEnd);
		// appendStr(fil);
		send(tmp);
	}
	
	public void sendFilePiece(byte[] byt)
	{
		long tst  = System.currentTimeMillis();
		String tmp = "";
		tmp += this.appendCmd(Cmd.cmdFileTrans);
		tmp += this.appendByte(byt);
		long tmid = System.currentTimeMillis();
		send(tmp);
		long ten = System.currentTimeMillis();
//		Log.e("NetworkUtility", "Base64:" + String.valueOf(tmid-tst));
//		Log.e("NetworkUtility", "transmit:" + String.valueOf(ten - tmid));
	}
	
}
