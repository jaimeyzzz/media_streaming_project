package com.example.dashplayer.networkController;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import android.app.Activity;
import com.example.dashplayer.FragmentPlayer;
import com.example.dashplayer.MainActivity;
import com.example.dashplayer.common.BatteryUtilities;
import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.PartnerMap;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.common.func;
import com.example.dashplayer.common.network.HttpDownloadModule;
import com.lichao.bluetooth.btbasic;

public class MasterControllerDumb extends MasterController {

	public MasterControllerDumb(FragmentPlayer _player, Activity _activity) {
		super(_player, _activity);
	}

	public int estimateTotBandwidth()
	{
		int tot = 0;
		for(int i=0;i<httpDown.size();++i)
			tot += httpDown.get(i).getNowSpeed();
		assigner.notifyBandwidth(tot);
		return tot;
	}
	
	public int getNowBitrate()
	{
		return assigner.getBitrate(player.nowPlaying);
	}
	
	public int getBufferedLength()
	{
		// 获得已经缓冲的长度
		return player.getBufferedLength();
	}
	
	public void reset_everything()
	{
		Iterator<Entry<String, PartnerInfo>> i = partner.entrySet().iterator();
		while(i.hasNext())
		{
			PartnerInfo p = i.next().getValue();
			p.mpdAcked = 0;
			/*
			p.outBandWidth = 0;
			p.inBandWidth = 0;
			p.connTime = 0;
			p.nowTask = 0;
			p.nowTaskBit = 0;
			*/
			InfoPack tmp = new InfoPack();
			tmp.put("cmd", Commands.reset);
			// TODO 传送键值对
		}
		onReset( null );
		assigner.reset();
	}
	
	///////////////// 从机 /////////////////////
	
	// 要求能够得到接入的设备的PartnerInfo，其中包含了连接的信息
	// 即在网络端就已经生成了PartnerInfo，使得能够得到其中的Socket和连接类型
	void onNewDeviceConnected(InfoPack p) {
		log("收到从机连入。");
		btNet.matchIdThd(partnerCount, p.get("mac"));	//step 3, included informid
		PartnerInfo tmp = new PartnerInfo(partnerCount);
		partner.put(String.valueOf(partnerCount),tmp);
		PartnerInfo part = partner.get(String.valueOf(partnerCount));
		part.transTask = 1;
		part.transTaskBit = 0;
		btNet.setSavePath(partnerCount, path+"a"+String.valueOf(part.transTask)+".mp4");
		partnerCount++;
	}
	
	// 当设备与主机中断连接时
	void onDisconnected(InfoPack p) {
		log("与#" + p.get("id") + "的连接中断。");
		partner.remove(p.get("id"));
	}
	
	// 当得到来自从机的心跳事件时
	void onHeartBeat( InfoPack p )
	{
		String id = p.get("id");
		PartnerInfo pInfo =  partner.get(id);
		pInfo.outBandWidth = Integer.valueOf(p.get("outBandWidth"));
	}
	
	///////////////// 主机自己 //////////////////////////

	void onReset( InfoPack info)
	{
		for(int i=0;i<httpDown.size();++i)
			httpDown.get(i).stop();
		httpDown.clear();
		httpDownloaded.clear();
	}
	
	void onTaskAssigned(InfoPack info)
	{
		/*
		int id = Integer.valueOf(info.get("id"));
		int no = Integer.valueOf(info.get("no"));
		downloadRecord[no] = new TimePair();
		downloadRecord[no].bufStTime = player.getBufferedLength();
		if(id!=0)
			return;*/
		String url = info.get("url");
		localHost.nowTask = Integer.valueOf(info.get("no"));
		localHost.nowTaskBit = Integer.valueOf(info.get("bitrate"));
		String location = path + localHost.id + "_" + localHost.nowTask + "_" + localHost.nowTaskBit + ".mp4";
		HttpDownloadModule tmp = new HttpDownloadModule();
		tmp.setTag(httpDownTag++);
		tmp.downFile(url, location, this.onFileOutterDownloaded, false);
		httpDown.add(tmp);
		this.log("收到任务"+localHost.nowTask+"，码率"+videoInfo.get(localHost.nowTaskBit).bitrate/1000+"k");
	}
	
	void downloadMpd()
	{
		localHost.nowTask = -1;
		localHost.nowTaskBit = -1;
		String location = mpdPath;
		HttpDownloadModule tmp = new HttpDownloadModule();
		tmp.setTag(httpDownTag++);
		tmp.downFile(mpdUrl, location, this.onFileOutterDownloaded, false);
		httpDown.add(tmp);
		this.log("开始下载mpd。");
	}

