package com.thuMediaLab.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thuMediaLab.common.VideoListAdapter;
import com.thuMediaLab.common.commonInfo;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class VideoSelectionActivity extends Activity {

	ListView lst;
//	SimpleAdapter adapter;
	VideoListAdapter adapter;
	VideoSelectionActivity me;
	AlertDialog dlg;
	
	public List<Map<String, Object>> videoData = new ArrayList<Map<String, Object>>();
	
	DialogInterface.OnClickListener onPlayConfirmed = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			Intent intent = new Intent();
			intent.setClass(me, PlayerActivity.class);
			startActivity(intent);
		}};
	
	void initData()
	{
		videoData = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map;

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video0);
		map.put("name", "Sintel");
		map.put("status", "The film follows a girl named Sintel who is searching for a baby dragon she calls Scales. A flashback reveals that Sintel found Scales with its wing injured and helped care for it, forming ...");
		map.put("info", "★★★★★");
		ret.add(map);
		
		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video1);
		map.put("name", "The Hobbit: The Desolation of Smaug");
		map.put("status", "The dwarves, along with Bilbo Baggins and Gandalf the Grey, continue their quest to reclaim Erebor, their homeland, from Smaug. Bilbo Baggins is in possession of a mysterious and magical ring.");
		map.put("info", "★★★★☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video2);
		map.put("name", "Tyler Perry's A Madea Christmas");
		map.put("status", "Madea dispenses her unique form of holiday spirit on rural town when she's coaxed into helping a friend pay her daughter a surprise visit in the country for Christmas.");
		map.put("info", "★★★☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video3);
		map.put("name", "Frozen");
		map.put("status", "Fearless optimist Anna teams up with Kristoff in an epic journey, encountering Everest-like conditions, and a hilarious snowman named Olaf in a race to find Anna's sister Elsa, whose icy powers have trapped the kingdom in eternal winter.");
		map.put("info", "★★★☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video4);
		map.put("name", "The Hunger Games: Catching Fire");
		map.put("status", "Katniss Everdeen and Peeta Mellark become targets of the Capitol after their victory in the 74th Hunger Games sparks a rebellion in the Districts of Panem.");
		map.put("info", "★★★☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video5);
		map.put("name", "Out of the Furnace");
		map.put("status", "When Rodney Baze mysteriously disappears and law enforcement fails to follow through, his older brother, Russell, takes matters into his own hands to find justice.");
		map.put("info", "★★★☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video6);
		map.put("name", "Thor: The Dark World");
		map.put("status", "Faced with an enemy that even Odin and Asgard cannot withstand, Thor must embark on his most perilous and personal journey yet, one that will reunite him with Jane Foster and force him to sacrifice everything to save us all.");
		map.put("info", "★★☆☆☆");
		ret.add(map);
		
		videoData = ret;
	}
	
	List<Integer> setData()
	{
		List<Integer> ans = new ArrayList<Integer>();
		Iterator<Map<String, Object>> i = videoData.iterator();
		while(i.hasNext())
		{
			Map<String, Object> p = i.next();
			ans.add((Integer) p.get("img"));
		}
		return ans;
	}

	List<String> setDataName()
	{
		List<String> ans = new ArrayList<String>();
		Iterator<Map<String, Object>> i = videoData.iterator();
		while(i.hasNext())
		{
			Map<String, Object> p = i.next();
			ans.add((String) p.get("name"));
		}
		return ans;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_selection);
		me = this;
		setTitle("Recommand Video");
		getActionBar().setIcon(R.drawable.ic_media_video_poster);
		lst = (ListView)findViewById(R.id.lstVideoSelection);

		initData();
		
		adapter = new VideoListAdapter(this);
		lst.setAdapter(adapter);
		/*
		adapter = new SimpleAdapter(this, setData(), R.layout.listitem_video, new String[]{"img0","img1","img2","img3"}, new int[]{R.id.itemVideo0,R.id.itemVideo1,R.id.itemVideo2,R.id.itemVideo3});
		lst.setAdapter(adapter);
		lst.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	        {
				me.setResult(1);
				finish();
	        }
	    });
	    */
	}
	
	void initDetail(View v, Map<String, Object> p)
	{
		((ImageView)v.findViewById(R.id.videoImg)).setImageResource((Integer)p.get("img"));
		((TextView)v.findViewById(R.id.videoName)).setText((String)p.get("name"));
		((TextView)v.findViewById(R.id.videoSubtitle)).setText((String)p.get("info"));
		((TextView)v.findViewById(R.id.videoDetail)).setText((String)p.get("status"));
	}
	
	public void onItemSelect(int index)
	{
//		Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup tmp = (ViewGroup)this.findViewById(R.id.dialog_videoDetail);
		View view = inflater.inflate(R.layout.dialog_videoinfo, tmp);
		initDetail(view, videoData.get(index));
		dlg = new AlertDialog.Builder(this).setView(view)
			.setPositiveButton("Play now!", onPlayConfirmed)
			.setNegativeButton("forget it..", null).show();
		Button posBtn = (Button)dlg.findViewById(android.R.id.button1);
		posBtn.setBackgroundColor(Color.rgb(0, 162, 232));
		posBtn.setTextColor(Color.rgb(255, 255, 255));
		posBtn.setShadowLayer(2, 1, 1, Color.GRAY);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_video_selection, menu);
		return true;
	}

}
