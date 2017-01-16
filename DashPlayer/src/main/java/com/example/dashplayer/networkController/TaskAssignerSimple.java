package com.example.dashplayer.networkController;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.networkController.TaskAssigner.TaskRecord;

public class TaskAssignerSimple extends TaskAssigner {

	// 只发奇偶任务
	
	public TaskAssignerSimple(OnEventListener evnt) {
		evntMain = evnt;
		cnt = new int[2];
		cnt[1] = 1;
	}
	
	int[] cnt;
	
	int algo_getBitrate()
	{
		int res = -1;
		int minn = 0;
		int b = bandwidth*8;
		for(int i = 0;i<videoInfo.size();++i)
		{
			int p = videoInfo.get(i).bitrate;
			if(p<=b && (res==-1||videoInfo.get(res).bitrate<p))
				res = i;
			if(p<videoInfo.get(minn).bitrate)
				minn = i;
		}
		lastBitrate = ( res == -1?minn:res);
		return lastBitrate;
	}
	
	/**
	 * @param partner	参与者列表
	 * @param no		需要被指派任务的编号
	 * @param buffered	已经缓冲的时间
	 */
	public void assignTask(PartnerInfo p)
	{
		InfoPack map = new InfoPack();
		map.put("cmd", Commands.notify);
		map.put("txt", "[debug]Task assigned.");
		evntMain.work(map);
		
		if(p.mpdAcked==0 && p.id==0)
		{
			p.nowTask = -1;
			p.nowTaskBit = -1;
			postTask(p);
			return;
		}
		// first available video fragment index;
		int tmp = n;
		for(int i = 0; i<n; ++i) {
			if(videoStatus[i] == 0) {
				tmp = i;
				break;
			}
		}
		/*
		if(p.id == 0)
			tmp += 2;
		*/
		if(tmp>=n)
			return;
		int no = tmp;
		// status == 1 : task to do
		videoStatus[tmp] = 1;
		/*
		if(cnt[p.id]>=n)
			return;
		int no = cnt[p.id];
		cnt[p.id] += 2;
		*/
		downloadRecord[no] = new TimePair();
		downloadRecord[no].bufStTime = player.getBufferedLength();
		p.nowTask = no;
		int bitrate = algo_getBitrate();
		p.nowTaskBit = bitrate;
		lastBitrate = bitrate;
		downloadRecord[no].bitrate = bitrate;
		fragmentBitrate[no] = bitrate;
		postTask(p);
	}

	public void postTask(PartnerInfo p)
	{
		InfoPack map = new InfoPack();
		if(p.nowTask==-1)
		{
			map.put("cmd", Commands.downloadMpd);
		} else
		{
			videoStatus[p.nowTask] = 1;
			videoDownloadee[p.nowTask] = p.id;
			taskRecord[p.nowTask] = new TaskRecord(p.id,p.nowTaskBit);
			map.put("cmd", Commands.taskAssign);
			map.put("no", String.valueOf(p.nowTask));
			map.put("bitrate", String.valueOf(p.nowTaskBit));
			map.put("url", videoInfo.get(p.nowTaskBit).url[p.nowTask]);
			map.put("stTime", String.valueOf(System.currentTimeMillis()));
		}
		if(p.id==0) {
			// 如果是主机自己的话
			evntMain.work(map);
		}else
		{
			innet.sendinfo(map, p.id, null, 0);
		}
	}

	@Override
	public int getSelectedBitrate() {
		if(videoInfo == null)
			return 0;
		return videoInfo.get(lastBitrate).bitrate;
	}
}