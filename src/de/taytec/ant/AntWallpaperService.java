package de.taytec.ant;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.util.*;
import android.view.*;
import javax.security.auth.callback.*;
import java.util.*;

public class AntWallpaperService extends WallpaperService
{

	public WallpaperService.Engine onCreateEngine()
	{
		return new AntWallpaperServiceEngine();
	}

	private class AntWallpaperServiceEngine extends Engine implements SurfaceHolder.Callback
	{
		private int scale = 8;
		private int direction = 0;
		private Bitmap bitmap;
		private int width;
		private int height;
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

		private int mPixelOffsetY;
		private int mPixelOffsetX;
		private float moffsetY = 0.5f;
		private float mOffsetX = 0.5f;

		private RadialGradient gradient;

		/**
		 */
		AntWallpaperServiceEngine()
		{
			handler.post(drawRunner);
			width = getDesiredMinimumWidth() / scale;
			height = getDesiredMinimumHeight() / scale;

			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);

			posX = getDesiredMinimumWidth() / 2;
			posY = getDesiredMinimumHeight() / 2;
			
			
			gradient = new RadialGradient(scale/2, scale/2, scale, 
				0xFF000000, 0x00000000, Shader.TileMode.CLAMP);
				
			paint.setAntiAlias(true);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
			paint.setShader(gradient);
		}

		/**
		 */
		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			surfaceHolder.addCallback(this);
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
		public void onOffsetsChanged(float xOffset, float yOffset,
									 float xOffsetStep, float yOffsetStep,
									 int xPixelOffet, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, 
								   xOffsetStep, yOffsetStep, 
								   xPixelOffet, yPixelOffset);

			mOffsetX = xOffset;
			moffsetY = yOffset;
			mPixelOffsetX = xPixelOffet;
			mPixelOffsetY = yPixelOffset;

			Log.d("ants", "onOffsetChanged() xo:" + xOffset + " yo:" + yOffset 
				  + " xos:" + xOffsetStep + " yos:" + yOffsetStep 
				  + " xpo:" + xPixelOffet + " ypo:" + yPixelOffset);
		}

		

		@Override
		public void onTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
			Log.d("ants", "onTouchEvent() " + event.toString());
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				posX = (int)event.getAxisValue(MotionEvent.AXIS_X) + getDesiredMinimumWidth()/2 + mPixelOffsetX;
				posY = (int)event.getAxisValue(MotionEvent.AXIS_Y) + getDesiredMinimumHeight()/2 + mPixelOffsetY;
				Log.d("ants", "Pos x: " + posX + "y: " + posY);
			}
		}

		

		public void draw()
		{
			SurfaceHolder surfaceHolder = getSurfaceHolder();
			Canvas canvas = surfaceHolder.lockCanvas();
			if (canvas == null)
			{
				return;
			}

			Log.d("Ants", "x: " + posX + " y: " + posY);
			for (int loop = 10; loop > 0; loop --)
			{
				if (bitmap.getPixel(posX/scale, posY/scale) == Color.TRANSPARENT)
				{
					paint.setColor(colorOne);
					bitmap.setPixel(posX/scale, posY/scale, Color.WHITE);
					direction ++;
				}
				else
				{
					paint.setColor(colorNull);
					bitmap.setPixel(posX/scale, posY/scale, Color.TRANSPARENT);
					direction --;
				}
		
				canvas.drawCircle((float)posX, (float)posY, scale/2f, paint);
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
							posY = height - 1;
						}
						break;
					case 1:
						posX ++;
						if (posX >= width)
						{
							posX = 0;
						}
						break;
					case 2:
						posY ++;
						if (posY >= height)
						{
							posY = 0;
						}
						break;
					case 3:
						posX --;
						if (posX < 0)
						{
							posX = width - 1;
						}
						break;
				}
			}


			surfaceHolder.unlockCanvasAndPost(canvas);
			handler.removeCallbacks(drawRunner); 
			if (visible)
			{ 
				handler.postDelayed(drawRunner, 1000); 
			}
		}


		/*
		 * methods of interface SurfaceHolder.Callback
		 */

		public void surfaceCreated(SurfaceHolder surfaceHolder)
		{
			// TODO: Implement this method
		}

		public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4)
		{
			// TODO: Implement this method
		}

		public void surfaceDestroyed(SurfaceHolder p1)
		{
			// TODO: Implement this method
		}

	}
}
