package com.example.dashplayer.networkController;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.TimePair;

import java.util.ArrayList;

public class TaskAssignerCourse extends TaskAssigner {
	public TaskAssignerCourse(OnEventListener evnt) {
		evntMain = evnt;
	}


	// TODO：partner speed -> 0;
	/**
	 * @param p			partner
	 */
	public void assignTask(PartnerInfo p) {
		if (p.id == 0 && p.mpdAcked == 0) {
			p.nowTask = -1;
			p.nowTaskBit = -1;
			postTask(p);
			return;
		}
		// first available video fragment index;
		int fragIdx = 0;
		for(int i = 0; i < n; ++i) {
			if(videoStatus[i] == 0) {
				fragIdx = i;
				break;
			}
		}
        // choose a task here;
		int partnerIdx = partnerAvail.indexOf(String.valueOf(p.id));
		int bitrate = getFinalBitrate();

		// TODO
		int taskSpeed = p.outBandWidth;
		if (taskSpeed != -1) {
			double taskFinishTime = (double)Long.valueOf(p.stTime) + (p.nowTaskBit + bitrate) / (double)taskSpeed * fragmentDuration * 1000;
			for (int j = partnerIdx + 1; j < partnerAvail.size(); j ++) {
				PartnerInfo tmpP = partner.get(partnerAvail.get(j));
				double tmpSpeed = tmpP.outBandWidth;
				double tmpStartTime = (double)Long.valueOf(tmpP.stTime);
				if (tmpP.workStatus == 0)
					tmpStartTime = (double)System.currentTimeMillis();
				int taskNum = (int)((taskFinishTime - tmpStartTime) / tmpSpeed / (double)fragmentDuration / 1000.0);
				fragIdx += taskNum;
			}
		}
		else if (p.id != 0) {
			fragIdx = fragIdx + partnerAvail.size();
		}
		for (int i = fragIdx; i < n; i ++) {
			if (videoStatus[i] == 0)
				break;
		}
		if(fragIdx >= n)
			return;
		// status == 1 : task to do
		videoStatus[fragIdx] = 1;

        p.nowTask = fragIdx;
        if (fragIdx >= keyFragment) {
            lastBitrate = bitrate;
        }
        p.nowTaskBit = lastBitrate;
        downloadRecord[fragIdx] = new TimePair();
        downloadRecord[fragIdx].bufStTime = player.getBufferedLength();
		downloadRecord[fragIdx].bitrate = bitrate;

        fragmentBitrate[fragIdx] = lastBitrate;
		postTask(p);

	}

	public void postTask(PartnerInfo p) {
		InfoPack map = new InfoPack();
		p.workStatus = 1;
		if (p.nowTask == -1) { // download mpd file;
			map.put("cmd", Commands.downloadMpd);
		}
		else {
			taskRecord[p.nowTask] = new TaskRecord(p.id, p.nowTaskBit);
			map.put("cmd", Commands.taskAssign);
			map.put("no", String.valueOf(p.nowTask));
			map.put("bitrate", String.valueOf(p.nowTaskBit));
			map.put("url", videoInfo.get(p.nowTaskBit).url[p.nowTask]);
			map.put("stTime", String.valueOf(System.currentTimeMillis()));
		}

		if (p.id == 0) { // localhost
			evntMain.work(map);
		}
		else {
			innet.sendinfo(map, p.id, null, 0);
			// TODO sendHashMap(p 或者 p.socket,map);
		}
	}

    int getFinalBitrate() {
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
		int bitrate = ( res == -1?minn:res);
		System.out.println(bitrate);
        return bitrate;
    }
	@Override
	public int getSelectedBitrate() {
        if(videoInfo == null)
            return 0;
        return videoInfo.get(lastBitrate).bitrate;
	}
}