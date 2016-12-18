package com.example.dashplayer.networkController;

import java.io.File;
import java.util.ArrayList;

import android.os.Message;
import android.util.Log;

import com.example.dashplayer.FragmentPlayer;
import com.example.dashplayer.XmlParser;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.VideoFragmentsInfo;
import com.example.dashplayer.common.VideoInfo;
import com.example.dashplayer.common.func;
import com.example.dashplayer.common.network.HttpDownloadModule;

/*
 *  外部非公共依赖：
		FragmentPlayer player
 */

public class TestController {

	FragmentPlayer player;
	HttpDownloadModule httpDown = new HttpDownloadModule();
	XmlParser xmlParser = new XmlParser();
	VideoInfo videoInfo;
	int[] testBit = new int[]{0,1,0,0,1,0};
	int downloading = -1;
	int n;
	
	public TestController(FragmentPlayer _player)
	{	// 需要绑定一个fragmentplayer
		player = _player;
	}
	
	public void playTest()
	{
		player.play_test();
	}
	
	public void play(final String url)
	{
		new Thread(new Runnable(){
			@Override
			public void run() {
				func.createPath("/storage/sdcard0/tmp");
				httpDown.downFile(url, "/storage/sdcard0/tmp/tmp.mpd", work,true);
			}
		}).start();
	}
	
	OnEventListener work = new OnEventListener()
	{
		@Override
		public void work(Object param) {
			videoInfo = xmlParser.parseXmlFromFile("/storage/sdcard0/tmp/tmp.mpd");
			player.setInfo(videoInfo);
			n = videoInfo.get(0).url.length;
			for( downloading = 0; downloading<n; ++downloading)
			{	
				final String local = "/storage/sdcard0/tmp/"+downloading+".mp4";
				player.playLst[downloading] = local;
				int br = getBitrate();
				final int i = downloading;
				httpDown.downFile(videoInfo.get(br).url[downloading], local , new OnEventListener()
				{
					// 当第$downloading个视频下载完成时
					@Override
					public void work(Object param) {
						player.onNewTruncAvailable(i);
						/*
						Message tmp = new Message();
						tmp.arg1 = i;
						tmp.what =1 ;
						player.evtHandler.sendMessage(tmp);
						*/
					}	
				},true);
			}
		}
	};
	
	public int getBitrate()
	{
		return testBit[downloading];
	}
	
	
}
