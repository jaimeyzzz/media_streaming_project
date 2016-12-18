package com.lichao.bluetooth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.Logable;
import com.example.dashplayer.common.OnEventListener;
import com.lichao.NetworkUtility;
import com.lichao.NetworkUtility.Cmd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class btservice extends Logable{
	/**
	 * This class does all the work for setting up and managing Bluetooth
	 * connections with other devices. It has a thread that listens for
	 * incoming connections(acceptThread), a thread for connecting with a device(connectThread), and a
	 * thread for performing data transmissions when connected(connectedThread).
	 */
	    /**************************definition***************************/
		// Debugging
	    private static final String TAG = "btservice";
	    private static final boolean D = true;

	    // Name for the SDP record when creating server socket
	    private static final String NAME_SECURE = "BluetoothSecure";
	    private static final String NAME_INSECURE = "BluetoothInsecure";

	    // Unique UUID for this application
	    private static final UUID MY_UUID_SECURE =
	        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	    private static final UUID MY_UUID_INSECURE =
	        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

	    // Member fields
	    private final BluetoothAdapter mAdapter;
	    private final Handler mHandler;
	    private AcceptThread mSecureAcceptThread;
	    private AcceptThread mInsecureAcceptThread;
	    private ConnectThread mConnectThread;
	    public Set<ConnectedThread> mConnectedThread = new HashSet<ConnectedThread>();
	    public ConnectedThread mMyThread;	// 当作为客户端时候的ConnectedThread
	    private int mState; 
	    
	    private OnEventListener mEvent;

	    // Constants that indicate the current connection *state*
	    public static final int STATE_NONE = 0;       // we're doing nothing
	    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	    public static int year;
	    public static int month;
	    public static int day;
	    public static int hour;
	    public static int minute;
	    public static int second;
	    
	    public String sender_mac;
	    public static String mastersavepath = "/storage/sdcard0/temporary/";
	    public static String savefilename;
	    public static int cnt = 1;
	    
	    OnEventListener logEvnt = new OnEventListener()
	    {
			@Override
			public void work(Object param) {
				InfoPack p = new InfoPack();
				p.put("cmd", Commands.notify);
				p.put("txt", "[错误]" + (String)param);
				mEvent.work(p);
			}
	    };
	    
	    /**
	     * Constructor. Prepares a new Bluetooth session.
	     * @param context  The UI Activity Context
	     * @param handler  A Handler to send messages back to the UI Activity
	     */
	     /*************************方法1：构造函数**********************
	      *************************功能:传递变量**********************/
	    public btservice(btbasic _btbasic, Handler handler, OnEventListener _mEvent) {
	        mAdapter = BluetoothAdapter.getDefaultAdapter();
		    mastersavepath = "/storage/sdcard0/temporary/";
	        mState = STATE_NONE;
	        mEvent = _mEvent;
	        mHandler = handler;     //handler是调用btservice方法时传进来的
	    }

	    /**
	     * Set the current state of the chat connection
	     * @param state  An integer defining the current connection state
	     */
	    /**************************方法2设置传输状态函数***********************
	     *************************************************************/
	    private synchronized void setState(int state) {
	        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
	        mState = state;

	        // Give the new state to the Handler so the UI Activity can update
	        mHandler.obtainMessage(btbasic.TRUNCK_STATE_CHANGE, state, -1).sendToTarget();
	    }

	    private synchronized void setState(int state, Bundle bnd) {
	        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
	        mState = state;

	        // Give the new state to the Handler so the UI Activity can update
	        Message msg = mHandler.obtainMessage(btbasic.TRUNCK_STATE_CHANGE, state, -1);
	        msg.setData(bnd);
	        msg.sendToTarget();
	    }
	    
	    /**************************方法3读取状态函数***********************
	     **************************在sendinfo中被调用****************************/
	    /**
	     * Return the current connection state. */
	    public synchronized int getState() {
	        return mState;
	    }

	    /**************************方法4开始线程的函数***********************
	     ******************检查是否已连接和正要连接，启动服务器监听线程accept**********/
	    /**
	     * Start the chat service. Specifically start AcceptThread to begin a
	     * session in listening (server) mode. Called by the Activity onResume() */
	    public synchronized void start() {
	        if (D) Log.d(TAG, "start");

	        // Cancel any thread attempting to make a connection
	        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

	        // Cancel any thread currently running a connection
	        Iterator<ConnectedThread> i = mConnectedThread.iterator();
	        while(i.hasNext())
	        {
	        	ConnectedThread p = i.next();
	        	p.cancel();
	        }
	        mConnectedThread.clear();

	        setState(STATE_LISTEN);

	        // Start the thread to listen on a BluetoothServerSocket
	        if (mSecureAcceptThread == null) {
	            mSecureAcceptThread = new AcceptThread(true);
	            mSecureAcceptThread.start();
	        }
	        if (mInsecureAcceptThread == null) {
	            mInsecureAcceptThread = new AcceptThread(false);
	            mInsecureAcceptThread.start();
	        }
	    }
	    /**************************方法5：进行连接的函数***********************
	     *******************功能：检查是否试图连接，开启试图连接线程*****************/
	    /**
	     * Start the ConnectThread to initiate a connection to a remote device.
	     * @param device  The BluetoothDevice to connect
	     * @param secure Socket Security type - Secure (true) , Insecure (false)
	     */
	    public synchronized void connect(BluetoothDevice device, boolean secure) {
	        if (D) Log.d(TAG, "connect to: " + device);

	        // Cancel any thread attempting to make a connection
	        if (mState == STATE_CONNECTING) {
	            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
	        }

	        /*
	        // Cancel any thread currently running a connection
	        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
	         */
	        
	        // Start the thread to connect with the given device
	        mConnectThread = new ConnectThread(device, secure);
	        mConnectThread.start();
	        setState(STATE_CONNECTING);
	    }
	    /**************************方法6：已连接函数***********************
	     *******检查是否已连接和正要连接，取消accept线程，开启已连接线程，传回数据********/
	    //如果要一直保持接收那么此处不需要取消，另外在connectedthread里传输完成后也不需要再start了
	    /**
	     * Start the ConnectedThread to begin managing a Bluetooth connection
	     * @param socket  The BluetoothSocket on which the connection was made
	     * @param device  The BluetoothDevice that has been connected
	     */
	    public synchronized ConnectedThread connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
	        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

	        // Cancel the thread that completed the connection
	        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

	        // Cancel the accept thread because we only want to connect to one device -- What? u sure?  (Commented by Gerald)
	        //if we want to connect more, we don't need to cancel, so that we don't need to restart it either? -- Damn right!
	        
	        // Start the thread to manage the connection and perform transmissions
	        
	        ConnectedThread tmp = new ConnectedThread(socket, socketType, mEvent);
	        tmp.start();

	        // Send the name of the connected device back to the UI Activity
	        //这个handler是setup里传过来的handler
	        Message msg = mHandler.obtainMessage(btbasic.TRUNCK_DEVICE_NAME);
	        Bundle bundle = new Bundle();
	        bundle.putString(btbasic.DEVICE_NAME, device.getName());//将DEVICENAME与实际NAME捆绑
	        bundle.putString("mac", device.getAddress());
	        msg.setData(bundle);    //发送bundle类变量bundle
	        mHandler.sendMessage(msg);   //发送消息msg到消息队列

	        setState(STATE_CONNECTED, bundle);
	        return tmp;
	    }
	    /**************************方法7：停止函数***********************
	     **************************停止所有线程，应在destroy函数中被调用，但还没写*******/
	    /**
	     * Stop all threads
	     */
	    public synchronized void stop() {
	        if (D) Log.d(TAG, "stop");

	        if (mConnectThread != null) {
	            mConnectThread.cancel();
	            mConnectThread = null;
	        }

	        Iterator<ConnectedThread> i = mConnectedThread.iterator();
	        while(i.hasNext())
	        {
	        	ConnectedThread p = i.next();
	        	p.cancel();
	        }
	        mConnectedThread.clear();

	        if (mSecureAcceptThread != null) {
	            mSecureAcceptThread.cancel();
	            mSecureAcceptThread = null;
	        }

	        if (mInsecureAcceptThread != null) {
	            mInsecureAcceptThread.cancel();
	            mInsecureAcceptThread = null;
	        }
	        setState(STATE_NONE);
	    }
	    /**************************方法9：连接失败函数***********************
	     **************************发回失败信息，再重新启动start************************/
	    /**
	     * Indicate that the connection attempt failed and notify the UI Activity.
	     */
	    private void connectionFailed() {
	        // Send a failure message back to the Activity
	        Message msg = mHandler.obtainMessage(btbasic.TRUNCK_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(btbasic.TOAST, "Unable to connect device");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
	        

	        // Start the service over to restart listening mode
	        btservice.this.start();
	    }
	    /**************************方法10：连接丢失函数***********************
	     **************************发送丢失消息********************/
	    /**
	     * Indicate that the connection was lost and notify the UI Activity.
	     */
	    private void connectionLost(ConnectedThread thd) {
	        // Send a failure message back to the Activity
	        Message msg = mHandler.obtainMessage(btbasic.CONN_LOST);
	        Bundle bundle = new Bundle();
	        bundle.putString(btbasic.TOAST, "Device connection was lost");
	        bundle.putInt("id", thd.id);
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
	        mConnectedThread.remove(thd);
	    }
	    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    ///////////////////////////////////////////////// Accept Thread ///////////////////////////////////////////////////////////////
	    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    /**************************线程1：接收线程***********************
	     *************************************************************/
	    /**
	     * This thread runs while listening for incoming connections. It behaves
	     * like a server-side client. It runs until a connection is accepted
	     * (or until cancelled).
	     */
	    private class AcceptThread extends Thread {
	        // The local server socket
	        private final BluetoothServerSocket mmServerSocket;
	        private String mSocketType;

	        public AcceptThread(boolean secure) {
	            BluetoothServerSocket tmp = null;
	            mSocketType = secure ? "Secure":"Insecure";

	            // Create a new listening server socket
	            try {
	                if (secure) {
	                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
	                        MY_UUID_SECURE);
	                } else {
	                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
	                            NAME_INSECURE, MY_UUID_INSECURE);
	                }
	            } catch (IOException e) {
	            	  log("Accept初始化失败！");
	                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
	            }
	            mmServerSocket = tmp;
	        }

	        public void run() {
	            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
	                    "BEGIN mAcceptThread" + this);
	            setName("AcceptThread" + mSocketType);

	            BluetoothSocket socket = null;//不是seversocket

	            // Listen to the server socket if we're not connected
	            while (true) {
	                try {
	                    socket = mmServerSocket.accept();
	                    Log.i(TAG, socket.toString()+"!!");
	                } catch (IOException e) {
		            	  log("Accept失败！");
	                    Log.e(TAG, "Socket Type: " + mSocketType + " AcceptThread.accept() failed", e);
	                    break;
	                }
	                if (socket != null) {
	                    synchronized (btservice.this) {
	                        switch (mState) {
	                        case STATE_LISTEN:
	                        case STATE_CONNECTING:
	                            ConnectedThread thd = connected(socket, socket.getRemoteDevice(), mSocketType);
	                            mConnectedThread.add(thd);
	                            InfoPack ret = new InfoPack();
	                            ret.put("cmd", Commands.slaverConnected);
	                            ret.put("mac", socket.getRemoteDevice().getAddress());
	                            mEvent.work(ret);
	                            break;
	                        case STATE_NONE:
	                        case STATE_CONNECTED:
	                        }
	                    }
	                }else
	                	// Gerald: keep everything works 'til the server is down.
	                	break;
	            }
	            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

	        }

	        public void cancel() {
	            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
	            try {
	                mmServerSocket.close();
	            } catch (IOException e) {
	            	  log("cancel失败！");
	                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
	            }
	        }
	    }

	    /**************************function2:connectThread***********************
	     *************************************************************/
	    /**
	     * This thread runs while attempting to make an outgoing connection
	     * with a device. It runs straight through; the connection either
	     * succeeds or fails.
	     */
	    private class ConnectThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final BluetoothDevice mmDevice;
	        private String mSocketType;

	        public ConnectThread(BluetoothDevice device, boolean secure) {
	            mmDevice = device;
	            BluetoothSocket tmp = null;
	            mSocketType = secure ? "Secure" : "Insecure";

	            // Get a BluetoothSocket for a connection with the
	            // given BluetoothDevice
	            try {
	                if (secure) {
	                    tmp = device.createRfcommSocketToServiceRecord(
	                            MY_UUID_SECURE);
	                } else {
	                    tmp = device.createInsecureRfcommSocketToServiceRecord(
	                            MY_UUID_INSECURE);
	                }
	            } catch (IOException e) {
	            	  log("创建connectThread失败！");
	                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
	            }
	            mmSocket = tmp;
	            Log.i(TAG, "finish creat socket");
	        }

	        public void run() {
	            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
	            setName("ConnectThread" + mSocketType);

	            // Always cancel discovery because it will slow down a connection
	            mAdapter.cancelDiscovery();

	            // Make a connection to the BluetoothSocket
	            try {
	                // This is a blocking call and will only return on a
	                // successful connection or an exception
	                mmSocket.connect();
	            } catch (IOException e) {
	                // Close the socket
	            	 Log.e(TAG, "Socket connect failed");
	            	try {
	                    mmSocket.close();
	                } catch (IOException e2) {
	                    Log.e(TAG, "unable to close() " + mSocketType +
	                            " socket during connection failure", e2);
	                }
	                connectionFailed();
	                return;
	            }
	            Log.i(TAG, "finish connect socket");

	            // Reset the ConnectThread because we're done
	            synchronized (btservice.this) {
	                mConnectThread = null;
	            }

	            // Start the connected thread
	            mMyThread = connected(mmSocket, mmDevice, mSocketType);
                InfoPack ret = new InfoPack();
                ret.put("cmd", Commands.slaverConnected);
                ret.put("mac", mmDevice.getAddress());
                mEvent.work(ret);
	        }

	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) {
	                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
	            }
	        }
	    }
	    
	    public void setSavePath(ConnectedThread thd, String s)
	    {
	    	thd.setFileSavePath(s);
	    }
	    
	    
	    private  String SDPATH;
	       //获得SD卡的路径,,/sdcard/
	       public  String getSDPATH(){
	    	   SDPATH=Environment.getDownloadCacheDirectory()+"/";
	    	   return SDPATH;
	         }
	       //创建文件
	       public File creatSDFile(String fileName)throws IOException{
	    	   File file=new File(SDPATH+fileName);
	    	   file.createNewFile();
	    	   return file;
	       }
	       //创建目录
	       public File creatSDDir(String dirName){
	    	File dir=new File(SDPATH+dirName);
	    	dir.mkdir();
	    	   return dir;   
	       }
	       //判断SD卡上的文件是否存在
	       public boolean isFileExist(String fileName){
	    	   File file=new File(SDPATH+fileName);
	    	   return file.exists();
	       }
	       //从SD卡上读出来用蓝牙发出，在send中已写
	   
	    //从蓝牙的inputStream中读取保存到SD卡,在线程中写了

	    
	public void getsystemtime(){        
        Calendar c= Calendar.getInstance();      
       year = c.get(Calendar.YEAR);  
      month = c.get(Calendar.MONTH);  
       day = c.get(Calendar.DAY_OF_MONTH); 
       hour = c.get(Calendar.HOUR_OF_DAY);  
      minute = c.get(Calendar.MINUTE); 
      second=c.get(Calendar.SECOND);
	}
		
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**************************function3:connectedThread***********************
	 *************************************************************/
	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	class ConnectedThread extends Thread {
		// 传输用的thread
	//    private final BluetoothSocket mmBtSocket;
		private String TAG = "ConnectedThread";
		private final BluetoothSocket mmBtSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    private final NetworkUtility network;
	    OnEventListener mEvnt;
	    String OppoMac;
	    int id;
	
	    public ConnectedThread(BluetoothSocket socket, String socketType, OnEventListener mEvent) {
	        Log.d(TAG, "create ConnectedThread(Bluetooth): " + socketType);
	        mmBtSocket = socket;
	        OppoMac = mmBtSocket.getRemoteDevice().toString();//is used to mark who send it
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	        mEvnt = mEvent;
	
	        // Get the BluetoothSocket input and output streams
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) {
	        	log("输入输出流失败！");
	            Log.e(TAG, "temp sockets not created", e);
	        }
	
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    	network = new NetworkUtility(tmpIn, tmpOut, mEvent);
	    }
	    
	    public void setId(int id)
	    {
	    	this.id = id;
	    	network.id = id;
	    }
	    
	    public void setFileSavePath(String path)
	    {
	    	network.outPath = path;
	    }
	
	    public void run() {
	        Log.i(TAG, "BEGIN mConnectedThread");
	        byte[] buffer = new byte[1024*4];
	        // Keep listening to the InputStream while connected
	        while (true) {
	            try {
	    		   ///////////////////////////////////////////////
	    		   //////
	    		   //////	接收
	    		   //////
	    		   //////////////////////////////////////////////
	            	int len;
		    		   while((len = mmInStream.read(buffer)) > -1){
		    			   byte[] byt = Arrays.copyOf(buffer,  len);
						   network.dataProcesser(byt);
//						   sleep(40);
//  	    			   output.write(buffer);
		    		   }
	              } catch (Exception e) {
	            	  log("接收失败！");
	                Log.e(TAG, "disconnected", e);
	                connectionLost(this);
	                break;
	            }
	            finally{
		    		  Log.i(TAG, "END mConnectedThread");
		    		  btservice.this.start();
		    	   }
	        }
	    }
	    
	    public int getSpeed()
	    {
	    	return network.getSpeed();
	    }
	    
	    public void sendInfoPack(InfoPack pack)
	    {
	    	String tmp = "";
			tmp += network.appendCmd(NetworkUtility.Cmd.cmdInfo);	// 发送命令
			tmp += network.appendInfo(pack);
			network.send(tmp);
	    }

	    public void sendTrash()
	    {
	    	network.send("{400");
	    }
	    
	    // 经测试，同步有效
	    public synchronized void doSend(final String fileName,long time) {
	    	////////////////////////////
	    	//
	    	// 发送文件
	    	// 
	    	/////////////////////////////
	    	synchronized(mmOutStream)
	    	{
		    	   OutputStream output = mmOutStream;
		    	   FileInputStream input;
		    	   try{
		    		   network.startFileTrans();
		    		   input = new FileInputStream(fileName);
		    		   byte[] buffer = new byte[4*1024];	//4k写一次
		    		   int len;
		    		   while( (len = input.read(buffer)) > -1 ){	//从用input从文件写到buffer
		    			   byte[] tmp = Arrays.copyOf(buffer, len);
		    			   network.sendFilePiece(tmp);	//用output从buffer写到输出流
		    		   }
		    		   network.endFileTrans();
		    		   output.flush();//清除缓存
		    		   
				   InfoPack ret = new InfoPack();
				   ret.put("cmd", Commands.bluetoothTransmitted);
		    	       mEvent.work(ret);
	            } catch (Exception e) {
	            	  log("发送失败！");
	                Log.e(TAG, "Exception during write", e);
	            }
	    	}
	    }
	    
	    /**************************已连接线程内的发送文件函数***********************/
	    /** Write to the connected OutStream.
	     * @param buffer  The file to send
	     */
	    public void send(final String fileName,final long time) {
	       // mmOutStream.write(buffer);
	    	Thread thd = new Thread(new Runnable(){
				@Override
				public void run()
		        {
					doSend(fileName, time);
	        	}    
			});
	    	thd.start();
	    }
	    
	    /**************************已连接线程内部的取消函数***********************
	     *************************************************************/
	    public void cancel() {
	        try {
	            mmBtSocket.close();
	        } catch (IOException e) {
				InfoPack p = new InfoPack();
				p.put("cmd", Commands.notify);
				p.put("txt", "[错误]蓝牙取消时遇到了问题！");
				mEvent.work(p);
	            Log.e(TAG, "close() of connect socket failed", e);
	        }
	        
	    }
	    
	    public void setAndAcquireId(int id)
	    {
	    	this.id = id;
	    	network.id = id;
	    	InfoPack p = new InfoPack();
	    	p.put("cmd", Commands.informId);
	    	p.put("id", String.valueOf(id));
	    	String tmp = "";
	    	tmp += network.appendCmd(NetworkUtility.Cmd.cmdInfo);
	    	tmp += network.appendInfo(p);
	    	network.send(tmp);
	    }
	}
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}