	// 外网下载到文件时
	OnEventListener onFileOutterDownloaded = new OnEventListener(){
		@Override
		public void work(Object param) {
			/*
			 * 考虑文件下载失败
			 */
			HttpDownloadModule httpDownObj = null;
			InfoPack map = (InfoPack)param;
			String location = map.get("location");
			String err = map.get("error");
			if(!err.equals("false"))
			{
				if(location.endsWith(".mpd"))
					loge("下载mpd失败！");
				else
					loge("下载"+location+"失败！");
				return;
			}
			int tag = Integer.valueOf(map.get("tag"));
			int no;
			for(int i=0;i<httpDown.size();++i)
				if(httpDown.get(i).tag==tag)
				{
					httpDownObj = httpDown.remove(i);
					httpDownloaded.add(httpDownObj);
					break;
				}
			if( location.endsWith(".mpd") )
			{
				// 当完成的是mpd文件时
				videoInfo = xmlParser.parseXmlFromFile(mpdPath);
				boolean ret = func.checkMpdAvailable(videoInfo);
				if(!ret)
				{
					loge("mpd解析失败：不正确的mpd！");
					return;
				}
				localHost.mpdAcked = 1;
				downloadRecord = new TimePair[videoInfo.get(0).url.length];
				assigner.downloadRecordBinder(downloadRecord);
				player.setInfo(videoInfo);
				assigner.videoInfoBinder(videoInfo);
				
				// 写入matlab
				String s = "set(gca,'xtick',[],'ytick',[";
				for(int i = 0; i<videoInfo.size(); ++i)
				{
					if( i>0 )
						s += ",";
					s += String.valueOf(videoInfo.get(i).bitrate);
				}
				logm( s+"]);" );
				
			}
			else
			{
				no = localHost.nowTask;
				player.onNewTruncAvailable(no,location);
				downloadRecord[no].bufEnTime = player.getBufferedLength();
				downloadRecord[no].enTime = System.currentTimeMillis();
				downloadRecord[no].dat = Integer.valueOf(map.get("byte"));
				downloadRecord[no].speed = Integer.valueOf(map.get("speed"));
			}
			int speed = Integer.valueOf(map.get("speed"));
			log("下载完成，速度是"+(speed/1024)+"Kb/s");
			assigner.assignTask(localHost);
		}
	};
	
	// 内网下载到文件时（绑定在网络模块中，不通过controller解析键值对获得）
	OnEventListener onFileInnerDownloaded = new OnEventListener(){
		@Override
		public void work(Object param) {
			/* TODO
			 * 考虑文件下载失败
			 */
			InfoPack map = (InfoPack)param;
			String location = map.get("location");
			int no = Integer.valueOf(map.get("no"));

			player.onNewTruncAvailable(no,location);
			
			downloadRecord[no].enTime = System.currentTimeMillis();
			downloadRecord[no].dat = Integer.valueOf(map.get("byte"));
			downloadRecord[no].speed = Integer.valueOf(map.get("speed"));
			downloadRecord[no].bufEnTime = player.getBufferedLength();
			
		}
	};
	
	
	
	public void play(String url)
	{
		reset_everything();
		mpdUrl = url;
		Iterator<Entry<String, PartnerInfo>> i = partner.entrySet().iterator();
		while(i.hasNext())
		{
			PartnerInfo p = i.next().getValue();
			assigner.assignTask(p);
		}
	}

	// 当从slaver那里(或者assigner!)收到键值对的时候 在这里分发到各个成员方法
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
			this.onDisconnected(p);
		if(cmd.equals(Commands.slaverConnected))
			this.onNewDeviceConnected(p);
		if(cmd.equals(Commands.taskFinished))
		{
			// 从外网 ( from externet )
			log("下载完成。");
		}
		if(cmd.equals(Commands.bluetoothReceived))	// 当已经接受(bluetoothReceived)
		{
			String local =  p.get("path");
			log("接收完成：" + local);
			PartnerInfo part = partner.get(p.get("id"));
			player.onNewTruncAvailable(part.transTask, local);
			part.transTask += 2;
			btNet.setSavePath(part.id, path+"a"+String.valueOf(part.transTask)+".mp4");
		}
		if(cmd.equals(Commands.notify))
			log(p.get("txt"));
		if( cmd.equals(Commands.heartBeat))
			onHeartBeat(p);	else
		if( cmd.equals(Commands.taskAssign) )
			onTaskAssigned(p);	else
		if( cmd.equals(Commands.downloadMpd) )
			downloadMpd();
		if( cmd.equals(Commands.transmitAcquire) )
		{
			int id = Integer.valueOf(p.get("id"));
			String path = p.get("location");
			log("准备蓝牙接收：" + String.valueOf(id) + "@" + path);
			btNet.setSavePath(id, path);
			PartnerInfo part = partner.get(String.valueOf(id));
			part.transTask = p.getInt("no");
			part.transTaskBit = p.getInt("bitrate");
		}
			
	}
}
