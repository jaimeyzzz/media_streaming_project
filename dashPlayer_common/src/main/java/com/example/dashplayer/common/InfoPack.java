package com.example.dashplayer.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.util.Base64;

public class InfoPack extends HashMap<String,String> {

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!  !!Warning!!					!!!
	 * !!  控制语句中不能带','和';'!!!!!!!!!!
	 * !!								!!
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	private static final long serialVersionUID = 1227476794989948630L;

	public static String InfoPackToStr(InfoPack mp)
	{
		return mp.toString();
	}
	
	public String toString()
	{
		String ret = "";
		Iterator<Entry<String, String>> i = this.entrySet().iterator();
		while(i.hasNext()) {
			InfoPack.Entry<String, String> p = i.next();
			ret += p.getKey() + "," + p.getValue() + ";";
		}
		return ret;
	}
	
	public static InfoPack fromString(String s)
	{
		InfoPack ret = new InfoPack();
		ret.clear();
		String[] v = new String[2];
		v[0] = ""; v[1] = "";
		int state = 0;
		for(int i  = 0; i < s.length(); ++i)
		{
			char ch = s.charAt(i);
			if( ch == ',' )
				state = 1;
			else
			if( ch == ';' )
			{
				ret.put(v[0], v[1]);
				state = 0;
				v[0] = "";
				v[1] = "";
			}else
				v[state] += s.charAt(i);
		}
		return ret;
	}
	
	public int getInt(String s)
	{
		return Integer.valueOf(get(s));
	}
	
	public void putInt(String s,int p)
	{
		put(s, String.valueOf(p));
	}
	
	public static InfoPack fromBase64(String s)
	{
		String tmp = new String(Base64.decode(s, Base64.NO_WRAP));
		return fromString(tmp);
	}
}
