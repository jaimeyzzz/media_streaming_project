package com.example.dashplayer.networkController;

import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.example.dashplayer.XmlParser;
import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.network.HttpDownloadModule;
import com.example.dashplayer.networkController.SlaverController.DownloadedVideo;
import com.lichao.bluetooth.btbasic;


public class SlaverControllerDumb extends SlaverController {

	public SlaverControllerDumb()
	{
		now = 2;
		innet = new btbasic(this);
		innet.tranpre("bt");
		me = new PartnerInfo(0);
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
	Timer timerHeartBeat;
	static int heartbeatInterval = 1000;	// 心跳频率
	
	// 要求能够得到master设备的Socket，其中包含了连接的信息
	OnEventListener onMasterConnected = new OnEventListener(){
		@Override
		public void work(Object param) {
			master = (Socket)param;
			timerHeartBeat.schedule(onHeartBeat, 0, heartbeatInterval);
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
		String local = video.location;
		log( "开始传输:" + local);
		innet.sendtrunck(0, local, 0);
	}
	
	void newDumbDown()
	{
		/*
		HttpDownloadModule tmp = new HttpDownloadModule();
		tmp.downFile(	"http://192.168.1.222/medialab/sintel/400k/sintel_2048_"+String.valueOf(now)+".mp4",
						"/storage/sdcard0/temporary/a"+String.valueOf(now)+".mp4", onFileDownloaded, false);
		httpDown.add(tmp);
		*/
		// 直接使用本地资源
		if(now>=58)
			return;
		InfoPack ret = new InfoPack();
		ret.put("location", "/storage/sdcard0/local/a"+String.valueOf(now)+".mp4");
		now+=2;
		onFileDownloaded.work(ret);
	}
	
	// 当从机完成任务下载时
	OnEventListener onFileDownloaded = new OnEventListener(){
		@Override
		public void work(Object param) {
			/////////////////////
			// Only for test
			InfoPack para = (InfoPack)param;
			String location = para.get("location");
			addToTransQueue(new DownloadedVideo(0,0,location,0));
			if(now>=60)
				return;
			newDumbDown();
			//
			//////////////////////
		}
	};
	
	public void connectMaster()
	{
		innet.requestconnect(innet.Nexus7mac);
		log("正在连接");
//		bluetooth.
	}

	// 当从master（或者蓝牙）那里收到键值对的时候 在这里分发到各个成员方法
	@Override
	public void work(Object param) {
		// 当蓝牙事件触发的时候
		InfoPack p = (InfoPack)param;
		String cmd = p.get("cmd");
		if(cmd.equals(Commands.connLost))
			log("连接中断。");
		if(cmd.equals(Commands.slaverConnected))
		{
			innet.matchIdThd(0, "");
			newDumbDown();
			log("已连接。");
		}
		if(cmd.equals(Commands.taskFinished))
		{
			log("下载完成。");
		}
		if(cmd.equals(Commands.notify))
			log(p.get("txt"));
		if(cmd.equals(Commands.bluetoothTransmitted))
		{
			log("发送完成。");
			downloadedQueue.pop();
			sentFileInQueue();
		}
	}
}
