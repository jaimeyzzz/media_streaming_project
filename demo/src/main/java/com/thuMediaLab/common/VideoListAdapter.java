package com.thuMediaLab.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thuMediaLab.demo.R;
import com.thuMediaLab.demo.VideoSelectionActivity;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private static final String TAG = VideoListAdapter.class.getSimpleName();
	static int col = 5;
	int row;
	List<Map<String, Object>> dat;
	List<View> lstView;
	VideoSelectionActivity faView;
	
	public VideoListAdapter(VideoSelectionActivity _parent)
	{
		lstView = new ArrayList<View>();
		faView = _parent;
		dat = _parent.videoData;
		row = dat.size()/4;
		if(dat.size()%4>0)
			row++;
	}
	
	@Override
	public int getCount() {
		return row;
	}

	@Override
	public Object getItem(int p) {
		return lstView.get(p);
	}

	@Override
	public long getItemId(int p) {
		return p;
	}

	@Override
	public View getView(final int index, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.listitem_video, parent, false);
        }
        while(lstView.size()<=index)
        	lstView.add(null);
        lstView.set(index, view);
        int[] arr = {R.id.itemVideo0,R.id.itemVideo1,R.id.itemVideo2,R.id.itemVideo3};
        int[] arrName = {R.id.itemVideoName0,R.id.itemVideoName1,R.id.itemVideoName2,R.id.itemVideoName3};
        int[] arrLayout = {R.id.itemVideoLayout0,R.id.itemVideoLayout1,R.id.itemVideoLayout2,R.id.itemVideoLayout3};
        for(int i =0 ; i<4; ++i)
        {
        	final int p = index*4+i;
        	ImageView img = (ImageView)view.findViewById(arr[i]);
        	TextView tex = (TextView)view.findViewById(arrName[i]);
        	LinearLayout layout = (LinearLayout)view.findViewById(arrLayout[i]);
        	if(p>=dat.size())
        	{
        		img.setOnClickListener(null);
        		layout.setVisibility(View.INVISIBLE);
        	}else
        	{
	        	img.setImageResource((Integer)dat.get(p).get("img"));
	        	tex.setText((String)dat.get(p).get("name"));
        		layout.setVisibility(View.VISIBLE);
		        img.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						faView.onItemSelect(p);
					}});
        	}
        }
		return view;
	}

}
