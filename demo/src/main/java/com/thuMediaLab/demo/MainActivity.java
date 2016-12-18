package com.thuMediaLab.demo;

import com.thuMediaLab.common.commonInfo;

import android.os.Bundle;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private FragmentTabHost mTabHost;

	private Class fragmentArray[] = { ContactList.class, DumbActivity.class, DumbActivity.class, DumbActivity.class, DumbActivity.class };  
    private int iconArray[] = { R.drawable.tabbar_home, R.drawable.tabbar_profile, R.drawable.tabbar_discover, R.drawable.tabbar_message_center, R.drawable.tabbar_more };  
    private String titleArray[] = { "Home", "User", "Search", "Message", "More" };  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupView();
		setTitle(commonInfo.myName);
		getActionBar().setIcon(commonInfo.myImg);
	}

	private void setupView() {
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		mTabHost.getTabWidget().setDividerDrawable(null);

		int count = fragmentArray.length;
//		int count = 1;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(titleArray[i]).setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.home_btn_bg);
		}

	}

	private View getTabItemView(int index) {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View view = layoutInflater.inflate(R.layout.view_idle_bottom_navi, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.iv_icon);
		imageView.setImageResource(iconArray[index]);

		TextView textView = (TextView) view.findViewById(R.id.tv_icon);
		textView.setText(titleArray[index]);

		return view;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
