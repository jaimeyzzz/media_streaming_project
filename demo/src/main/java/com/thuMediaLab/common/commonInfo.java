package com.thuMediaLab.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thuMediaLab.demo.PlayerActivity;
import com.thuMediaLab.demo.R;

/*
 * 这里记录了公共的信息，都是用static来存。
 */
public class commonInfo {
	public static int type = 0;
	public static String myName = "test";
	public static String hisName = "Gerald Zhang";
	public static int myImg;
	public static int hisImg;
	public static int videoNum = 6;
	public static class chatInfo
	{
		public int type;	// 说话人  
		public String text;
		public chatInfo(int _type, String _text)
		{
			type = _type;
			text = _text;
		}
	};
	public static List<chatInfo> chatList = new ArrayList<chatInfo>();
	public static String videoAddr = "http://192.168.1.222/medialab/sintel/test.mpd";
	

	public static List<Map<String, Object>> videoData = new ArrayList<Map<String, Object>>();

	public static void initData()
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
		map.put("img", R.drawable.video7);
		map.put("name", "The Wolf of Wall Street");
		map.put("status", "Based on the true story of Jordan Belfort, from his rise to a wealthy stockbroker living the high life to his fall involving crime, corruption and the federal government.");
		map.put("info", "★☆☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video8);
		map.put("name", "Interior. Leather Bar.");
		map.put("status", "Filmmakers James Franco and Travis Mathews re-imagine the lost 40 minutes from \"Cruising\" as a starting point to a broader exploration of sexual and creative freedom.");
		map.put("info", "★☆☆☆☆");
		ret.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.video9);
		map.put("name", "Paranormal Activity: The Marked Ones");
		map.put("status", "After being \"marked,\" Jesse begins to be pursued by mysterious forces while his family and friends try to save him.");
		map.put("info", "★☆☆☆☆");
		ret.add(map);
		
		videoData = ret;
	}
	
	static void initDetail(View v, Map<String, Object> p)
	{
		((ImageView)v.findViewById(R.id.videoImg)).setImageResource((Integer)p.get("img"));
		((TextView)v.findViewById(R.id.videoName)).setText((String)p.get("name"));
		((TextView)v.findViewById(R.id.videoSubtitle)).setText((String)p.get("info"));
		((TextView)v.findViewById(R.id.videoDetail)).setText((String)p.get("status"));
	}
	
	static AlertDialog dlg;
	static Activity dlgAct;

	static DialogInterface.OnClickListener onPlayConfirmed = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			Intent intent = new Intent();
			intent.setClass(dlgAct, PlayerActivity.class);
			dlgAct.startActivity(intent);
		}};
	
	public static void showVideoDetailDialog(Activity activity, int index)
	{
//		Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
		LayoutInflater inflater = activity.getLayoutInflater();
		ViewGroup tmp = (ViewGroup)activity.findViewById(R.id.dialog_videoDetail);
		View view = inflater.inflate(R.layout.dialog_videoinfo, tmp);
		initDetail(view, videoData.get(index));
		dlg = new AlertDialog.Builder(activity).setView(view)
			.setPositiveButton("Play now!", onPlayConfirmed)
			.setNegativeButton("forget it..", null).show();
		Button posBtn = (Button)dlg.findViewById(android.R.id.button1);
		posBtn.setBackgroundColor(Color.rgb(0, 162, 232));
		posBtn.setTextColor(Color.rgb(255, 255, 255));
		posBtn.setShadowLayer(2, 1, 1, Color.GRAY);
		dlgAct = activity;
	}
	
}
