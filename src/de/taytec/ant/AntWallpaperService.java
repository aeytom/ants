package de.taytec.ant;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class AntWallpaperService extends WallpaperService
{
	public final static String logtag = "Ants";
	
	private long cfgDrawInterval = 500;	// msec
	private int cfgScale = 16;
	private int cfgIterationsPerInterval = 500;

	public WallpaperService.Engine onCreateEngine()
	{
		return new AntWallpaperServiceEngine();
	}

	/**
	 * @author tay
	 *
	 */
	/**
	 * @author tay
	 *
	 */
	private class AntWallpaperServiceEngine extends Engine implements SurfaceHolder.Callback
	{
		private int direction = 0;
		private Bitmap bitmap;
		private int mWidth;
		private int mHeight;
		private int posX;
		private int posY;
		private int colorNull = Color.CYAN;
		private int colorOne = Color.BLUE;
		private Paint paint = new Paint();
		private boolean visible = true;
		private final Handler handler = new Handler(); 
		private final Runnable drawRunner = new Runnable() { 
			@Override 
			public void run()
			{ 
				draw(); 
			}
		};

		private int mPixelOffsetX;
		private Bitmap backBitmap;
		private Canvas backCanvas;

		
		/* (non-Javadoc)
		 * @see android.service.wallpaper.WallpaperService.Engine#onCreate(android.view.SurfaceHolder)
		 */
		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			
			mWidth = getDesiredMinimumWidth() / cfgScale;
			mHeight = getDesiredMinimumHeight() / cfgScale;

			bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);

			posX = mWidth / 2;
			posY = mHeight / 2;
			
			new RadialGradient(getDesiredMinimumWidth() / 2, getDesiredMinimumHeight() / 2, cfgScale, 
				0xFF000000, 0x80000000, Shader.TileMode.CLAMP);
				
			surfaceHolder.addCallback(this);
			handler.post(drawRunner);
		}


		/* (non-Javadoc)
		 * @see android.service.wallpaper.WallpaperService.Engine#onVisibilityChanged(boolean)
		 */
		@Override 
		public void onVisibilityChanged(boolean visible)
		{ 
			this.visible = visible;
			if (visible)
			{ 
				handler.post(drawRunner); 
			}
			else
			{ 
				handler.removeCallbacks(drawRunner);
			}
		}


		/* (non-Javadoc)
		 * @see android.service.wallpaper.WallpaperService.Engine#onOffsetsChanged(float, float, float, float, int, int)
		 */
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
									 float xOffsetStep, float yOffsetStep,
									 int xPixelOffet, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, 
								   xOffsetStep, yOffsetStep, 
								   xPixelOffet, yPixelOffset);

			mPixelOffsetX = (int)(xOffset * backBitmap.getWidth());

			Log.d(logtag, "onOffsetChanged() xo:" + xOffset + " yo:" + yOffset 
				  + " xos:" + xOffsetStep + " yos:" + yOffsetStep 
				  + " xpo:" + xPixelOffet + " ypo:" + yPixelOffset);
		}

		
		/* (non-Javadoc)
		 * @see android.service.wallpaper.WallpaperService.Engine#onTouchEvent(android.view.MotionEvent)
		 */
		@Override
		public void onTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				posX = (int)(mPixelOffsetX + event.getX()) / cfgScale;
				posY = (int)event.getY() / cfgScale;
				Log.d(logtag, "Pos x: " + posX + " y: " + posY);
			}
		}

		

		/**
		 * 
		 */
		public void draw()
		{
			Canvas canvas = getSurfaceHolder().lockCanvas();
			if (canvas == null)
			{
				return;
			}

			Log.d(logtag, "x: " + posX + " y: " + posY + " bw: " + bitmap.getWidth());
			int color;
			for (int loop = cfgIterationsPerInterval; loop > 0; loop --)
			{
				if (bitmap.getPixel(posX, posY) == colorOne)
				{
					color = colorNull;
					direction ++;
				}
				else
				{
					color = colorOne;
					direction --;
				}
				paint.setShader(new RadialGradient(posX * cfgScale, posY * cfgScale, cfgScale, color, Color.TRANSPARENT, Shader.TileMode.CLAMP));
				bitmap.setPixel(posX, posY, color);
				backCanvas.drawCircle((float)posX * cfgScale, (float)posY * cfgScale, cfgScale, paint);
				direction = direction % 4;
				if (direction < 0)
				{
					direction += 4;
				}

				switch (direction)
				{
					case 0:
						posY --;
						if (posY < 0)
						{
							posY = mHeight - 1;
						}
						break;
					case 1:
						posX ++;
						if (posX >= mWidth)
						{
							posX = 0;
						}
						break;
					case 2:
						posY ++;
						if (posY >= mHeight)
						{
							posY = 0;
						}
						break;
					case 3:
						posX --;
						if (posX < 0)
						{
							posX = mWidth - 1;
						}
						break;
				}
			}

			canvas.drawBitmap(backBitmap, 
					new Rect(mPixelOffsetX, 0, mPixelOffsetX + backBitmap.getWidth()/2 - 1, backBitmap.getHeight()-1), 
					canvas.getClipBounds(), 
					null);

			getSurfaceHolder().unlockCanvasAndPost(canvas);
			handler.removeCallbacks(drawRunner); 
			if (visible)
			{ 
				handler.postDelayed(drawRunner, cfgDrawInterval ); 
			}
		}


		/*
		 * methods of interface SurfaceHolder.Callback
		 */

		/* (non-Javadoc)
		 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
		 */
		public void surfaceCreated(SurfaceHolder surfaceHolder)
		{
			// TODO: Implement this method
		}

		
		/* (non-Javadoc)
		 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
		 */
		public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height)
		{
			Log.d(logtag, "surfaceChanged() f: " + format + " w: " + width + " h: " + height);
			
			backBitmap = Bitmap.createBitmap(2 * width, height, Bitmap.Config.ARGB_8888);
			backCanvas = new Canvas(backBitmap);
			
			mWidth = 2 * width / cfgScale;
			mHeight = height / cfgScale;
			posX = mWidth / 2;
			posY = mHeight / 2;
			bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
		}

		/* (non-Javadoc)
		 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
		 */
		public void surfaceDestroyed(SurfaceHolder p1)
		{
			// TODO: Implement this method
		}

	}
}
