package com.lichao.tramsmitunit;

import java.util.HashMap;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.lichao.bluetooth.btbasic;
import com.lichao.bluetooth.btservice;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Start extends Activity implements OnEventListener {

	private static final String TAG = "transmitstart"; 
	private static Context context;
	private Button startButton;
	private Button slaversendButton;
	private Button mastersendButton;
	private Button connecttodeviceButton;
	private int i;
	int id;	// 测试用的id
	int top;	// 已经分配了多少id
	private InfoPack infopack;
	private static String prestatue;
	private static String sendstatue;
	private String receivestatue;
	private String Mac;
	private String Ip;
	public static btbasic m_btbasic;
	public btservice m_btservice;
	public static String addr;  //this varible maybe should not be static, every device should have thier own?
	public static String path;
	public static long time=0;
	public static String filename;//the name of the file that the slaver will send
	/*******match map*******/
 //   public static HashMap<String,String> match_map=null;//use to match id with mac-addresss match_map
	public static String[] transLst;
	public int n;
	public int now;
	public String hostAddr = btbasic.LGmac;//target address static;
	Start me;
	int slaver;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		me = this;
		setContentView(R.layout.activity_start);
		m_btbasic = new btbasic(this);
	    prestatue=m_btbasic.tranpre("bt");
	    top = 1;

	    n = 6;
	    transLst = new String[n];
	    for(int i = 0; i<6; ++i)
	    	transLst[i] = "/storage/sdcard0/temporary/a" + String.valueOf(i) + ".mp4";
	    
		mastersendButton = (Button)findViewById(R.id.mastersend);
		mastersendButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				/* now = 0;sendNew();  */
				InfoPack p = new InfoPack();
				p.put("cmd", Commands.notify);
				p.put("txt", "Hello world!");
				m_btbasic.sendinfo(p, id, me, 0);
			}
		});
		slaver = 0;	// 初始不是slaver
		startButton = (Button)findViewById(R.id.start);
		startButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//addr =match_map.get(0);// equals addr= LGmac;
				m_btbasic.requestconnect(hostAddr);
				id = 0;
				slaver = 1;
				Log.i(TAG, "SUC");
			}
		});
	   // btbasic m_btbasic= new btbasic();//why can I define it here and use it in the listeners?because protect?
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	public void sendNew()
	{
		if(now>=n)
		{
			Toast.makeText(context, "上传完成。", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(context, "开始传输"+transLst[now], Toast.LENGTH_SHORT).show();
		m_btbasic.sendtrunck(0, transLst[now], time);
		now++;
	}

	@Override
	public void work(Object param) {
		// Handle bluetooth events
		final InfoPack p = (InfoPack)param;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				String cmd = p.get("cmd");
				if(p.get("cmd").equals(Commands.connLost))
					Toast.makeText(context, "连接中断。", Toast.LENGTH_SHORT).show();
				if(p.get("cmd").equals(Commands.slaverConnected))
				{
					Toast.makeText(context, "已连接:"+p.get("mac"), Toast.LENGTH_SHORT).show();
					if(slaver == 0)
					{
						id = top++;
						m_btbasic.matchIdThd(id, p.get("mac"));	//step 3
						m_btbasic.setSavePath(id, "/storage/sdcard0/temporary/a0.mp4");
					}else
					{
						id = 0;
						m_btbasic.matchIdThd(id, "");
						m_btbasic.setSavePath(id, "/storage/sdcard0/temporary/a0.mp4");
					}
				}
				if(cmd.equals(Commands.taskFinished))
				{
					Toast.makeText(context, "下载完成。", Toast.LENGTH_SHORT).show();
				}
				if(cmd.equals(Commands.bluetoothReceived))
				{
					Toast.makeText(context, "下载完成：" + p.get("path"), Toast.LENGTH_SHORT).show();
					m_btbasic.setSavePath(id, "/storage/sdcard0/temporary/a" + String.valueOf(now) + ".mp4");
					now++;
				}
				if(p.get("cmd").equals(Commands.notify))
					Toast.makeText(context, p.get("txt"), Toast.LENGTH_SHORT).show();
				if(p.get("cmd").equals(Commands.bluetoothTransmitted))
					sendNew();
				if(cmd.equals(Commands.bluetoothInfoReceived))
				{
					String cmd_ = p.get("cmd_");
					if(cmd_.equals(Commands.informId))
					{
						// step 4
						Toast.makeText(context, "id：" + p.get("id"), Toast.LENGTH_SHORT).show();
						/*
						int id = Integer.valueOf(p.get("id"));
						m_btbasic.setMyId(id);
						*/
					}
					if(cmd_.equals(Commands.notify))
						Toast.makeText(context, p.get("txt"), Toast.LENGTH_SHORT).show();
				}
			}});		
	}

}
