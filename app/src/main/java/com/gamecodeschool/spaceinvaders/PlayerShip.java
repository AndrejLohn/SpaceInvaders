package com.gamecodeschool.spaceinvaders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

/**
 * Representation of the players ship. Includes all necessary functionality to move the ship
 * and fire at the invaders.
 *
 * @version %I%, %G%
 */
public class PlayerShip {

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private RectF rect;
    private float x;
    private float y;
    private float length;
    private float height;

    private Bitmap bitmap;

    private int shipMoving = STOPPED;
    private float shipSpeed;

    /**
     * Creates the player ship and sets its bounding box for hit detection.
     *
     * @param context   the applications instance state
     * @param screenX   the screens size in x-direction
     * @param screenY   the screens size in y-direction
     * @see             Context
     * @see             RectF
     * @see             BitmapFactory#decodeResource(Resources, int)
     * @see             Bitmap#createScaledBitmap(Bitmap, int, int, boolean)
     */
    public PlayerShip(Context context, int screenX, int screenY){

        rect = new RectF();
        length = screenX/10;
        height = screenY/10;

        x = (screenX-length) / 2;
        y = screenY - height;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        shipSpeed = 350;
    }

    /**
     * Gets the ships surrounding rectangle, its bounding box.
     *
     * @return  the ships surrounding rectangle
     */
    public RectF getRect(){
        return rect;
    }

    /**
     * Gets the ships bitmap.
     *
     * @return  the bitmap
     */
    public Bitmap getBitmap(){
        return bitmap;
    }

    /**
     * Gets the ships x-coordinate.
     *
     * @return  the ships x-position
     */
    public float getX(){
        return x;
    }

    /**
     * Gets the ships size in x-direction.
     *
     * @return  the ships length
     */
    public float getLength(){
        return length;
    }

    /**
     * Gets the ships size in y-direction.
     *
     * @return  the ships height
     */
    public float getHeight(){
        return height;
    }

    /**
     * Sets the ships movement state, i.e. its moving direction.
     *
     * @param state the movement state
     *              0 = STOPPED - no movement
     *              1 = LEFT - left
     *              2 = RIGHT - right
     */
    public void setMovementState(int state) {
        shipMoving = state;
    }

    /**
     * Updates the ships position and bounding box according to its movement state
     * and the time passed since the last update.
     *
     * @param fps   the time passed since the last update
     */
    public void update(long fps){
        float nextPosition = -1;
        if(shipMoving == LEFT){
            nextPosition = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            nextPosition = x + shipSpeed / fps;
        }

        if(0 <= nextPosition && nextPosition <= length*9) {
            x = nextPosition;
        }
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }
}
