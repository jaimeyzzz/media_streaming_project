package com.thuMediaLab.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thuMediaLab.common.commonInfo;
import com.thuMediaLab.common.commonInfo.chatInfo;
import com.thuMediaLab.demo.ContactList.eStatus;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ChatActivity extends Activity {

	SimpleAdapter adapter;
	ListView lst;
	TextView chatText;
	List<Map<String, Object>> tmp;
	Map<String, Object> map;
	ChatActivity me;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		me = this;

		chatText = (TextView)findViewById(R.id.tChatContent);
		chatText.setText("");
		setTitle("Talk with "+commonInfo.hisName);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		initLst();
		findViewById(R.id.btChatSend).setOnClickListener(this.onBtChatSendClicked);
		findViewById(R.id.btChatVideo).setOnClickListener(this.onBtChatVideoClicked);
	}
	
	OnClickListener onBtChatSendClicked = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			commonInfo.chatList.add(new commonInfo.chatInfo(0, chatText.getText().toString()));
			chatText.setText("");
			addToTmp(commonInfo.chatList.get(commonInfo.chatList.size()-1));
			adapter.notifyDataSetChanged();
		}
	};

	OnClickListener onBtChatVideoClicked = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			intent.setClass(me, VideoSelectionActivity.class);
			startActivityForResult(intent, 0);
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1)
		{
			Intent intent = new Intent();
			intent.setClass(me, PlayerActivity.class);
			startActivity(intent);
		}
	}
	
	// TODO 把聊天框改成一开始贴在上面的样子，不要第一句话就贴着下边
	
	void addToTmp(commonInfo.chatInfo p)
	{
		map = new HashMap<String, Object>();
		// TODO 太丑了……
		if(p.type==0)
			map.put("imgMe", commonInfo.myImg);
		else
			map.put("imgMe", android.R.color.transparent);
		if(p.type==1)
			map.put("imgHim", commonInfo.hisName);
		else
			map.put("imgHim", android.R.color.transparent);
		map.put("text", p.text);
		tmp.add(map);
	}
	
	void initLst()
	{
		lst = (ListView)findViewById(R.id.lstChat);
		tmp = new ArrayList<Map<String, Object>>();
		if(commonInfo.chatList.size()==0)
			commonInfo.chatList.add(new chatInfo(-1, "Now you two can talk."));
		for(int i =0; i < commonInfo.chatList.size();++i)
		{
			commonInfo.chatInfo p = commonInfo.chatList.get(i);
			addToTmp(p);
		}
		adapter = new SimpleAdapter(this, tmp, R.layout.listitem_chat, new String[]{"imgHim","text","imgMe"}, new int[]{R.id.chatImg1,R.id.chatText, R.id.chatImg2});
		lst.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chat, menu);
		return true;
	}

}
