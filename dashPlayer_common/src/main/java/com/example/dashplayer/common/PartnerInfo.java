package com.example.dashplayer.common;

import java.net.Socket;
import java.util.ArrayList;


public class PartnerInfo {
	
	private static int partnerCnt = 0;

	public PartnerInfo(int id)
	{
		this.id = id;
	}
	
	public static PartnerInfo findPartnerById(ArrayList<PartnerInfo> lst, int no) {
		for(int i=0;i<lst.size();++i)
			if(lst.get(i).id==no)
				return lst.get(i);
		return null;	
	}

	public void setId() {
		id = partnerCnt++;
	}

	public int id;				// partner的唯一标识
	public int connType;		// 0:蓝牙 1:wifi -1:连的是自己
	public int outBandWidth;	// 外网速度
	public int inBandWidth;	// 内网速度
	public int connTime;		// 互联时间（预留）
	public int nowTask;		// 分配的任务在任务列表中的下标（第nowTask个trunk）
	public int nowTaskBit;		// 分配的任务的码率
	public int transTask;		// 正在传输的任务
	public int transTaskBit;	// 正在传输的任务的码率
	public int mpdAcked;		// 对方是否已经有mpd信息
	public long stTime;		// 任务的开始时间
	
}
 