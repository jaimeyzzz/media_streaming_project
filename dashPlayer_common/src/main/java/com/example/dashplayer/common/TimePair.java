package com.example.dashplayer.common;

import java.util.ArrayDeque;

public class TimePair
{
	public long stTime, enTime;
	public int bufStTime, bufEnTime;
	public int dat;
	public int speed;
	public int bitrate;
	public TimePair(long stTime, long enTime,int dat, int speed)
	{
		this.stTime = stTime;
		this.enTime = enTime;
		this.dat = dat;
		this.speed = speed;
	}
	public TimePair()
	{
		stTime = System.currentTimeMillis();
	}
}

