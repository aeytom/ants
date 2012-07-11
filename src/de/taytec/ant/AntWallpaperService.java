package de.taytec.ant;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.util.*;
import android.view.*;

public class AntWallpaperService extends WallpaperService
{

	public WallpaperService.Engine onCreateEngine()
	{
		return new AntWallpaperServiceEngine();
	}


	private class AntWallpaperServiceEngine extends Engine
	{
		private int scale = 8;
		private int direction;
		private Bitmap bitmap;
		private int width;
		private int height;
		private int posX;
		private int posY;
		private int colorNull = Color.CYAN;
		private int colorOne = Color.BLUE;
		private boolean visible = true;
		private final Handler handler = new Handler(); 
		private final Runnable drawRunner = new Runnable() { 
			@Override 
			public void run()
			{ 
				draw(); 
			}
		};



		AntWallpaperServiceEngine()
		{
			handler.post(drawRunner);
		}


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


		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			direction = 0;
			width = getDesiredMinimumWidth() / scale;
			height = getDesiredMinimumHeight() / scale;
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			
			posX = width / 2;
			posY = width / 2;

			super.onCreate(surfaceHolder);
		}


		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
									 float xOffsetStep, float yOffsetStep,
									 int xPixelOffet, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, 
								   xOffsetStep, yOffsetStep, 
								   xPixelOffet, yPixelOffset);
			Log.d("ants", "onOffsetChanged() xo:"+xOffset+" yo:"+yOffset+" xos:"+xOffsetStep+" yos:"+yOffsetStep+" xpo:"+xPixelOffet+" ypo:"+yPixelOffset);
		}


		@Override
		public void onTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
			Log.d("ants", "onTouchEvent() " + event.toString());
		}


		public void draw()
		{
			int minX = posX, minY = posY, maxX = posX, maxY = posY;
			
			SurfaceHolder surfaceHolder = getSurfaceHolder();
			Canvas canvas = surfaceHolder.lockCanvas();
			if (canvas.getDensity() == Bitmap.DENSITY_NONE)
			{
				Log.d("Ants", "set bitmap");
				canvas.setBitmap(bitmap);
			}

			for (int loop = 10; loop > 0; loop --)
			{
				if (bitmap.getPixel(posX, posY) == colorNull)
				{
					bitmap.setPixel(posX, posY, colorOne);
					direction ++;
				}
				else
				{
					bitmap.setPixel(posX, posY, colorNull);
					direction --;
				}
				direction = direction % 4;
				if (direction < 0)
				{
					direction += 4;
				}

				switch (direction)
				{
					case 0:
						posY --;
						minY = Math.min(minY, posY);
						if (posY < 0)
						{
							posY = height - 1;
							minY = 0;
							maxY = height - 1;
						}
						break;
					case 1:
						posX ++;
						maxX = Math.max(maxX,posX);
						if (posX >= width)
						{
							posX = 0;
							minX = 0;
							maxX = width - 1;
						}
						break;
					case 2:
						posY ++;
						maxY = Math.max(maxY,posY);
						if (posY >= height)
						{
							posY = 0;
							minY = 0;
							maxY = height - 1;
						}
						break;
					case 3:
						posX --;
						minX = Math.min(minX, posX);
						if (posX < 0)
						{
							posX = width - 1;
							minX = 0;
							maxX = width - 1;
						}
						break;
				}
			}
			
			int cWidth = canvas.getWidth();
			int cHeight = canvas.getHeight();
			Log.d("Ants", "w:"+cWidth+" h:"+cHeight+" d: "+canvas.getDensity());
/*	
			canvas.drawBitmap(bitmap, 
				null, 
				new Rect(0, 0, getDesiredMinimumWidth(), getDesiredMinimumHeight()), 
				null);
*/
				/*
			canvas.drawBitmap(bitmap, 
							  new Rect(minX,minY,maxX,maxY), 
							  new Rect(minX*scale, minY*scale, maxX*scale, maxY*scale), 
							  null);
			*/
			surfaceHolder.unlockCanvasAndPost(canvas);
			handler.removeCallbacks(drawRunner); 
			if (visible)
			{ 
				handler.postDelayed(drawRunner, 1000); 
			}
		}
	}
}
