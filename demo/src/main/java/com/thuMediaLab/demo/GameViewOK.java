package com.thuMediaLab.demo;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GameViewOK extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder surfaceHolder;
	private boolean isThreadRunning = true;
	private MyThread thread;
	public  Bitmap bitmap,bitmap2,bitmap3;
	public  Matrix matrix= new Matrix(); 
    public  Paint mPaint = new Paint();  
    public  InputStream is,is2;
    Canvas canvas;
    private int t,t2;
    public  Matrix cloud=new Matrix();

	public GameViewOK(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);//注册回调方法
	    isThreadRunning=false;
	    this.setZOrderOnTop(true);//设置画布  背景透明
	    surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		//创建surfaceView时启动线程
		t=0;
		t2=1482;
		is = getResources().openRawResource(R.drawable.middle1_1); 
		is2 = getResources().openRawResource(R.drawable.middle2_3);
		bitmap = BitmapFactory.decodeStream(is);  
        bitmap2 = BitmapFactory.decodeStream(is2);
        isThreadRunning = true;  
        thread = new MyThread();  
        thread.start();  
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		//当surfaceView销毁时, 停止线程的运行. 避免surfaceView销毁了线程还在运行而报错.
		isThreadRunning = false;
		//第三种方法防止退出时异常. 当surfaceView销毁时让线程暂停300ms . 醒来再执行run()方法时,isThreadRunning就是false了. 
		try
		{
			Thread.sleep(300);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 将绘图的方法单独写到这个方法里面.
	 */
	private void drawVieW()
	{
		try
		{//第一种方法防止退出时异常: 当isThreadRunning为false时, 最后还是会执行一次drawView方法, 但此时surfaceView已经销毁
			//因此才来判断surfaceHolder
			if (surfaceHolder != null)
			{
				//1. 在surface创建后锁定画布
				canvas = surfaceHolder.lockCanvas();
				//2. 可以在画布上进行任意的绘画操作( 下面是画一条红色 的线 )
				/*Paint paint = new Paint();
				paint.setColor(Color.BLUE);
				//paint.setStyle(Style.STROKE);//只有边框
				paint.setStrokeWidth(5);
				canvas.drawCircle(200, 200, r++, paint);*/
				if(t>=1460) t=0;
				if(t2<=0) t2=1482;
				t=t+2;
				t2=t2-2;
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
	            canvas.drawBitmap(bitmap,t,550,mPaint);  
	            canvas.drawBitmap(bitmap,t+1460,550,mPaint);
	            //canvas.drawBitmap(bitmap,t+830,200,mPaint);
	            canvas.drawBitmap(bitmap,t-1460,550,mPaint);
	            //canvas.drawBitmap(bitmap,t-830,200,mPaint);
	            canvas.drawBitmap(bitmap2,t2,550,mPaint);
	            canvas.drawBitmap(bitmap2,t2-1482,550,mPaint);
	            //canvas.drawBitmap(bitmap2,t2-832,200,mPaint);
	            canvas.drawBitmap(bitmap2,t2+1482,550,mPaint);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			//canvas是根据surfaceHolder得到的, 最后一次surfaceView已经销毁, canvas当然也不存在了.
	    	if (canvas != null)
				//3. 将画布解锁并显示在屏幕上
				surfaceHolder.unlockCanvasAndPost(canvas);
		}

	}

    class MyThread extends Thread
  {
	public void run()
	{
		//每隔100ms刷新屏幕
		while (isThreadRunning)
		{
			drawVieW();
			try
			{
				Thread.sleep(100);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
  }
	/*
	 * 这个是第二种方法解决退出是报错的问题. 当按下返回键时, 提前设置isThreadRunning为false, 让线程结束.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			isThreadRunning = false;
		}
		return super.onKeyDown(keyCode, event);
	}
	*/
}