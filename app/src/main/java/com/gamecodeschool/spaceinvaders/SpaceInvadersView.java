package com.gamecodeschool.spaceinvaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Holds and draws this games elements. Handles player input and game object collision detection.
 *
 * @version %I%, %G%
 * @see     SurfaceView
 * @see     Runnable
 */
public class SpaceInvadersView extends SurfaceView implements Runnable {

    private Context context;
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;

    private volatile boolean playing;
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    private long fps;
    private long timeThisFrame;

    private int screenX;
    private int screenY;

    private HUD hud;

    private PlayerShip playerShip;
    private Bullet bullet;
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;
    private Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;

    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    public static final String PREFS_NAME = "com.gamecodeschool.spaceinvaders.PrefsFile";
    private int highScore;
    int score = 0;
    private int lives = 3;

    private long menaceInterval = 1000;
    private boolean uhOrOh;
    private long lastMenaceTime = System.currentTimeMillis();

    /**
     * Constructs this SpaceInvadersView
     *
     * @param context   the instance state to create or restore
     * @param x         the screens size in x-direction
     * @param y         the screens size in y-direction
     * @see             Context
     * @see             SurfaceView
     * @see             SurfaceHolder
     * @see             SharedPreferences
     * @see             SurfaceHolder
     * @see             Paint
     * @see             HUD
     * @see             SoundPool
     * @see             AssetManager
     * @see             AssetFileDescriptor
     * @see             Log
     * @see             #prepareLevel()
     * @see             SurfaceView#getHandler()
     * @see             Context#getSharedPreferences(String, int)
     * @see             Context#getAssets()
     * @see             SharedPreferences#getInt(String, int)
     * @see             AssetManager#openFd(String)
     * @see             SoundPool#load(String, int)
     * @see             Log#e(String, String)
     */
    public SpaceInvadersView(Context context, int x, int y) {
        super(context);

        this.context = context;

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = settings.getInt("highScore", 0);

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        hud = new HUD(screenX, screenY);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    /**
     * Initializes the game objects.
     *
     * @see PlayerShip
     * @see Bullet
     * @see DefenceBrick
     */
    private void prepareLevel() {

        menaceInterval = 1000;
        playerShip = new PlayerShip(context, screenX, screenY);
        bullet = new Bullet(screenY);

        for(int i=0; i< invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        numInvaders = 0;
        for(int row=0; row<5; row++) {
            for(int column=0; column<6; column++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders++;
            }
        }

        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
            for(int column = 0; column < 10; column ++ ) {
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }
    }

    /**
     * Handles the game loop of update() and draw() thus running the game.
     *
     * @see #update()
     * @see #draw()
     * @see Runnable#run()
     * @see System#currentTimeMillis()
     * @see SoundPool#play(int, float, float, int, int, float)
     */
    @Override
    public void run() {

        while (playing) {
            long startFrameTime = System.currentTimeMillis();

            if(!paused){
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        soundPool.play(uhID, 1, 1, 0, 0, 1);
                    } else {
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    lastMenaceTime = System.currentTimeMillis();
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    /**
     * Updates the view based on the time passed, player input and game actions.
     * Checks for bullet hits, necessary invader drop downs, victory and loss conditions.
     * Handles the invader shooting logic.
     *
     * @see #prepareLevel()
     * @see Invader#update(long)
     * @see Invader#takeAim(float, float)
     * @see Invader#getVisibility()
     * @see Invader#dropDownAndReverse()
     * @see Invader#getRect()
     * @see PlayerShip#update(long)
     * @see PlayerShip#getRect()
     * @see Bullet#update(long)
     * @see Bullet#shoot(float, float, int)
     * @see Bullet#getStatus()
     * @see Bullet#setInactive()
     * @see Bullet#getImpactPointY()
     * @see DefenceBrick#getVisibility()
     * @see DefenceBrick#setInvisible()
     */
    private void update(){

        boolean bumped = false;
        boolean lost = false;

        playerShip.update(fps);

        // Update the invaders if visible and make them shoot
        for(int i = 0; i < numInvaders; i++){
            if(invaders[i].getVisibility()) {
                invaders[i].update(fps);

                if(invaders[i].takeAim(playerShip.getX(), playerShip.getLength())){
                    if(invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {
                        nextBullet++;

                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0;
                        }
                    }
                }

                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0){
                    bumped = true;
                }
            }
        }

        // Update all the invaders bullets if active
        for(int i=0; i<invadersBullets.length; i++) {
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // Check for invaders bumping into the screen edge, drop them down if necessary
        // Check for successful invasion
        if(bumped) {
            for(int i = 0; i < numInvaders; i++){
                invaders[i].dropDownAndReverse();
                if(invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }
            menaceInterval = menaceInterval - 80;
        }

        if(lost){
            prepareLevel();
        }

        if(bullet.getStatus()) {
            bullet.update(fps);
        }

        if(bullet.getImpactPointY() < 0) {
            bullet.setInactive();
        }

        for(int i=0; i<invadersBullets.length; i++) {
            if(invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }

        // Check for a successful player shot and for player victory
        if(bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 10;

                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }

        // Check if an alien bullet hit a shelter brick
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                for(int j = 0; j < numBricks; j++){
                    if(bricks[j].getVisibility()){
                        if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())){
                            // A collision has occurred
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }

        // Check if the player bullet hit a shelter brick
        if(bullet.getStatus()){
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bullet.getRect(), bricks[i].getRect())){
                        // A collision has occurred
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // check if an invader bullet hit the player ship and resulting game loss
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())){
                    invadersBullets[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    if(lives == 0){
                        if(score > highScore) {
                            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("highScore", score);
                            editor.commit();
                            highScore = score;
                        }

                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();
                    }
                }
            }
        }
    }

    /**
     * Draws all game objects to the drawing surface.
     *
     * @see RectF
     * @see DefenceBrick#getVisibility()
     * @see Invader#getVisibility()
     * @see Bullet#getStatus()
     * @see Surface#isValid()
     * @see SurfaceHolder#lockCanvas()
     * @see SurfaceHolder#unlockCanvasAndPost(Canvas)
     * @see Paint#setColor(int)
     * @see Canvas#drawColor(int)
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     * @see Canvas#drawRect(Rect, Paint)
     * @see Canvas#drawText(String, float, float, Paint)
     * @see Canvas#drawRoundRect(RectF, float, float, Paint)
     */
    private void draw(){
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 0, 0, 0));

            paint.setColor(Color.argb(255,  255, 255, 255));


            // Draw the player ship
            canvas.drawBitmap(
                    playerShip.getBitmap(),
                    playerShip.getX(),
                    screenY-playerShip.getHeight(),
                    paint);

            // Draw the invaders
            for(int i = 0; i < numInvaders; i++) {
                if(invaders[i].getVisibility()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(
                                invaders[i].getBitmap(),
                                invaders[i].getX(),
                                invaders[i].getY(),
                                paint);
                    } else {
                        canvas.drawBitmap(
                                invaders[i].getBitmap2(),
                                invaders[i].getX(),
                                invaders[i].getY(),
                                paint);
                    }
                }
            }

            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // Draw the players bullet if active
            if(bullet.getStatus()) {
                canvas.drawRect(bullet.getRect(), paint);
            }

            // Draw the invaders bullets if active
            for(int i=0; i<invadersBullets.length; i++) {
                if(invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // Draw the score, remaining lives and high score
            paint.setColor(Color.argb(255,  249, 129, 0));
            paint.setTextSize(90);
            canvas.drawText(
                    "Score: " + score + "   Lives: " + lives + "   High Score: " + highScore,
                    10,
                    90,
                    paint);

            // Draw buttons
            paint.setColor(Color.argb(80, 255, 255, 255));

            for (Rect rect : hud.currentButtonList) {
                RectF rf = new RectF(rect.left, rect.top, rect.right, rect.bottom);
                canvas.drawRoundRect(rf, 15f, 15f, paint);
            }

            // Draw button Text
            paint.setColor(Color.argb(255,  249, 129, 0));
            paint.setTextSize(hud.buttonWidth/3);
            canvas.drawText(
                    "L",
                    hud.left.left+hud.buttonWidth*12/32,
                    hud.left.top+hud.buttonHeight*2/3,
                    paint);
            canvas.drawText(
                    "R",
                    hud.right.left+hud.buttonWidth*12/32,
                    hud.right.top+hud.buttonHeight*2/3,
                    paint);
            canvas.drawText(
                    "FIRE!",
                    hud.shoot.left+hud.buttonWidth/8,
                    hud.shoot.top+hud.buttonHeight*2/3,
                    paint);
            if(paused) {
                canvas.drawText(
                        "Play",
                        hud.pause.left+hud.buttonWidth*3/16,
                        hud.pause.top+hud.buttonHeight*2/3,
                        paint);
            } else {
                canvas.drawText(
                        "Pause",
                        hud.pause.left+hud.buttonWidth/32,
                        hud.pause.top+hud.buttonHeight*2/3,
                        paint);
            }

            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Executed on game pause. Shuts down the thread.
     *
     * @see Thread#join()
     */
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    /**
     * Executed on game start or resume. Starts the thread.
     *
     * @see Thread#start()
     */
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.

    /**
     * OnTouchListener inherited from SurfaceView. Forwards screen touches to the HUD.
     *
     * @param motionEvent   the touch event, represents player input
     * @return              <code>true</code> always
     * @see                 MotionEvent
     * @see                 SurfaceView#onTouchEvent(MotionEvent)
     * @see                 HUD#handleInput(MotionEvent, int)
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        for(int i=0; i<motionEvent.getPointerCount(); i++) {
            hud.handleInput(motionEvent, i);
        }
        return true;
    }

    /**
     * Holds all objects to recognize and handle player input.
     *
     * @version %I%, %G%
     */
    class HUD {

        Rect left;
        Rect right;
        Rect shoot;
        Rect pause;

        int buttonWidth;
        int buttonHeight;
        int buttonPadding;

        public ArrayList<Rect> currentButtonList = new ArrayList<>();

        /**
         * Constructs this HUD. Sets button sizes appropriately.
         *
         * @param screenWidth   the devices screen width
         * @param screenHeight  the devices screen height
         * @see                 Rect
         * @see                 ArrayList#add(Object)
         */
        HUD(int screenWidth, int screenHeight) {
            buttonWidth = screenWidth / 8;
            buttonHeight = screenHeight / 7;
            buttonPadding = screenWidth / 80;

            left = new Rect(buttonPadding,
                    screenHeight - buttonHeight - buttonPadding,
                    buttonWidth,
                    screenHeight - buttonPadding);

            right = new Rect(buttonWidth + buttonPadding,
                    screenHeight - buttonHeight - buttonPadding,
                    buttonWidth + buttonPadding + buttonWidth,
                    screenHeight - buttonPadding);

            shoot = new Rect(screenWidth - buttonWidth - buttonPadding,
                    screenHeight - buttonHeight - buttonPadding,
                    screenWidth - buttonPadding,
                    screenHeight - buttonPadding);

            pause = new Rect(screenWidth - buttonPadding - buttonWidth,
                    buttonPadding,
                    screenWidth - buttonPadding,
                    buttonPadding + buttonHeight);

            currentButtonList.add(left);
            currentButtonList.add(right);
            currentButtonList.add(shoot);
            currentButtonList.add(pause);
        }

        /**
         * Handles screen touches and player input.
         * Starts and pauses the game, moves the player ship and fires bullets.
         *
         * @param motionEvent   the touch event, represents player input
         * @param index         the touch event pointer index to handle multiple
         *                      simultaneous screen touches
         * @see                 MotionEvent
         * @see                 MotionEvent#getX()
         * @see                 MotionEvent#getY()
         * @see                 SurfaceView#onTouchEvent(MotionEvent)
         * @see                 Rect#contains(int, int)
         * @see                 HUD#handleInput(MotionEvent, int)
         * @see                 PlayerShip#setMovementState(int)
         * @see                 Bullet#shoot(float, float, int)
         * @see                 SoundPool#play(int, float, float, int, int, float)
         */
        public void handleInput(MotionEvent motionEvent, int index) {

            int x = (int) motionEvent.getX(index);
            int y = (int) motionEvent.getY(index);

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (right.contains(x, y)) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else if (left.contains(x, y)) {
                        playerShip.setMovementState(playerShip.LEFT);
                    } else if (shoot.contains(x, y)) {
                        if(bullet.shoot(
                                playerShip.getX()+playerShip.getLength()/2,
                                screenY-playerShip.getHeight(),
                                bullet.UP)) {
                            soundPool.play(
                                    shootID,
                                    1,
                                    1,
                                    0,
                                    0,
                                    1);
                        }
                    } else if(pause.contains(x, y)) {
                        paused = !paused;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (right.contains(x, y) || left.contains(x, y)){
                        playerShip.setMovementState(playerShip.STOPPED);
                    }
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    if (shoot.contains(x, y)) {
                        if(bullet.shoot(
                                playerShip.getX()+playerShip.getLength()/2,
                                screenY,
                                bullet.UP)) {
                            soundPool.play(
                                    shootID,
                                    1,
                                    1,
                                    0,
                                    0,
                                    1);
                        }
                    } else if(pause.contains(x, y)) {
                        paused = !paused;
                    }
                    break;
            }
        }
    }
}