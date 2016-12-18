package com.example.dashplayer.common;

public class Logable {
	
	OnEventListener onLog;
	
	public void bindOnLogEvent(OnEventListener _onLog)
	{
		onLog = _onLog;
	}
	
	public void log(String text)
	{
		if( onLog == null )
			return;
		onLog.work(text);
	}
	
	public void loge(String text)
	{
		log("[错误]"+text);
	}
	
	public void logm(String text)
	{
		log("[matlab_m]"+text);
	}
	
	public void logdat(String text)
	{
		log("[matlab_dat]"+text);
	}
}
