package com.thuMediaLab.demo;

import com.thuMediaLab.common.commonInfo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class LoginActivity extends Activity {

	private LoginActivity me;
	private GameViewOK gameViewOk=null;
	SurfaceView surface;     
    SurfaceHolder surfaceHolder;   
	// TODO 区分客户端登陆和服务器登录
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		me = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.findViewById(R.id.btLogin).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loginAsServer();
				Intent intent = new Intent();
				intent.setClass(me, MainActivity.class);
				startActivity(intent);
				me.finish();
			}});		
		this.findViewById(R.id.btSignup).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loginAsClient();
				Intent intent = new Intent();
				intent.setClass(me, MainActivity.class);
				startActivity(intent);
				me.finish();
			}
		});
	}
	
	public void loginAsServer()
	{
		commonInfo.type = 0;
		commonInfo.myName = "Gerald Zhang";
		commonInfo.myImg = R.drawable.neku;
		commonInfo.hisName = "Chao Li";
		commonInfo.hisImg = R.drawable.h_lichao;
	}

	public void loginAsClient()
	{
		commonInfo.type = 1;
		commonInfo.myName = "Chao Li";
		commonInfo.myImg = R.drawable.h_lichao;
		commonInfo.hisName = "Gerald Zhang";
		commonInfo.hisImg = R.drawable.neku;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

}
