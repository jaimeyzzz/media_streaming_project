package com.example.dashplayer.networkController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.widget.Toast;

import com.example.dashplayer.FragmentPlayer;
import com.example.dashplayer.MainActivity;
import com.example.dashplayer.XmlParser;
import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.Logable;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.common.PartnerMap;
import com.example.dashplayer.common.TimePair;
import com.example.dashplayer.common.VideoFragmentsInfo;
import com.example.dashplayer.common.VideoInfo;
import com.example.dashplayer.common.func;
import com.example.dashplayer.common.BatteryUtilities;
import com.example.dashplayer.common.network.HttpDownloadModule;
import com.lichao.bluetooth.btbasic;

/* 
 * 外部非公共依赖：
  		FragmentPlayer player
 */

public class MasterController extends Logable implements OnEventListener {
	static String path = "/storage/sdcard0/temporary/";
	static String mpdPath = path + "temp.mpd";
	static String mpdUrl = "http://114.215.41.77/sintel/test.mpd";
	static int maxTruncNum = 1000000;
	static int minDownloadSpeed = 10;
    static int bandWidthQueueSize = 10;
//	public Integer downloading = -1;

	/* partner info */
	int partnerCount = 0;
	public PartnerMap partner = new PartnerMap();
	public ArrayList<String> partnerAvail = new ArrayList<String>();
	class SortPartnerAvail implements Comparator {
		public int compare(Object o1, Object o2) {
			int s1 = partner.get((String)o1).outBandWidth;
			int s2 = partner.get((String)o2).outBandWidth;
			if (s1 == -1) {
				return (s2 == -1) ? 0 : 1;
			}
			if (s2 == -1) {
				return 0;
			}
			if (s1 > s2)
				return 1;
			return 0;
		}
	}
	PartnerInfo localHost;

	/* video info */
	VideoInfo videoInfo;
	int n;	// 视频的trunk数
	int targetLim = 0;

	TaskAssigner assigner;
	FragmentPlayer player;
	int httpDownTag = 0;
	
	TimePair[] downloadRecord;
	BatteryUtilities battery;
    Queue<Integer> bandWidthQueue = new ArrayDeque<Integer>();
	int nowBandwidth, nowBandwidth2;
	
	// other initial classes;
	Random rand = new Random();
	ArrayList<HttpDownloadModule> httpDown = new ArrayList<HttpDownloadModule>(); //用来保存已经下载完成的下载模块对象 严格按照下载完成时间排序
	ArrayList<HttpDownloadModule> httpDownloaded = new ArrayList<HttpDownloadModule>(); // 正在下载或者还没有开始下载的对象
	XmlParser xmlParser = new XmlParser();
	Timer heartbeat = new Timer();
	btbasic btNet;	// inner network transmit obj
	

	// 主机端需要绑定一个播放器
	public MasterController(FragmentPlayer _player, Activity activity) {
		btNet = new btbasic(this);
		btNet.tranpre("bt");
		battery = new BatteryUtilities(activity);
		player = _player;

		assigner = new TaskAssignerCourse(this);
		assigner.playerBinder(player);
		assigner.partnerBinder(partner);
		assigner.networkBinder(btNet);
		assigner.partnerAvailBinder(partnerAvail);
//		localHost.onFileRecv = onFileInnerDownloaded;		2

		// TODO : create path directory;
		// add localhost as a partner
        partnerCount = 0;
		localHost = new PartnerInfo(partnerCount);
		partner.put(String.valueOf(partnerCount), localHost);
        partnerCount ++;

		heartbeat.schedule(new TimerTask(){
			@Override
			public void run() {
				onHeartBeat();
			}
		}, 0, 1000);
		log("Master device deployed.");
	}
	
	float batteryLevel;
	
