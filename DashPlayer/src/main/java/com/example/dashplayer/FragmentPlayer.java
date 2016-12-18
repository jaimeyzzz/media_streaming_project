package com.example.dashplayer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.FFmpegSurfaceView;
import com.appunite.ffmpeg.NotPlayingException;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.VideoInfo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.VideoView;

public class FragmentPlayer extends Fragment implements OnClickListener, FFmpegListener, OnEventListener {
	
	View view;
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        view = inflater.inflate(R.layout.frag_player, container, false);
	        return view;
	    }
	 @Override
	 public void onActivityCreated(Bundle bundle) {
		 super.onActivityCreated(bundle);
		 init();
	 }

	Button btPlayer;

	FragmentManager fragmentManager = this.getFragmentManager();
	int cnt = 0;
	int playWidth = 1200;
	int playHeight = 700;

	Handler mHandler;

	VideoInfo info;
	FFmpegPlayer[] mp = new FFmpegPlayer[2];
	FFmpegSurfaceView[] surfaceView = new FFmpegSurfaceView[2];
//	SurfaceHolder holder, holder2, playHolder;
	public int nowPlaying = -1;
	public String[] playLst;
	int tot;
	int[] finLst;	// 记录已经完成的列表
	int orix = 0,oriy = 0;
	int status = -1;
	ArrayDeque<Integer> availableTruncQueue;
	Timer tUpdate = new Timer();

    public Handler evtHandler;
	/*
	 * -1	: 没有播放任务
	 * 0	: 没有在播放
	 * 1	: 正在播放
	 */

    int lastPlayTime = -10;
	void init() {
		btPlayer = (Button) view.findViewById(R.id.btPlay_);
		surfaceView[0] = (FFmpegSurfaceView)view.findViewById(R.id.surfaceView0);
		surfaceView[1] = (FFmpegSurfaceView)view.findViewById(R.id.surfaceView1);
		new Thread(new Runnable()
		{
		      public void run() {
		          Looper.prepare();
		          evtHandler = new Handler() {
		              public void handleMessage(Message msg) {
		            	  switch(msg.what)
		            	  {
		            	  case 1:	// 新的trunc下载完成
		            		  onNewTruncAvailable(msg.arg1);
		            		  break;
		            	  }
		              }
		          };
		          Looper.loop();
		      }
		  }).start();

		btPlayer.setOnClickListener(this);
		for(int i=0;i<2;++i)
		{
			mp[i] = new FFmpegPlayer((FFmpegDisplay) surfaceView[i], getActivity());
			mp[i].setMpegListener(this);
			mp[i].mIsFinished = true;
		}
		tUpdate.schedule(new TimerTask(){
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				int nowPlay = getNowPlayTime();
				if(lastPlayTime==nowPlay)
				{
					mp[0].mIsFinished = mp[1].mIsFinished = true;
					if(nowPlaying >=0 && nowPlaying<n && finLst[nowPlaying]>=1)
						continuePlay();
				}
				lastPlayTime = nowPlay;
			}}, 0, 2000);
	}
	
	public void play_test()
	{
		initPlayer();
		playLst = initTestLst();
		this.onNewTruncAvailable(0);
	}

	int switchDelay = 10;
	int n = 50;
	public String[] initTestLst() {
		String[] ret = new String[n];
		finLst = new int[n];
		for (int i = 0; i < n; ++i)
		{
//			ret[i] = "/storage/sdcard0/sample/despicable_me_ii/Despicable_Me_II_"+i+"_.mp4";
//			ret[i] = "/storage/sdcard0/sample/3gp/a" + i + ".3gp";
			ret[i] = "/storage/sdcard0/sample/sintel/400k/sintel_2048_" + (i+1) + ".mp4";
//			ret[i] = "/storage/sdcard0/sample/sintel_ts/tmp" + i + ".ts";
//			ret[i] = "/storage/sdcard0/sample/test/Despicable_Me_II_"+i+".mp4";
			finLst[i] = 1;
		}
		cnt = 0;
		info = new VideoInfo();
		info.duration = n*10;
		return ret;
	}
	
	public int getBufferedLength()
	{
		int en = 0;
		for(int i=nowPlaying;i<n;++i)
			if(finLst[i]==1)
				en = i;
			else
				break;
		en = (en+1)*info.get(0).fragmentDuration;
		int now = getNowPlayTime();
		return en-now;
	}
	
	public int getNowPlayTime()
	{
		if(nowPlaying<0)
			return 0;
		int tmp = (int)(mp[nowPlaying%2].mCurrentTimeUs/1000/1000);
		return duration*nowPlaying + tmp;
	}
	
	public int getTotPlayTime()
	{
		if(info == null)
			return -1;
		return info.duration;
	}

	void hideSurface(final SurfaceView p)
	{
		this.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				p.setLayoutParams(new AbsoluteLayout.LayoutParams(1,1,orix,oriy));
			}
		});
	}
	void showSurface(final SurfaceView p)
	{
		this.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				p.setLayoutParams(new AbsoluteLayout.LayoutParams(playWidth,playHeight,orix,oriy));
			}
		});
	}
	
	void setSize()
	{
		int width = view.getWidth();
		int height = view.getHeight();
		int w = info.get(0).width;
		int h = info.get(0).height;
		if(width*h>w*height)
		{
			// 左右留黑边 leave black margin left-right
			playHeight = height;
			playWidth = (int)((float)w/h*height);
		}else
		{
			// 上下留黑边 leave black margin top-bottom
			playWidth = width;
			playHeight = (int)((float)h/w*width);
		}
		orix = (width-playWidth)/2;
		oriy = (height-playHeight)/2;
	}


	private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
	private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;

	private void setDataSource(final FFmpegPlayer mp,final String src) {
		mp.mIsFinished = false;
		HashMap<String, String> params = new HashMap<String, String>();
		
		// set font for ass
		File assFont = new File(Environment.getExternalStorageDirectory(),
				"DroidSansFallback.ttf");
		params.put("ass_default_font_path", assFont.getAbsolutePath());

		mp.setDataSource(src, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
				mSubtitleStreamNo);

	}

	private static String getSDCardFile(String file) {
		File videoFile = new File(Environment.getExternalStorageDirectory(),
				file);
		return "file://" + videoFile.getAbsolutePath();
	}

	@SuppressWarnings("deprecation")
	public void initPlayer() {
		// 清空之前的数据
		try {
			cnt = 0;
			showSurface(surfaceView[0]);
			showSurface(surfaceView[1]);
			surfaceView[0].bringToFront();
			nowPlaying = 0;
			status = 0;
			playLst = new String[n];
			finLst = new int[n];
			availableTruncQueue = new ArrayDeque<Integer>();
		} catch (Exception ex) {
			Log.d("initPlayer", ex.getMessage());
		}
	}
	
	int duration;
	
	public void setInfo(VideoInfo _info)
	{
		info = _info;
		duration = info.get(0).fragmentDuration;
		n = info.get(0).url.length;
		setSize();
		initPlayer();
	}
	
	@Override
	public void onClick(View arg0) {
		play_test();
	}

	@Override
	public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams) {
	}

	@Override
	public void onFFResume(NotPlayingException result) {
		ffStat[nowPlaying%2] = 1;
	}

	@Override
	public void onFFPause(NotPlayingException err) {
		// TODO Auto-generated method stub
		
	}
	
	int[] ffStat = new int[2];

	@Override
	public void onFFStop() {
		ffStat[nowPlaying%2] = 0;
	}

	long lastUpdTime = 0;
	
	@Override
	public void onFFUpdateTime(long mCurrentTimeUs, long mVideoDurationUs, boolean isFinished) 
	{
		lastUpdTime = System.currentTimeMillis();
		if(!isFinished)
			return;
		mp[nowPlaying%2].mIsFinished = true;
		mp[(nowPlaying+1)%2].mIsFinished = true;
		nowPlaying++;
		if( nowPlaying==n )
		{
			status = -1;
			return;
		}
		if(finLst[nowPlaying]==0)
		{
			status = 0;
			return;
		}
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
    			continuePlay();
            }
        });
	}
	
	public void continuePlay()
	{
		mp[nowPlaying%2].mCurrentTimeUs = 0;
		mp[ nowPlaying%2 ].resume();
		mp[ nowPlaying%2 ].mIsFinished = false;
		if(nowPlaying%2==0)
		{
//			hideSurface(surfaceView[0]);
			showSurface(surfaceView[0]);
			hideSurface(surfaceView[1]);
		}
		else
		{
//			hideSurface(surfaceView[1]);
			showSurface(surfaceView[1]);
			hideSurface(surfaceView[0]);
		}
		if(cnt<n && finLst[cnt]==1)
			setDataSource(mp[(nowPlaying+1)%2],playLst[cnt++]);
	}

	@Override
	public void onFFSeeked(NotPlayingException result) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void work(Object param) {
		// TODO Auto-generated method stub
		
	}
	
	public void onNewTruncAvailable(int no, String location)
	{
		playLst[no] = location;
		this.onNewTruncAvailable(no);
	}
	
	int getStatus()
	{
		if(status == -1)
			return -1;
		if(!mp[nowPlaying%2].mIsFinished)
			status = 1;
		else
			status = 0;
		return status;
	}
	
	public void onNewTruncAvailable(int no)
	{
		status = getStatus();
		finLst[no] = 1;
		switch(status)
		{
		case -1:return;
		case 0:
			if( no == nowPlaying)
			{
				status = 1;
				setDataSource(mp[nowPlaying%2],playLst[cnt++]);
				continuePlay();
			}
			/*
			else
			if(finLst[nowPlaying] != 0)
			{
				setDataSource(mp[nowPlaying%2],playLst[nowPlaying]);
				continuePlay();
			}
			*/
			break;
		case 1:
			if( no == nowPlaying+1 )
				setDataSource(mp[(nowPlaying+1)%2],playLst[cnt++]);
			break;
		}
	}
	
	public void setPlayList(String[] _lst)
	{
		playLst = _lst;
	}
	
	public boolean isPlaying()
	{
		return status != -1;
	}
	
}
