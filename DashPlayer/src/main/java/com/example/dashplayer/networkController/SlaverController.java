package com.example.dashplayer.networkController;

import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.Toast;

import com.example.dashplayer.XmlParser;
import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.Logable;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.VideoFragmentsInfo;
import com.example.dashplayer.common.VideoInfo;
import com.example.dashplayer.common.network.HttpDownloadModule;
import com.lichao.bluetooth.btbasic;

public class SlaverController extends Logable implements OnEventListener {

	static String path = "/storage/sdcard0/temporary/";
	static String mpdPath = path + "temp.mpd";

    btbasic innet;
    int now;
    PartnerInfo me;
    int status = 0;	// 0 : 未连接, 1 : 已连接

    public PartnerInfo getInfo()
    {
        return me;
    }

    Random rand = new Random();

    public SlaverController()
    {
        now = 1;
        innet = new btbasic(this);
        innet.tranpre("bt");
        me = new PartnerInfo(0);
    /*
    HttpDownloadModule tmp = new HttpDownloadModule();
    tmp.downFile("http://192.168.1.222/medialab/sintel/400k/sintel_2048_2.mp4", "/storage/sdcard0/temporary/a2.mp4", this.onFileDownloaded, false);
    httpDown.add(tmp);
    */
    }

    ArrayList<HttpDownloadModule> httpDown = new ArrayList<HttpDownloadModule>();
    XmlParser xmlParser = new XmlParser();
    //	VideoInfo videoInfo;
    class DownloadedVideo
    {
        // 一个暂存信息用的结构
        int no,bit;
        long stTime;
        String location;
        public DownloadedVideo(int no,int bit,String location,long stTime)
        {
			this.no = no;
			this.bit = bit;
			this.location = location;
			this.stTime = stTime;
		}
	}
	
	void addToTransQueue(DownloadedVideo p)
	{
		downloadedQueue.add(p);
		if(downloadedQueue.size() == 1)
			this.sentFileInQueue();
	}
	
	ArrayDeque<DownloadedVideo> downloadedQueue = new ArrayDeque<DownloadedVideo>(); 
	
	Socket master;
	Timer timerHeartBeat = new Timer();
	static int heartbeatInterval = 1000;	// 心跳频率
	
	// 要求能够得到master设备的Socket，其中包含了连接的信息
	OnEventListener onMasterConnected = new OnEventListener(){
		@Override
		public void work(Object param) {
			master = (Socket)param;
		}
	};
	
	// 当设备与主机中断连接时
	OnEventListener onDisconnected = new OnEventListener(){
		@Override
		public void work(Object param) {
			//TODO donothing?
			status = 0;
		}
	};
	
	// 传送在队首的文件
	void sentFileInQueue()
	{
		if(downloadedQueue.isEmpty())
			return;
		DownloadedVideo video = downloadedQueue.getFirst();
		InfoPack info = new InfoPack();
		info.put("id", String.valueOf(me.id));
		info.put("cmd", Commands.transmitAcquire);
		info.put("no", String.valueOf(video.no));
		info.put("bitrate",String.valueOf(video.bit));
		info.put("location", video.location);
		innet.sendinfo(info, 0, null, 0);
		me.transTask = video.no;
		me.transTaskBit = video.bit;
		String local = video.location;
		log( "开始传输:" + local);
		innet.sendtrunck(0, local, 0);
	}
	
	// 当从机完成任务下载时
	OnEventListener onFileDownloaded = new OnEventListener(){
		@Override
		public void work(Object param) {
			InfoPack map = (InfoPack)param;
			String location = map.get("location");
			String err = map.get("error");
			if(!err.equals("false"))
			{
				// TODO 处理错误
				log("[Error]下载失败！！");
				return;
			}
			log("下载完成。");
			InfoPack info = new InfoPack();
			info.put("cmd", Commands.taskFinished);
			info.put("id", String.valueOf(me.id));
			innet.sendinfo(info, 0, null, 0);
			addToTransQueue(new DownloadedVideo(me.nowTask,me.nowTaskBit,location,me.stTime));
		}
	};
	
