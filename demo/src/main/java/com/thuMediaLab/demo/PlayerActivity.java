package com.thuMediaLab.demo;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.example.dashplayer.FragmentPlayer;
import com.example.dashplayer.common.LoggerTextviewMatlab;
import com.example.dashplayer.common.func;
import com.example.dashplayer.networkController.MasterController;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Pair;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayerActivity extends Activity {
	
	Timer timProcess;
	Random rand = new Random();
	static int barMargin = 1;
	static int barWidth = 5;
	static int maxBandwidth = 1500000;
	static int maxBatteryLevel = 1500000;
	Queue<BandwidthRecord> speedRecord = new ArrayDeque<BandwidthRecord>();
	Queue<Integer> batteryRecord = new ArrayDeque<Integer>();
	SurfaceView plotBB;
	SurfaceView plotB;
	SurfaceHolder holderBB, holderB;
	FragmentPlayer fragmentPlayer;
	Timer tProgress;
	Timer tSlowdown = new Timer();
	int last = 0;
	
	class BandwidthRecord
	{
		public int bandwidth, bandwidth2, bitrate;
		public BandwidthRecord(int a,int b, int c)
		{
			bandwidth = a;
			bandwidth2 = b;
			bitrate = c;
		}
	};
	
	void plotBandwidthBitrate(int bandwidth, int bitrate, int bandwidth2)
	{
		int trueWidth = plotBB.getWidth();
		int width = trueWidth - 50;
		int height = plotBB.getHeight();
		speedRecord.add(new BandwidthRecord(bandwidth, bandwidth2, bitrate));
		int siz = speedRecord.size();
		int offset = siz*barWidth>width?siz*barWidth-width:0;
		
		Iterator<BandwidthRecord> i = speedRecord.iterator();
		int cnt = 0;
		Canvas g = holderBB.lockCanvas();
		if(g == null)
			return;
		Paint pnt = new Paint();
		pnt.setColor(Color.BLACK);
		g.drawRect(0, 0, trueWidth, height, pnt);
		pnt.setColor(Color.rgb(0, 160, 232));
		Paint pntBitrate = new Paint();
		pntBitrate.setColor(Color.RED);
		pntBitrate.setStrokeWidth(3);
		Paint pntBandwidth2 = new Paint();
		pntBandwidth2.setColor(Color.CYAN);
		Paint pntGrid = new Paint();
		pntGrid.setColor(Color.GRAY);
		pntGrid.setStrokeWidth(1);
		pntGrid.setStyle(Style.STROKE);
		pntGrid.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		Paint pntText = new Paint();
		pntText.setColor(Color.WHITE);
		pntText.setTextSize(20);

		//Draw grid
		int gridWLen = width/5;
		for(int gridx = -offset%gridWLen;gridx<width;gridx+= gridWLen)
			g.drawLine(gridx, 0, gridx, height, pntGrid);
		int gridHei = 500000;
		for(int gridy = gridHei;gridy <=maxBandwidth;gridy += gridHei)
		{
			int y = (maxBandwidth - gridy)*height/maxBandwidth;
			g.drawLine(0, y, width, y, pntGrid);
			g.drawText(String.valueOf(gridy/1000), width, y, pntText);
		}
		
		while(i.hasNext())
		{
			BandwidthRecord p = i.next();
			int x = cnt*barWidth - offset;
			cnt++;
			if(x + barWidth<0)
				continue;
			int y = (maxBandwidth - p.bandwidth*8)*height/maxBandwidth;
			g.drawRect(x+barMargin, y, x+barWidth, height, pnt);
			int y2 = (maxBandwidth - (p.bandwidth+p.bandwidth2)*8)*height/maxBandwidth;
			g.drawRect(x+barMargin, y2, x+barWidth, y, pntBandwidth2);
			y = (maxBandwidth - p.bitrate)*height/maxBandwidth;
			/*
			if(last!=null)
			{
				int yy = (maxBandwidth - last.bitrate)*height/maxBandwidth;
				g.drawLine(x - barWidth/2, yy, x + barWidth/2, y, pntBitrate);
			}
			last = p;
			*/
		}
		holderBB.unlockCanvasAndPost(g);
	}
	
	void plotBatteryLevel(int battery)
	{
		// 虽然它名字叫做Battery Level，但是它的心已经是bitrate的心了
		int trueWidth = plotBB.getWidth();
		int width = trueWidth - 50;
		int height = plotB.getHeight();
		batteryRecord.add(battery);
		int siz = batteryRecord.size();
		int offset = siz*barWidth>width?siz*barWidth-width:0;
		Iterator<Integer> i = batteryRecord.iterator();
		int cnt = 0;
		Canvas g = holderB.lockCanvas();
		if(g == null)
			return;
		Paint pnt = new Paint();
		pnt.setColor(Color.BLACK);
		pnt.setStrokeWidth(5);
		g.drawRect(0, 0, trueWidth, height, pnt);
		pnt.setColor(Color.YELLOW);
		Paint pntGrid = new Paint();
		pntGrid.setColor(Color.GRAY);
		pntGrid.setStrokeWidth(1);
		pntGrid.setStyle(Style.STROKE);
		pntGrid.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		pntGrid.setTextSize(20);
		Paint pntText = new Paint();
		pntText.setColor(Color.WHITE);
		pntText.setTextSize(20);

		//Draw grid
		int gridWLen = width/5;
		for(int gridx = -offset%gridWLen;gridx<width;gridx+= gridWLen)
			g.drawLine(gridx, 0, gridx, height, pntGrid);
		int gridHei = 500000;
		for(int gridy = gridHei;gridy <=maxBatteryLevel;gridy += gridHei)
		{
			int y = (maxBatteryLevel - gridy)*height/maxBatteryLevel;
			g.drawLine(0, y, width, y, pntGrid);
			g.drawText(String.valueOf(gridy/1000), width, y, pntText);
		}
		
		last = 0;
		while(i.hasNext())
		{
			Integer p = i.next();
			int x = cnt*barWidth - offset;
			cnt++;
			int y = (maxBatteryLevel - p)*height/maxBatteryLevel;
			//g.drawRect(x+barMargin, y, x+barWidth, height, pnt);
			int yy = (maxBatteryLevel - last)*height/maxBatteryLevel;
			last = p;
			if(x + barWidth<0)
				continue;
			g.drawLine(x - barWidth/2, yy, x + barWidth/2, y, pnt);
		}
		holderB.unlockCanvasAndPost(g);
	}
	
	TextView tLog;
	ProgressBar progress;
	MasterController master;
	LoggerTextviewMatlab logger;
	PlayerActivity me;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);		
		me = this;
		
        func.bindActivity(this);
        tLog = (TextView)this.findViewById(R.id.tLog);
        progress = (ProgressBar)this.findViewById(R.id.playerProgress);
        fragmentPlayer = (FragmentPlayer)getFragmentManager().findFragmentById(R.id.fragPlayer);
        logger = new LoggerTextviewMatlab(this, tLog);
        master = new MasterController(fragmentPlayer, me);
        master.bindOnLogEvent(logger);
        //master.setSpdLim(15);
		master.setSpdLim(15); // ljm add;
		
		plotBB = (SurfaceView) this.findViewById(R.id.playerSpeedPlot);
		plotB = (SurfaceView) this.findViewById(R.id.playerBatteryPlot);
		holderBB = plotBB.getHolder();
		holderB = plotB.getHolder();
		/*
		timProcess = new Timer();
		timProcess.schedule(new TimerTask(){
			@Override
			public void run() {
				plotBandwidthBitrate(rand.nextInt(1500), rand.nextInt(1500));
				plotBatteryLevel(rand.nextInt(100));
			}}, 1000, 100);
		*/
		startPlay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_player, menu);
		return true;
	}
	
	void startPlay()
	{
		if(tProgress!=null)
		{
			tProgress.cancel();
			tProgress.purge();
		}
	    tProgress = new Timer();
		master.play("http://114.215.41.77/sintel/test.mpd");
	    tProgress.schedule(new TimerTask(){
			@Override
			public void run() {
				final int now = fragmentPlayer.getNowPlayTime();
				final int tot = fragmentPlayer.getTotPlayTime();
				final int bitrate = master.getNowBitrate();
				final int bandwidth = master.getBandwidth();
				final int bandwidth2 = master.getBandwidth2();
				final int batteryLevel = master.getBatteryLevel();
				plotBandwidthBitrate(bandwidth, bitrate, bandwidth2);
				plotBatteryLevel(bitrate);
				if(tot==-1)
					return;
				final int buf = fragmentPlayer.getBufferedLength();
				me.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						progress.setMax(tot);
						progress.setProgress(now);
						progress.setSecondaryProgress(buf + now);
					}
				});
			}}, 0, 1000);

		/*tSlowdown.schedule(new TimerTask(){
			@Override
			public void run() {
				master.setSpdLim(50);
			}}, 60000);*/
	}
}