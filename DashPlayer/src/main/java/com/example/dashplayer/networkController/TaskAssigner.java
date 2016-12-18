package com.example.dashplayer.networkController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.dashplayer.FragmentPlayer;
import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.PartnerMap;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.common.VideoFragmentsInfo;
import com.example.dashplayer.common.VideoInfo;
import com.lichao.bluetooth.btbasic;

public abstract class TaskAssigner {
	
	class TaskRecord
	{
		int id,br;
		public TaskRecord(int id, int br)
		{
			this.id = id;
			this.br = br;
		}
	}
	
	TaskRecord[] taskRecord;

	static int qref = 30;
	
	OnEventListener evntMain;	// 当要给Control发消息的时候的接收对象
	VideoInfo videoInfo;
	int fragmentDuration;
	int[] videoStatus;	// 0：未下载 1：已分配人去下载 2：已下载完成 3：已播放或正在播放
	int[] videoDownloadee;
	int lastBitrate;
	String mpdUrl;
	int n;
	int bn;
	int nowPlaying, nowPlayingTime;	// 前正在播放的切片编号，以及当前切片的已经播放时间
	FragmentPlayer player;
	TimePair[] downloadRecord;
	PartnerMap partner;
	int bandwidth;
	btbasic innet;
	
	public void reset()
	{
		if(videoStatus==null)
			videoStatus = new int[0];
		for(int i=0;i<videoStatus.length;++i)
			switch(videoStatus[i])
			{
			case 3: videoStatus[i] = 2;break;
			case 1: videoStatus[i] = 0;break;
			}
	}
	
	public void informDownloaded(int no)
	{
		videoStatus[no] = 1;
	}
	
	public void informPlayStatus(int no,int tim)
	{
		videoStatus[no] = 3; 
	}
	
	public void notifyBandwidth(int speed)
	{
		// 获取带宽
		bandwidth = speed;
	}
	
	public int getBitrate(int no)
	{
		/*
		if(taskRecord == null || taskRecord.length ==0)
			return 0;
		*/
		//if(no==-1)
		return lastBitrate;
		//return taskRecord[no].br;
	}
	
	public void videoInfoBinder(VideoInfo videoInfo)
	{
		this.videoInfo = videoInfo;
		n = videoInfo.get(0).url.length;
		videoStatus = new int[n];
		videoDownloadee = new int[n];
		taskRecord = new TaskRecord[n];
		bn = videoInfo.size();
		fragmentDuration = videoInfo.get(0).fragmentDuration;
		qref = Math.max(10,fragmentDuration * 2 );	// 即20
	}
	
	public void playerBinder( FragmentPlayer player)
	{
		this.player = player;
	}
	
	public void downloadRecordBinder( TimePair[] downloadRecord )
	{
		this.downloadRecord = downloadRecord;
	}
	
	public void partnerBinder(	PartnerMap partner )
	{
		this.partner = partner;
	}
	
	public void networkBinder( btbasic innet)
	{
		this.innet = innet;
	}
	
	abstract public void assignTask(PartnerInfo p);
	abstract public int getSelectedBitrate();
	
	public void onPartnerLost(String p)
	{
		PartnerInfo info = partner.get(p);
		int id = Integer.valueOf(p);
		for(int i = 0;i<n; ++i)
			if(videoStatus[i] == 1 && videoDownloadee[i] == id)
				videoStatus[i] = 0;
	}
	
	public void fileDownloaded(int p)
	{
		videoStatus[p] = 2;
	}
	
}
