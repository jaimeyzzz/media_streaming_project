package com.thuMediaLab.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class WelcomeActivity extends Activity {
	 /** Called when the activity is first created. */
	    private ViewPager mViewPager;
	    private WelcomeActivity me;
        //private PagerTitleStrip mPagerTitleStrip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		 mViewPager = (ViewPager)findViewById(R.id.viewPager);
		         //mPagerTitleStrip = (PagerTitleStrip)findViewById(R.id.pagertitle);
		         //将要分页显示的View装入数组中
		         LayoutInflater mLi = getLayoutInflater();
		         View view0 = mLi.inflate(R.layout.page0, null);
		         View view1 = mLi.inflate(R.layout.page1, null);
		         View view2 = mLi.inflate(R.layout.page2, null);
		         View view3 = mLi.inflate(R.layout.page3, null);
		         View view4 = mLi.inflate(R.layout.page4, null);
		         final ArrayList<View> views = new ArrayList<View>();
		         views.add(view0);
		         views.add(view1);
		         views.add(view2);
		         views.add(view3);
		         views.add(view4);
		         
		         //填充ViewPager的数据适配器
		         PagerAdapter mPagerAdapter = new PagerAdapter() {
		             //判断是否由对象生成界面
		        	 @Override
		             public boolean isViewFromObject(View arg0, Object arg1) {
		                 return arg0 == arg1;
		             }
		        	//获取当前窗体界面数
		             @Override
		             public int getCount() {
		                 return views.size();
		             }
		           //销毁position位置的界面
		             @Override
		             public void destroyItem(View container, int position, Object object) {
		                 ((ViewPager)container).removeView(views.get(position));
		             }
		           //初始化position位置的界面
		             @Override
		             public Object instantiateItem(View container, int position) {
		            	 ((ViewPager)container).addView(views.get(position));
		                 return views.get(position);
		             }
		         };
		         mViewPager.setAdapter(mPagerAdapter);		      
		         mViewPager.setOnPageChangeListener(new OnPageChangeListener() {		             
		             //页面选择   
		            public void onPageSelected(int position) {
		            	/* view从1到2滑动，2被加载后调用此方法*/		            	
		            }
		            public void onPageScrollStateChanged(int state) {
		            	//状态有三个:0空闲，1是正在滑行中，2目标加载完毕
		            }
     	            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
     	            	/*从1到2滑动，在1滑动前调用*/
     	            }
		        });
			}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_welcome, menu);
		return true;
	}
	
	// 当结束的时候调用这个函数。
	private void close()
	{
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}
	public void paly_login(View view) 
	{
	  close();
	}
	

}