	void getMyOutBandwidth()
	{
		me.outBandWidth = 0;
		if(httpDown.size()<=0)
			return;
		int tot = 0;
		tot = httpDown.get(httpDown.size()-1).getNowSpeed();
		me.outBandWidth = tot;
	}
	
	// 每次心跳 
	TimerTask onHeartBeat = new TimerTask(){
		@Override
		public void run() {
			InfoPack info = new InfoPack();
			info.put("cmd",Commands.heartBeat);
			info.put("id",String.valueOf(me.id));
			getMyOutBandwidth();
			for(int i =0 ;i<httpDown.size();++i)
				if(httpDown.get(i)!=null)
				{
					int bas = spdLim / 3;
					bas += (bas&1);
					httpDown.get(i).setSpeedLim(spdLim + rand.nextInt(bas)-(bas/2+1));
				}
			info.put("outBandWidth", String.valueOf(me.outBandWidth));
			innet.sendinfo(info, 0, null, 0);
		}
	};
	
	int spdLim = 0;

	public void setLimSpd(int lim)
	{
		spdLim = lim;
	}
	
	int httpDownTag = 0;
	// 当收到主机分配的任务时
	void onTaskAssigned( InfoPack info )
	{
		String url = info.get("url");
		me.nowTask = Integer.valueOf(info.get("no"));
		me.nowTaskBit = Integer.valueOf(info.get("bitrate"));
		me.stTime = Long.valueOf(info.get("stTime"));
		me.workStatus = 1;
		String location = path + me.id + "_" + me.nowTask + "_" + me.nowTaskBit + ".mp4";
		HttpDownloadModule tmp = new HttpDownloadModule();
		tmp.setSpeedLim(spdLim);
//		tmp.setSpeedLim(14);
		tmp.setTag(httpDownTag++);
		tmp.downFile(url, location, this.onFileDownloaded, false);
		httpDown.add(tmp);
		this.log("收到任务"+me.nowTask+"，码率为第" + me.nowTaskBit + "等级。");
	}
	
	void onReset( InfoPack info)
	{
		for(int i=0;i<httpDown.size();++i)
			httpDown.get(i).stop();
		httpDown.clear();
		me.nowTask = -1;
		me.nowTaskBit = -1;
		me.workStatus = 0;
	}
	
	public void connectMaster()
	{
		innet.requestconnect(innet.Note10mac);
		log("正在连接");
//		bluetooth.
	}

	// 当从master（或者蓝牙）那里收到键值对的时候 在这里分发到各个成员方法
	@Override
	public void work(Object param) {
		// 当蓝牙事件触发的时候
		InfoPack p = (InfoPack)param;
		String cmd = p.get("cmd");
		if(cmd.equals(Commands.bluetoothInfoReceived))
		{
			p.put("cmd", p.get("cmd_"));
			work(p);
		}
		
		if(cmd.equals(Commands.connLost))
			log("连接中断。");
		if(cmd.equals(Commands.slaverConnected))
		{
			log("已连接。");
		}
		if(cmd.equals(Commands.taskFinished))
		{
			log("下载完成。");
		}
		if(cmd.equals(Commands.bluetoothReceived))
		{
			log("下载完成：" + p.get("path"));
		}
		if(cmd.equals(Commands.notify))
			log(p.get("txt"));
		if(cmd.equals(Commands.bluetoothTransmitted))
		{
			log("发送完成。");
			downloadedQueue.pop();
			sentFileInQueue();
		}
		if( cmd.equals(Commands.taskAssign) )
			onTaskAssigned(p);
		if( cmd.equals(Commands.informId))
		{
			me.id = Integer.valueOf(p.get("id"));
			innet.matchIdThd(0, "");
			log("分配到Id：" + String.valueOf(me.id));
			timerHeartBeat.schedule(onHeartBeat, 0, heartbeatInterval);
		}
		if( cmd.equals(Commands.reset))
			onReset(p);
	}

}
