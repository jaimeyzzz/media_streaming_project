package com.thuMediaLab.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.thuMediaLab.demo.ContactList;
import com.thuMediaLab.demo.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class VideoExpandableListAdapter extends BaseExpandableListAdapter {

	ContactList fa;
	List<List<Map<String, Object>>> childData;
	List<Map<String, Object>> groupData;
	public View[] arrView;
	public int[] scaleRatio; 
	public int[] scaleType;
	int oriHeight = -300;
	Timer timer = new Timer();
	
	Activity activity;
	private LayoutInflater mLayoutInflater; 
	
	Integer[] videoRes = new Integer[]{	R.drawable.video0,R.drawable.video1,R.drawable.video2,
										R.drawable.video3,R.drawable.video4,R.drawable.video5,
										R.drawable.video6,R.drawable.video7,R.drawable.video8,
										R.drawable.video9};
	Bitmap[] videoImg = new Bitmap[commonInfo.videoNum];
	
	void initVideoImg()
	{
		for(int i =0 ;i<commonInfo.videoNum;++i)
		{
			videoImg[i] = BitmapFactory.decodeResource(fa.getResources(), videoRes[i]);
			videoImg[i] = Bitmap.createScaledBitmap(videoImg[i], 100, 150, true);
		}
	}
	
	public VideoExpandableListAdapter(ContactList _fa)
	{
		fa = _fa;
		childData = fa.childData;
		groupData = fa.listData;
		activity = fa.getActivity();
		arrView = new View[groupData.size()];
		scaleRatio = new int[groupData.size()];
		scaleType = new int[groupData.size()];
		mLayoutInflater = LayoutInflater.from(activity); 
		initVideoImg();
		for(int i =0;i<groupData.size();++i)
			scaleRatio[i] = oriHeight;
		/*
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				fa.getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run() {
						for(int i = 0;i<groupData.size();++i)
							if(arrView[i]!=null)
							{
								doAnimate(i);
							}
					}});
			}}, 0, 500);
		*/
	}
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition);
	}

	@Override
	public long getChildId(int group, int child) {
		return child;
	}

	@Override
	public View getChildView(int group, int child, boolean isLastChild, View convertView, ViewGroup parent) {
		/*
		ChildViewHolder childViewHolder;  
        View view = convertView;  
        int i1 = getChildType(group, child);  
        if (view == null) {  
            childViewHolder = new ChildViewHolder();  
            view = mLayoutInflater.inflate(R.layout.listitem_childvideo, null);  
            view.setTag(childViewHolder);  
        }else {  
            childViewHolder = (ChildViewHolder)view.getTag();  
        }  
        if ( i1 != 0) {  
            return mLayoutInflater.inflate(R.layout.list_item_card_detail_foot, null);  
        } 
        */
		View view = convertView;  
//        if (view == null) 
        {    
            view = mLayoutInflater.inflate(R.layout.listitem_childvideo, null);
            arrView[group] = view;
        }   
        String myName = (String)fa.listData.get(group).get("itemName");
        TextView recnPlayed = (TextView)view.findViewById(R.id.tRecentView);
        recnPlayed.setText(myName+"'s video playing history");
        LinearLayout container = (LinearLayout)view.findViewById(R.id.childVideoContainter0);
        LinearLayout containerRecm = (LinearLayout)view.findViewById(R.id.childVideoContainter1);
        List<Map<String, Object>> dat = childData.get(group);
        int now = 0;
        for(Iterator<Map<String, Object>> i = dat.iterator();i.hasNext();now++)
        {
        	Map<String, Object> p = i.next();
        	ImageView img = new ImageView(fa.getActivity());
        	LinearLayout border = new LinearLayout(fa.getActivity());
        	if(p.containsKey("recommendation"))
        	{
	        	img.setImageResource(videoRes[(Integer)p.get("video")]);
            	border.addView(img);
            	containerRecm.addView(border);
        	}else
        	{
	        	img.setImageResource(videoRes[(Integer)p.get("video")]);
	        	border.addView(img);
	        	container.addView(border);
        	}
//        	img.setImageBitmap(videoImg[(Integer)p.get("video")]);
        	img.setScaleType(ScaleType.CENTER_CROP);
        	border.setBackgroundResource(R.drawable.videolist_itemborder);
        	border.setPadding(8, 8, 8, 8);
        	LayoutParams params = new LayoutParams(300, 400);
        	params.setMargins(20, 0, 20, 20);
        	border.setLayoutParams(params);
        	params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        	img.setLayoutParams(params);
        	border.setTag(new Pair<Integer, Integer>(group, now));
        	if(p.containsKey("video"))
        	{
	        	border.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p) {
						Pair<Integer, Integer> pair = (Pair<Integer, Integer>)p.getTag();
						fa.videoSelected((Integer)childData.get(pair.first).get(pair.second).get("video"));
					}});
        	}else
        	{
	        	border.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p) {
						Pair<Integer, Integer> pair = (Pair<Integer, Integer>)p.getTag();
						fa.videoSelected((Integer)childData.get(pair.first).get(pair.second).get("video"));
					}});
        	}
        }
        
		view.startAnimation(AnimationUtils.loadAnimation(fa.getActivity(), android.R.anim.slide_in_left));
        return view;  
	}

	@Override
	public int getChildrenCount(int arg0) {
		return 1;
	}

	@Override
	public Object getGroup(int arg0) {
		return groupData.get(arg0);
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public long getGroupId(int arg0) {
		return arg0;
	}

	@Override
    public View getGroupView(int i, boolean isExpanded, View convertView, ViewGroup parent) {
		Map<String, Object> dat = groupData.get(i); 
	    if (convertView == null) {  
	        convertView = mLayoutInflater.inflate(R.layout.listitem_contact, null);
	    }
        ((ImageView)convertView.findViewById(R.id.itemImgStatus)).setImageResource((Integer)dat.get("itemImgStatus"));
        ((ImageView)convertView.findViewById(R.id.itemImg)).setImageResource((Integer)dat.get("itemImg"));
        ((TextView)convertView.findViewById(R.id.itemName)).setText((String)dat.get("itemName"));
        ((TextView)convertView.findViewById(R.id.itemStatus)).setText((String)dat.get("itemStatus"));
        ((TextView)convertView.findViewById(R.id.itemInfo)).setText((String)dat.get("itemInfo"));
        ((TextView)convertView.findViewById(R.id.btJoin)).setOnClickListener(fa.onJoinClicked);
	    /*
	    else{  
	        groupViewHolder = (GroupViewHolder)convertView.getTag();  
	    } 
	    if (isExpanded) {  
	        groupViewHolder.view_bottom_line.setVisibility(View.VISIBLE);  
	        groupViewHolder.view_bottom.setBackgroundResource(R.drawable.card_detail_list_item_middle);  
	    }else {  
	        groupViewHolder.view_bottom_line.setVisibility(View.INVISIBLE);  
	        groupViewHolder.view_bottom.setBackgroundResource(R.drawable.card_detail_list_item_bottom);  
	    }
	    */
	    return convertView;  
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	public void actCollapseGroup(int index)
	{
		scaleType[index] = -1;
	}
	
	public void actExpandGroup(int index)
	{
		scaleType[index] = 1;	
	}
	
	public void doAnimate(int index)
	{
		if(scaleType[index] != -1 && scaleType[index]!=1)
			return;
		Log.d("ASDF", String.valueOf(scaleRatio[index]));
		View victim = arrView[index].findViewById(R.id.childVideoOtterContainer0);
		/*
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = Math.round(scaleRatio[index]);
		*/
		LayoutParams params = (LayoutParams) victim.getLayoutParams();
		params.bottomMargin = scaleType[index]*100;
		scaleRatio[index] += scaleType[index]*50;
		scaleRatio[index] = Math.max(oriHeight, Math.min(0, scaleRatio[index]));
//		victim.setLayoutParams(params);
		victim.requestLayout();
		victim.invalidate();
		/*
		if(scaleRatio[index] == oriHeight)
		{
			scaleType[index] = -2;
			fa.lst.collapseGroup(index);
		}
		*/
	}
	
}
