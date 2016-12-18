package com.lichao.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.lichao.bluetooth.btservice.ConnectedThread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class btbasic{

	abstract class IdThdMap
	{
		public abstract ConnectedThread get(int id);
		public abstract void remove(int id);
		public abstract void set(int id, ConnectedThread thd);
	}
	
	class IdThdMapArray extends IdThdMap
	{
		ArrayList<ConnectedThread> dat = new ArrayList<ConnectedThread>();

		@Override
		public ConnectedThread get(int id) {
			return dat.get(id);
		}

		@Override
		public void remove(int id) {
			dat.remove(id);
		}

		@Override
		public void set(int id, ConnectedThread thd) {
			dat.set(id, thd);
		}
	}
	
	class IdThdMapStaticArray extends IdThdMap
	{
		ConnectedThread[] dat = new ConnectedThread[100];
		@Override
		public ConnectedThread get(int id) {
			return dat[id];
		}

		@Override
		public void remove(int id) {
			dat[id] = null;
		}

		@Override
		public void set(int id, ConnectedThread thd) {
			dat[id] = thd;
		}
		
	}
	
	IdThdMap idMap = new IdThdMapStaticArray();
	/****************************definition*********************/
	// Debugging
    private static final String TAG = "Devicetransmitbasic";   //log name
    private static final boolean D = true;

    // *TRUNCK* types sent from the btService Handler 
    public static final int TRUNCK_STATE_CHANGE = 1;
    public static final int TRUNCK_READ = 2;
    public static final int TRUNCK_WRITE = 3;
    public static final int TRUNCK_DEVICE_NAME = 4;
    public static final int TRUNCK_TOAST = 5;
    public static final int CONN_LOST = 6;

    // Key names received from the btService Handler 
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes   请求语句设置
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

   
    // Name of the connected device               连接了的设备名称
    private static String mConnectedDeviceName = null;
   
    // Local Bluetooth adapter                  本地蓝牙适配器
    public static BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the btservices        “服务类”对象
    private static btservice mbtService = null;

    private String Mac;
    private String Ip;
    
    /***bluetooth statue***/
    private static String TOAST_NULL="TOAST_NULL";
    private static String TOAST_ADDR_WRONG="TOAST_ADDR_WRONG";
    private static String TOAST_REQUSET_START="TOAST_REQUSET_START";
    private static String TOAST_Prepared="TOAST_Prepared";
    private String TOAST_notconnected="TOAST_notconnected";
    private String TOAST_SENT="TOAST_SENT";
    private String TOAST_RECEIVED="TOAST_RECEIVED";
    
    public static String HTCmac="7C:61:93:48:4C:DD";
	public static String LGmac="10:68:3F:5F:6C:00";
	public static String Nexus7mac="50:46:5D:C9:4A:18";
	public static String Note10mac = "94:35:0A:F2:1F:D1";
	public Handler mHamdler;
	
	
	public String slaversavepath;
	public String slaversendpath;
	
	OnEventListener messageListener;
	
    public btbasic( OnEventListener messageListener) {
           this.messageListener = messageListener;
    }
	/*  public void bindOnMessageListener(OnEventListener _event)//when should i use it?just use event.work will be enough
	    {
	    	failListener = _event;
	    }*/
   
    void log(String s)
    {
    	InfoPack info = new InfoPack();
    	info.put("cmd", Commands.notify);
    	info.put("txt", s);
    	messageListener.work(info);
    }
    
    /***func1**bt or wifi;open bt or wifi*/
    /**i turn on bluetooth earlier and send to each other to change the speed info and decide who is master**/
    public String tranpre(String mode){
    	if(D) Log.e(TAG, "++ TRANPRE ++");
    	if( mode == "bt"){
    		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    		 if(mBluetoothAdapter!=null)
       		  {
       			 System.out.println("we have bluetoothadapter");
       			 //2on or off
       			 if(!mBluetoothAdapter.isEnabled())
       			 {   				 
       				System.out.println("is closed");
       				return TOAST_REQUSET_START;
       			 }   
       			 System.out.println(mBluetoothAdapter.getAddress());
       		  }else{
       			  return TOAST_NULL;
       		  }
    		 System.out.println("will setup");           
            setup();
            System.out.println("setuped");
            return TOAST_Prepared;
     	}
    	if(mode =="wifi"){
    		return TOAST_Prepared;
    	}
    	return TOAST_ADDR_WRONG;    	
    }
    public void setup(){
    	 mbtService=new btservice(this, mHandler, messageListener);
         mbtService.start();	
    }
  
    public void setSavePath(int id, String s)
    {
    	ConnectedThread thd = idMap.get(id);
    	if(thd == null)
    	{
    		log("设置保存路径失败！" + s);
    		return;
    	}
    	mbtService.setSavePath(thd, s);
    }
  
    /**request connect**/
    public void requestconnect(String addr){
    	//i will use id later, the map should be kept in btbasic?
    	//if i have connect two device, should i have two Service? so i can not only use one static mbtservice at same time? and only one connected thread at same time?
    	//Can i connect without judge whether it is null? just start a new mbtService?
    	Log.i(TAG, "++ will request connect ++");
    	if (mbtService != null) 
    	{ // Initialize the BluetoothChatService to perform bluetooth connections
            // if (mbtService.getState() != btservice.STATE_CONNECTED) { //why can't i use this judge sentence?
             if(D) Log.e(TAG, "++ will connectDevice ++");
            //addr=map.get(id);
            //addr=LGmac; //haven't use dynamic addr
            connectDevice(addr,true);
          //  }
    	}else{
    		Log.i(TAG, "++ no mbtservice ++");
    	}
            
    }
    ///////////////////////////////
    // Map Control
    public ConnectedThread getThdFromId(int id)
    {
    	return idMap.get(id);
    }

    public ConnectedThread getThdFromId(String id)
    {
    	return idMap.get(Integer.valueOf(id));
    }
    
    public void removeThd(int id)
    {
    	if(idMap.get(id) != null)
    		idMap.remove(id);
    }
    
    public void matchIdThd(int id, String mac){
    	if(id==0)
    	{	// 我是从机我要匹配主机
    		idMap.set(id, mbtService.mMyThread);
    		return;
    	}
    	Iterator<ConnectedThread> i = mbtService.mConnectedThread.iterator();
    	while(i.hasNext())
    	{
    		ConnectedThread thd = i.next();
    		if(thd.OppoMac.equals(mac))
    		{
    			idMap.set(id, thd);
    			thd.setAndAcquireId(id);
    			return;
    		}
    	}
    }

    //
    /////////////////////////////////
    
    public void setMyId(int id)
    {
    	mbtService.mMyThread.id = id;
    }

    public void sendTrash(int id)
    {
    	ConnectedThread thd = getThdFromId(id);
    	thd.sendTrash();
    }

    /**Slaver:send after download**///use OnEvetListener
    public void sendtrunck(final int id, final String filename,long time){
    	ConnectedThread thd = getThdFromId(id);
    	if(thd == null)
    	{
    		log("发送失败！");
    		return;
    	}
    	thd.send(filename, time);
    }
	
    /***Master or slaver send info***/
    public void sendinfo(InfoPack map,int id,OnEventListener mOnEventListener,long time){
    	ConnectedThread thd = getThdFromId(id);
    	if(thd == null)
    	{
    		log("发送失败！");
    		return;
    	}
    	thd.sendInfoPack(map);
   }
     public String getMac(){
    	//use to get local mac or connected device mac
    	//在选择设备的时候就把这个写好，此处直接返回
    	return Mac;
    }
    
    public String getIp(){
		//use to get local ip or connected device mac
    	return Ip;
    }
    
    public static void changesavepath(HashMap<String,String> match_map,String id, String newmastersavepath){
    	if(D) Log.e(TAG, "++ CHANGESAVEPATH ++");
    	match_map.put(id, newmastersavepath);//a id can just match his mac?
    }
    
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            //本来想写以下代码，结果报错
            // startActivity(discoverableIntent);
        }
    }  
    
    public int getSpeed()
    {
    	Iterator<ConnectedThread> i = mbtService.mConnectedThread.iterator();
    	int cnt = 0;
    	while(i.hasNext())
    	{
    		ConnectedThread thd = i.next();
    		cnt += thd.getSpeed();
    	}
    	return cnt;
    }
   
    private void connectDevice(/*Intent data,*/String addr, boolean secure) {
    	if(D) Log.e(TAG, "++ ConnectDevice ++");
    	// Get the device MAC address
       /* String Mac = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(Mac);*/
    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
        // Attempt to connect to the device
        mbtService.connect(device, secure);
    }
      
 // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {//根据第一个参数区分
            case TRUNCK_STATE_CHANGE://1
                if(D) Log.i(TAG, "TRUNCK_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {//根据第二个参数区分
                case btservice.STATE_CONNECTED: //3
                {
                	Bundle bnd = msg.getData();
                  //  setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                   
                    break;
                }
                case btservice.STATE_CONNECTING://2
                  //  setStatus(R.string.title_connecting);
                    break;
                case btservice.STATE_LISTEN://1
                case btservice.STATE_NONE://0
                 //   setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case TRUNCK_WRITE://3  !!?
                              break;
            case TRUNCK_READ://2   !!?
                              break;
            case TRUNCK_DEVICE_NAME://4,,获取连接的名字
                         break;
            case TRUNCK_TOAST: //5 
            {
                InfoPack ret = new InfoPack();
                ret.put("cmd", Commands.connLost);
            	messageListener.work(ret);//which connection lost?
                break;
            }
	        case CONN_LOST: //5 
	        {
	        	Bundle bnd = msg.getData();
	        	int id = bnd.getInt("id");
	            InfoPack ret = new InfoPack();
	            ret.put("cmd", Commands.connLost);
	            ret.put("id", String.valueOf(id));
	        	messageListener.work(ret);//which connection lost?
	        	removeThd(id);
	            break;
	        }
            }
        }
    };
    
}