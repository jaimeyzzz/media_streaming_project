package com.example.dashplayer.networkController;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.networkController.TaskAssigner.TaskRecord;

public class TaskAssignerPaper extends TaskAssigner {

	public TaskAssignerPaper(OnEventListener evnt)
	{
		evntMain = evnt;
	}
	
	public int quantify(double v)
	{
		v = v*0.9; // 防止波动
		
		int res = 0;
		for(int i=0;i<videoInfo.size();++i)
			if( videoInfo.get(i).bitrate <= v )
				res = i;
		return res;
	}
	
	int increaseCount = 0;
	
	public int getFinalBitrate(int k, double v)
	{	// 最后一步，延缓上涨加速下跌
		if(k==0)
			return 0;
		int m = getM( k );
		if( downloadRecord[k].bufStTime < qref/2 )
			return quantify(v);
		else
			if( v > downloadRecord[k-1].bitrate )
			{
				increaseCount ++ ;
				if( increaseCount > m )
				{
					increaseCount = 0;
					return Math.min(downloadRecord[k-1].bitrate + 1, videoInfo.size()-1);
				}
			}else
				increaseCount = 0;
		return downloadRecord[k-1].bitrate;	// 因为缓冲区足够用，所以仍然下较大的码率也行
	}
	
	public int getm( int k )
	{
		int delta = downloadRecord[k].bufEnTime - downloadRecord[k].bufStTime;
		if( delta > 0.4 * fragmentDuration )
			return 1;
		if( 0.2 * fragmentDuration < delta && delta <= 0.4 * fragmentDuration)
			return 5;
		if( 0 * fragmentDuration < delta && delta <= 0.2 * fragmentDuration)
			return 15;
		return 20;
	}
	
	public int getM( int k )
	{
		int tot = 0;
		int cnt = 0;
		for(int i=k-1; i>=0 && cnt<3; --i,++cnt)
			tot += getm(i);
		return tot/cnt;
	}
	
	/**
	 * @param partner	参与者列表
	 * @param no		需要被指派任务的编号
	 * @param buffered	已经缓冲的时间
	 */
	public void assignTask(PartnerInfo p)
	{
		// TODO 需要填入算法
		// 目前是只有主机的测试版本
		if(p.mpdAcked==0 && p.id==0)
		{
			p.nowTask = -1;
			p.nowTaskBit = -1;
			postTask(p);
			return;
		}
		int tmp = n;
		for(int i = 0; i<n; ++i)
			if(videoStatus[i] == 0) {
				tmp = i;
				break;
			}
		if(tmp==n)
			return;
		int no = tmp;
		videoStatus[no] = 1;
		downloadRecord[no] = new TimePair();
		downloadRecord[no].bufStTime = player.getBufferedLength();
		p.nowTask = no;
		double f = getF(no);
		int bitrate = getFinalBitrate(no,f*bandwidth);
		p.nowTaskBit = bitrate;
		lastBitrate = bitrate;
		downloadRecord[no].bitrate = bitrate;
		postTask(p);
	}

	public double getF( int k )
	{
		/* 注意！ 这个函数对与k=0的情况是无效的！ */
		if(k==0)
			return 1;
		double p = 1;
		double W = 10;
		double tmp = Math.exp(p*(downloadRecord[k].bufStTime-qref));
		double Fq = 2*tmp/(1+tmp);
		double Ft = fragmentDuration/Math.max(1,fragmentDuration - (downloadRecord[k-1].bufEnTime - downloadRecord[k-1].bufStTime));
		int maxBitrate = videoInfo.get(videoInfo.size()-1).bitrate;
		double Fv = maxBitrate / ( downloadRecord[k-1].bitrate + W ) + W / ( maxBitrate + W );
		return Fq*Ft*Fv;
	}
	
	public void postTask(PartnerInfo p)
	{
		InfoPack map = new InfoPack();
		if(p.nowTask==-1)
		{
			map.put("cmd", Commands.downloadMpd);
		} else
		{
			taskRecord[p.nowTask] = new TaskRecord(p.id,p.nowTaskBit);
			map.put("cmd", Commands.taskAssign);
			map.put("no", String.valueOf(p.nowTask));
			map.put("bitrate", String.valueOf(p.nowTaskBit));
			map.put("url", videoInfo.get(p.nowTaskBit).url[p.nowTask]);
			map.put("stTime", String.valueOf(System.currentTimeMillis()));
		}
		if(p.id==0)
		{
			// 如果是主机自己的话
			evntMain.work(map);
		}else
		{
			// 给主机发一份副本
			evntMain.work(map);
			// TODO sendHashMap(p 或者 p.socket,map);
		}
	}

	@Override
	public int getSelectedBitrate() {
		// TODO Auto-generated method stub
		return 0;
	}
}
