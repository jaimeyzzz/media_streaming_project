package com.thuMediaLab.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.example.dashplayer.common.LoggerTextviewMatlab;
import com.example.dashplayer.common.PartnerInfo;
import com.example.dashplayer.networkController.SlaverController;
import com.thuMediaLab.common.VideoExpandableListAdapter;
import com.thuMediaLab.common.commonInfo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class ContactList extends Fragment {

	public ExpandableListView lst;
	SimpleAdapter adapter;
	ContactList me = this;
	VideoExpandableListAdapter adapterEX;
	enum eStatus
	{
		notChatting(0), isChatting(1);
		private int state;
		eStatus(int _state)	{state = _state;}
	}
	eStatus status = eStatus.notChatting;
	public List<List<Map<String,Object>>> childData;
	public List<Map<String, Object>> listData;
	AlertDialog dlgJoin;
	SlaverController slaver;
	LoggerTextviewMatlab logger;
	TextView tLog;
	Timer tProgress = new Timer();
	
	public OnClickListener onJoinClicked = new OnClickListener(){
		public void onClick(View arg0) {
			startSlaver();
		}};
		
	void startSlaver()
	{
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup tmp = (ViewGroup)getActivity().findViewById(R.id.dialog_join);
		final View view = inflater.inflate(R.layout.dialog_join, null);
		dlgJoin = new AlertDialog.Builder(getActivity()).setView(view).setCancelable(false).show();
        logger = new LoggerTextviewMatlab(this.getActivity(), tLog);
        slaver = new SlaverController();
        slaver.bindOnLogEvent(logger);
        slaver.setLimSpd(15);
		slaver.connectMaster();
		tProgress.schedule(new TimerTask(){
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run() {
						PartnerInfo info = slaver.getInfo();
						TextView tDownloading = (TextView)view.findViewById(R.id.tDownloading);
						TextView tTransmitting = (TextView)view.findViewById(R.id.tTransmitting);
						tDownloading.setText("#"+String.valueOf(info.nowTask));
						tTransmitting.setText("#"+String.valueOf(info.transTask));
					}});
			}} ,0 ,1000);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// 修改返回的View来修改tab显示的tab
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_contact_list, null);
		lst = (ExpandableListView)view.findViewById(R.id.lstView_Contact);
		lst.setGroupIndicator(getResources().getDrawable(R.drawable.hide_groupindicator));
		lst.setOnGroupExpandListener(new OnGroupExpandListener(){
			@Override
			public void onGroupExpand(int index) {
				for(int i =0;i<listData.size();++i)
					if(i!=index)
						lst.collapseGroup(i);
//				adapterEX.actExpandGroup(index);
			}});
		lst.setOnGroupCollapseListener(new OnGroupCollapseListener(){
			@Override
			public void onGroupCollapse(int index) {
				/*
				if(adapterEX.scaleType[index]==-2)	// 是真要collapse
					return;
					*/
				/*
				lst.expandGroup(index);
				adapterEX.actCollapseGroup(index);
				*/
			}});
		// 请姑且认为ImgStatus是表示是否上线的图片的名字。我也想不出更好的名字了
		// TODO 另外，请去掉ImgStatus的白色背景
		childData = getChildData();
		listData = getData();
		commonInfo.initData();
		/*
		adapterEX = new SimpleExpandableListAdapter(this.getActivity(), listData, R.layout.listitem_contact, new String[]{"itemImgStatus","itemImg","itemName","itemStatus","itemInfo"},
															new int[]{R.id.itemImgStatus,R.id.itemImg,R.id.itemName, R.id.itemStatus, R.id.itemInfo},
															childData, R.layout.listitem_childvideo, new String[]{"img"}, new int[]{R.id.childVideoItem});
		*/
		adapterEX = new VideoExpandableListAdapter(this);
		lst.setAdapter(adapterEX);
		/*
		// 原先的马赛克推荐墙方法
		adapter = new SimpleAdapter(this.getActivity(), getData(), R.layout.listitem_contact, new String[]{"itemImgStatus","itemImg","itemName","itemStatus","itemInfo"}, new int[]{R.id.itemImgStatus,R.id.itemImg,R.id.itemName, R.id.itemStatus, R.id.itemInfo});
		lst.setAdapter(adapter);
		lst.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	        {
				if(position==0)
					startChat();
	        }
	    });
	    */
		tLog = new TextView(this.getActivity());
		return view;
	}

	void startChat()
	{
		if(status == eStatus.isChatting)
			return;
		status = eStatus.isChatting;
		Intent intent = new Intent();
		intent.setClass(this.getActivity(), VideoSelectionActivity.class);
		startActivityForResult(intent, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		status = eStatus.notChatting;
		this.getActivity().setTitle(commonInfo.myName);
		this.getActivity().getActionBar().setIcon(commonInfo.myImg);
	}
	
	public void videoSelected(int id)
	{
		commonInfo.showVideoDetailDialog(this.getActivity(), id);
	}
	
	List<List<Map<String, Object>>> getChildData()
	{
		List<List<Map<String, Object>>> dat = new ArrayList<List<Map<String, Object>>>();
		List<Map<String, Object>> tmpLst;
		Random rand = new Random();
		for(int i = 0;i<8; ++i)
		{
			tmpLst = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> tmp;
			int num = 10;
			int[] ord = new int[num];
			for(int j = 0;j<num;++j)
				ord[j] = j;
			for(int j = 0;j<num;++j)
			{
				int a = rand.nextInt(num);
				int b = rand.nextInt(num);
				int tmq = ord[a];
				ord[a] = ord[b];
				ord[b] = tmq;
			}
			for(int j = 0;j<num;++j)
			{
				int offset = ord[j];
				tmp = new HashMap<String, Object>();
				tmp.put("img", R.drawable.video0+offset);
				tmp.put("video", offset);
				tmpLst.add(tmp);
			}
			num = 10;
			ord = new int[num];
			for(int j = 0;j<num;++j)
				ord[j] = j;
			for(int j = 0;j<num;++j)
			{
				int a = rand.nextInt(num);
				int b = rand.nextInt(num);
				int tmq = ord[a];
				ord[a] = ord[b];
				ord[b] = tmq;
			}
			for(int j = 0;j<num;++j)
			{
				int offset = ord[j];
				tmp = new HashMap<String, Object>();
				tmp.put("recommendation", 1);
				tmp.put("img", R.drawable.video0+offset);
				tmp.put("video", offset);
				tmpLst.add(tmp);
			}
			dat.add(tmpLst);
		}
		return dat;
	}
	
	List<Map<String, Object>> getData()
	{
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map = new HashMap<String, Object>();
		if(commonInfo.type == 0)
		{
			map.put("itemImgStatus", R.drawable.online);
			map.put("itemImg", R.drawable.h_lichao);
			map.put("itemName", "Chao Li");
			map.put("itemStatus", "I'm online. Anything interesting?");
			map.put("itemInfo", "Dec. 9, 2013");
		}else
		{
			map.put("itemImgStatus", R.drawable.online);
			map.put("itemImg", R.drawable.neku);
			map.put("itemName", "Gerald Zhang");
			map.put("itemStatus", "Available.");
			map.put("itemInfo", "Dec. 9, 2013");
		}
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_lifeng);
		map.put("itemName", "Lifeng Sun");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "one hour ago");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_yinxing);
		map.put("itemName", "Yinxing Hou");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "ten days ago");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_jinshu);
		map.put("itemName", "Jinshu Mao");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "one month ago");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_linjia);
		map.put("itemName", "Linjia Li");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "more than one year");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_hongyin);
		map.put("itemName", "Hongyin Luo");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "more than one year");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_hanxuan);
		map.put("itemName", "Hanxuan Yu");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "more than one year");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("itemImgStatus", R.drawable.offline);
		map.put("itemImg", R.drawable.h_mingxing);
		map.put("itemName", "Mingxing Xu");
		map.put("itemStatus", "Offline.");
		map.put("itemInfo", "more than one year");
		ret.add(map);
		
		return ret;
	}

}
