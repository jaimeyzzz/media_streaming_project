package com.example.dashplayer.common;

import java.util.ArrayList;


public class VideoFragmentsInfo
{
	public String[] url;
	public int bitrate;
	public int width,height;
	public int fragmentDuration;
	public VideoFragmentsInfo(ArrayList<String> _url, int _bitrate,int _width,int _height)
	{
		url = (String[])_url.toArray();
		bitrate = _bitrate;
		width = _width;
		height = _height;
	}
	public VideoFragmentsInfo()
	{
	}
}
