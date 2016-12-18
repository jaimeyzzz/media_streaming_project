package com.example.dashplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dashplayer.common.LoggerTextviewMatlab;
import com.example.dashplayer.common.OnEventListener;
import com.example.dashplayer.common.func;
import com.example.dashplayer.networkController.MasterController;
import com.example.dashplayer.networkController.MasterControllerDumb;
import com.example.dashplayer.networkController.SlaverController;
import com.example.dashplayer.networkController.SlaverControllerDumb;
import com.example.dashplayer.networkController.TestController;

public class MainActivity extends Activity {
	
	FragmentPlayer fragmentPlayer;
	TestController control;
	MasterController master;
	SlaverController slaver;
	public static MainActivity me;
	TextView tLog;
	Timer tProgress;
	SeekBar progress;
	LoggerTextviewMatlab logger;
	Button m_btClient;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        me = this;
        func.bindActivity(this);
        tLog = (TextView)this.findViewById(R.id.tLog);
        progress = (SeekBar)this.findViewById(R.id.pPlayProgress);
        logger = new LoggerTextviewMatlab(this, tLog);
        m_btClient = (Button)this.findViewById(R.id.m_btClient);
        fragmentPlayer = (FragmentPlayer)getFragmentManager().findFragmentById(R.id.fragPlayer);
        final ImageButton btPlay = (ImageButton)this.findViewById(R.id.btPlay);
        /*
        master = new MasterController(fragmentPlayer);
        master.bindOnLogEvent(logger);
        slaver = new SlaverController();
        slaver.bindOnLogEvent(logger);
        */
        btPlay.setEnabled(false);

        this.findViewById(R.id.m_btMaster).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
		        master = new MasterController(fragmentPlayer, me);
		        master.bindOnLogEvent(logger);
				btPlay.setEnabled(true);
			}
        });
        
        m_btClient.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				btPlay.setEnabled(false);
		        slaver = new SlaverController();
		        slaver.bindOnLogEvent(logger);
				slaver.connectMaster();
			}
        });
        
        btPlay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p) {
				m_btClient.setEnabled(false);
//				control.playTest();
				if(tProgress!=null)
				{
					tProgress.cancel();
					tProgress.purge();
				}
		        tProgress = new Timer();
				master.play("http://192.168.1.222/medialab/sintel/test.mpd");
//		        fragmentPlayer.play_test();
				btPlay.setImageResource(android.R.drawable.ic_media_pause);
		        tProgress.schedule(new TimerTask(){
					@Override
					public void run() {
						final int now = fragmentPlayer.getNowPlayTime();
						final int tot = fragmentPlayer.getTotPlayTime();
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
			}
        });
        logger.work("初始化完成");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
