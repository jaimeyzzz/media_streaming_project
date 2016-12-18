package com.example.dashplayer.common;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryUtilities {
	
	public Intent batteryStatus;
	
	public BatteryUtilities(Context context)
	{
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		 batteryStatus = context.registerReceiver(null, ifilter);
	}
	
	public float getBatteryPercent()
	{
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float)scale;
		return batteryPct;
	}
	
}
