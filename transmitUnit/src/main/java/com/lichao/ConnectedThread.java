package com.lichao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.dashplayer.common.Commands;
import com.example.dashplayer.common.InfoPack;
import com.example.dashplayer.common.OnEventListener;
import com.lichao.bluetooth.btservice;

/**************************function3:connectedThread***********************
 *************************************************************/
/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread extends Thread {
	/////////////////////////
	// Deprecated ///////////
	///////////////////////
	/*
	// 传输用的thread
//    private final BluetoothSocket mmBtSocket;
	private String TAG = "ConnectedThread";
	private final BluetoothSocket mmBtSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final NetworkUtility network;
    OnEventListener mEvnt;
    String OppoMac;

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
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    	network = new NetworkUtility(tmpIn, tmpOut, mEvent);
    }
    
    public void setFileSavePath(String path)
    {
    	network.outPath = path;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;
        File file=null;
        // Keep listening to the InputStream while connected
        while (true) {
            try {
	    		   ///////////////////////////////////////////////
	    		   //////
	    		   //////	接收文件
	    		   //////
	    		   //////////////////////////////////////////////
            	int len;
	    		   while((len = mmInStream.read(buffer)) > -1){
	    			   byte[] byt = Arrays.copyOf(buffer,  len);
				   network.dataProcesser(byt);
//  	    			   output.write(buffer);
	    		   }
              } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                connectionLost();
                // Start the service over to restart listening mode
                btservice.this.start();
                break;
            }
            finally{
	    		  Log.i(TAG, "END mConnectedThread");
	    		  btservice.this.start();
	    	   }
        }
    }
    
    public void sendInfoPack(InfoPack pack)
    {
    	String tmp = "";
		tmp += network.appendCmd(NetworkUtility.Cmd.cmdInfo);	// 发送命令
		tmp += network.appendInfo(pack);
		network.send(tmp);
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
                Log.e(TAG, "Exception during write", e);
            }
    	}
    }
    
    // 已连接线程内的发送文件函数
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
    // 已连接线程内的发送键值对函数
    public void sendinfor(final byte[] outinfor,OnEventListener mOnEventListener,long time) {
        try {
           // mmOutStream.write(buffer);
	    	   OutputStream output=mmOutStream;
	    	   try{
	    		   mmOutStream.write(outinfor);
            // Share the sent message back to the UI Activity
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }finally{
	    	   }
        }finally{
    		   //??
	    	   }
    }
    //已连接线程内部的取消函数
    public void cancel() {
        try {
            mmBtSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }   
    }
   */
}
