package com.example.spritesheet5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {
private GameView gameView;
int screenX,screenY;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    class GameView extends SurfaceView implements Runnable{
        private Thread gameThread;
        private SurfaceHolder ourHolder;
        private volatile boolean playing;
        Canvas canvas;
        Paint paint;
     Bitmap bitmapRunningMan;
        private boolean isMoving;
        private float runSpeedPerSecond = 250;
        private float manXPos = 10, manYPos=10;
        private int frameWidth = 115, frameHeight = 137;
        private int frameCount = 8;
        private int currentFrame = 0;
        private long fps;
        private long timeThisFrame;
        private long lastFrameChangeTime = 0;
        private int frameLengthInMillisecond = 100;
        private Rect frameToDraw = new Rect(0,0,frameWidth, frameHeight);
        private RectF whereToDraw = new RectF(manXPos, manYPos, manXPos+frameWidth, frameHeight);
        public GameView(Context context){
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            bitmapRunningMan = BitmapFactory.decodeResource(getResources(), R.drawable.running_man);
            bitmapRunningMan = Bitmap.createScaledBitmap(bitmapRunningMan, frameWidth*frameCount, frameHeight, false);
            playing = true;
        }

        @Override
        public void run() {
            while (playing){
                long startFrameTime = System.currentTimeMillis();
                update();
                draw();
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame>=1){
                    fps = 1000 / timeThisFrame;
                }
            }
        }
        public void update(){
            if(isMoving){
                manXPos = manXPos + runSpeedPerSecond /fps;

                if (manXPos > getWidth()){
                    manYPos += (int) frameHeight;
                    manXPos = 10;
                }
                if(manYPos +frameHeight >getHeight())
                {
                    manYPos = 10;
                }
                flip();
                manXPos = manXPos + (runSpeedPerSecond / fps);
            }

        }
        public void managerCurrentFrame(){
            long time = System.currentTimeMillis();
            if(isMoving){
                if(time > lastFrameChangeTime+frameLengthInMillisecond){
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if(currentFrame >= frameCount){
                        currentFrame = 0;
                    }

                }
            }

            frameToDraw.left = currentFrame *frameWidth;
            frameToDraw.right = frameToDraw.left +frameWidth;
        }
        public void draw(){
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);

                paint.setColor(Color.argb(255,115,66,17));
                canvas.drawRect(0, manYPos + frameHeight, screenX, screenY, paint);
                paint.setColor(Color.argb(255,55,156,44));
                canvas.drawRect(0, manYPos + frameHeight, screenX, manYPos + frameHeight + 30, paint);


                paint.setColor(Color.argb(255,249,129,0));
                paint.setTextSize(45);
                whereToDraw.set((int) manXPos, (int) manYPos, (int) manXPos+frameWidth,(int) manYPos +frameHeight);
                managerCurrentFrame();
                canvas.drawBitmap(bitmapRunningMan, frameToDraw, whereToDraw, null);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }
        public void flip() {
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            bitmapRunningMan = Bitmap.createBitmap(bitmapRunningMan, 0, 0, bitmapRunningMan.getWidth(), bitmapRunningMan.getHeight(), matrix, false);
        }
        public void pause(){
            playing = false;
            try {
                gameThread.join();
            }catch (InterruptedException e){
                Log.e("ERR","Joining Thread");
            }
        }
        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()& MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                isMoving = !isMoving;
                break;
            }
            return true;
        }
    }




}