	public void onHeartBeat() {
		spdLim += (targetLim - spdLim)/2+(targetLim - spdLim)%2;
		int bw = estimateTotBandwidth(); 
		batteryLevel = battery.getBatteryPercent();
		for (int i = 0; i < httpDown.size(); ++i) {
			if (httpDown.get(i) != null) {
				int bas = spdLim / 3;
				if (bas != 0) {
					bas += (bas & 1);
					httpDown.get(i).setSpeedLim(spdLim + rand.nextInt(bas) - (bas / 2 + 1));
				}
			}
		}
		if (player.isPlaying())
			logdat(String.valueOf(nowBandwidth)+" "+String.valueOf(nowBandwidth2)+" "+String.valueOf(assigner.getSelectedBitrate()));
		for (Entry<String, PartnerInfo> p : partner.entrySet())
			if(p.getValue().id!=0)
				btNet.sendTrash(p.getValue().id);

		// TODO : change fragment status when network changed;
		/*for (int i = 0; i < partnerAvail.size(); i ++) {
            PartnerInfo p = partner.get(partnerAvail.get(i));
            if (p.outBandWidth != -1 && p.outBandWidth < minDownloadSpeed) {
                if (p.nowTask >= 0)
                    assigner.videoStatus[p.nowTask] = 0;
            }
        }*/

        // log infomation;
        String logFileName = "/storage/sdcard0/DecmodLog/" + "log.txt";

        try {
            File targetFile = new File(logFileName);
            File parent = targetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                return;
            }
            if(!targetFile.exists() && !targetFile.createNewFile()) {
                return;
            }

            RandomAccessFile fout = new RandomAccessFile(targetFile, "rw");
            fout.seek(targetFile.length());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            Date date = new Date(System.currentTimeMillis());

            String str = formatter.format(date) + " ";
            str += String.valueOf(nowBandwidth) + " " + String.valueOf(nowBandwidth2) + " ";
            str += String.valueOf(player.nowPlaying) ;
            if (assigner.fragmentBitrate.length > 0 && player.nowPlaying >= 0) {
                str += " " + String.valueOf(assigner.fragmentBitrate[player.nowPlaying]);
            }
            str += "\n";
            fout.write(str.getBytes());
            fout.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
	
	public int getBatteryLevel() {
		return (int)(battery.getBatteryPercent() * 100);
	}

	public int estimateTotBandwidth() {
		int tot = 0;
		int btSpeed = 0;
		for(int i=0;i<httpDown.size();++i)
			tot += httpDown.get(i).getNowSpeed();
		int innerSpd = 0;
		Iterator<Entry<String, PartnerInfo>> i = partner.entrySet().iterator();
		while(i.hasNext()) {
			Entry<String, PartnerInfo> p = i.next();
            if (p.getValue().id == 0)
                continue;
			innerSpd += p.getValue().outBandWidth;
		}
		btSpeed = btNet.getSpeed();
		log("http:"+tot+";inner:"+innerSpd);
        bandWidthQueue.offer(tot);
        if (bandWidthQueue.size() > bandWidthQueueSize) {
            bandWidthQueue.poll();
        }
        tot = 0;
        for (Integer iter : bandWidthQueue) {
            tot += iter;
        }
        tot = (int)(tot / (double)bandWidthQueue.size());
        assigner.notifyBandwidth(tot + innerSpd);
        if (httpDown.size() > 0)
            partner.get("0").outBandWidth = tot;
		nowBandwidth = tot;
		nowBandwidth2 = innerSpd;
		return tot;
	}

	public int getNowBitrate()
	{
		return assigner.getSelectedBitrate();
	}
	
	public int getBandwidth()
	{
		return nowBandwidth;
	}

	public int getBandwidth2()
	{
		return nowBandwidth2;
	}
	
	public int getBufferedLength()
	{
		// 获得已经缓冲的长度
		return player.getBufferedLength();
	}
	
	public void reset_everything() {
		Iterator<Entry<String, PartnerInfo>> i = partner.entrySet().iterator();
		while(i.hasNext()) {
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
		log("Slaver joined.");
		btNet.matchIdThd(partnerCount, p.get("mac"));	//step 3, included informid
		String partnerId = String.valueOf(partnerCount);
		partner.put(partnerId, new PartnerInfo(partnerCount));
        partnerCount ++;

		partnerAvail.add(partnerId);
		Collections.sort(partnerAvail, new SortPartnerAvail());
		assigner.setKeyFragment(player.nowPlaying + partnerAvail.size());
		assigner.assignTask(partner.get(partnerId));
	}
	
	// 当设备与主机中断连接时
	void onDisconnected(InfoPack p) {
		log("Lost connection: #" + p.get("id"));
		assigner.onPartnerLost(p.get("id"));
		partner.remove(p.get("id"));
		partnerAvail.remove(p.get("id"));
	}
	
	// 当从机完成任务下载时
	void onSlaverTaskFinished( InfoPack info ) {
		String id = info.get("id");
		PartnerInfo p = partner.get(id);
		p.workStatus = 0;
		assigner.assignTask(p);
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
	
	int spdLim = 0;

	public void setSpdLim(int lim)
	{
		spdLim = lim;
		targetLim = lim;
	}
	
	public void setSpdLimGrad(int lim)
	{
		spdLim = lim;
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
		tmp.setSpeedLim(spdLim);
		tmp.setTag(httpDownTag++);
		tmp.downFile(url, location, this.onFileOutterDownloaded, false);
		httpDown.add(tmp);
		this.log("Task recv: "+ localHost.nowTask+", BR:"+videoInfo.get(localHost.nowTaskBit).bitrate/1000+"k");
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
		this.log("Downloading mpd...");
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
				loge(err);
				if(location.endsWith(".mpd"))
					loge("Failed to download mpd!");
				else
					loge(location+" failed!");
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
					loge("invalid mpd!");
					return;
				}
				localHost.mpdAcked = 1;
				n = videoInfo.get(0).url.length;
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
			log("Downloaded. spd:"+(speed/1024)+"KB/s");
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
		assigner.setKeyFragment(0);
		mpdUrl = url;
		Iterator<Entry<String, PartnerInfo>> partnerIter = partner.entrySet().iterator();
		while (partnerIter.hasNext()) {
			String partnerId = partnerIter.next().getKey();
			PartnerInfo p = partner.get(partnerId);
			int outSpeed = p.outBandWidth;
			if (outSpeed != 0) {
				partnerAvail.add(partnerId);
			}
		}
		Collections.sort(partnerAvail, new SortPartnerAvail());
		for (int i = partnerAvail.size() - 1; i >= 0; i --) {
			PartnerInfo p = partner.get(partnerAvail.get(i));
			assigner.assignTask(p);
		}
	}

	// 当从slaver那里(或者assigner!)收到键值对的时候 在这里分发到各个成员方法
	@Override
	public void work(Object param) {
		// 当蓝牙事件触发的时候
		InfoPack p = (InfoPack)param;
		String cmd = p.get("cmd");
		if (cmd.equals(Commands.bluetoothInfoReceived)) {
			p.put("cmd", p.get("cmd_"));
			work(p);
		}
		
		if (cmd.equals(Commands.connLost))
			this.onDisconnected(p);
		if (cmd.equals(Commands.slaverConnected))
			this.onNewDeviceConnected(p);
		if (cmd.equals(Commands.taskFinished)) {
			// 从外网 ( from externet )
			log("downloaded.");
		}
		if (cmd.equals(Commands.bluetoothReceived))	{ // 当已经接受(bluetoothReceived)
			String local =  p.get("path");
			log("BT received:" + local);
			PartnerInfo part = partner.get(p.get("id"));
			player.onNewTruncAvailable(part.transTask, local);
		}
		if (cmd.equals(Commands.notify))
			log(p.get("txt")); else
		if (cmd.equals(Commands.heartBeat))
			onHeartBeat(p);	else
		if (cmd.equals(Commands.taskFinished))
			onSlaverTaskFinished(p);	else
		if (cmd.equals(Commands.taskAssign))
			onTaskAssigned(p);	else
		if (cmd.equals(Commands.downloadMpd))
			downloadMpd(); else
		if (cmd.equals(Commands.transmitAcquire)) {
			int id = Integer.valueOf(p.get("id"));
			String path = p.get("location");
			log("Prepare BT receive:" + String.valueOf(id) + "@" + path);
			btNet.setSavePath(id, path);
			PartnerInfo part = partner.get(String.valueOf(id));
			part.transTask = p.getInt("no");
			part.transTaskBit = p.getInt("bitrate");
		}
	}
}
