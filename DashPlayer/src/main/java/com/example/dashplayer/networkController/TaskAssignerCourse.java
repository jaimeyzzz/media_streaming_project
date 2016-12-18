package com.example.dashplayer.networkController;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.common.VideoInfo;
import com.example.dashplayer.networkController.TaskAssigner.TaskRecord;

public class TaskAssignerCourse extends TaskAssigner {

	public TaskAssignerCourse(OnEventListener evnt) {
		evntMain = evnt;
	}
	
	/**
	 * @param p	partner involved
	 */
	public void assignTask(PartnerInfo p) {
		// TODO task assign algorithm

		// get MPD file;
		if (p.mpdAcked == 0 && p.id == 0) {
			p.nowTask = -1;
			p.nowTaskBit = -1;
			postTask(p);
			return;
		}

		// choose a task;
        int partnerBandWidth = p.inBandWidth + p.outBandWidth;

		int tmp = n;
		for (int i = 0; i < n; ++i) {
            // if task not assigned
            if (videoStatus[i] == 0) {
                int fragmentBitrate = videoInfo.get(i).bitrate;
                int fragmentDuration = videoInfo.get(i).fragmentDuration;


                tmp = i;
                break;
            }
        }

		if (tmp == n)
			return;
		int no = tmp;
		videoStatus[no] = 1;
		downloadRecord[no] = new TimePair();
		downloadRecord[no].bufStTime = player.getBufferedLength();
		p.nowTask = no;
		int bitrate = 0;
		p.nowTaskBit = bitrate;
		lastBitrate = bitrate;
		downloadRecord[no].bitrate = bitrate;
		postTask(p);
	}
	
	public void postTask(PartnerInfo p) {
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