package com.gamecodeschool.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

/**
 * Represents a single invader. Includes all necessary functionality to move the invader
 * and the logic to determine if the invader should shoot a bullet.
 *
 * @version %I%, %G%
 */
public class Invader {

    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = RIGHT;
    private float shipSpeed;

    private RectF rect;
    private float x;
    private float y;
    private float length;
    private float height;

    private Random generator = new Random();

    private Bitmap bitmap1;
    private Bitmap bitmap2;

    boolean isVisible;

    /**
     * Creates an invader at a position depending on ist index in the moving array of invaders.
     *
     * @param context   the applications instance state
     * @param row       the invaders row index
     * @param column    the invaders column index
     * @param screenX   the devices screen size in x-direction
     * @param screenY   the devices screen size in y-directino
     */
    public Invader(Context context, int row, int column, int screenX, int screenY) {

        rect = new RectF();
        length = screenX / 20;
        height = screenY / 20;
        isVisible = true;
        shipSpeed = 40;

        int padding = screenX / 25;
        x = column * (length + padding);
        y = row * (length + padding/4);

        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);

        bitmap1 = Bitmap.createScaledBitmap(bitmap1,
                (int) (length),
                (int) (height),
                false);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2,
                (int) (length),
                (int) (height),
                false);
    }

    /**
     * Sets the invader invisible.
     * This is used to flag the invader as hit by the player and therefore as no longer in the game.
     */
    public void setInvisible() {
        isVisible = false;
    }

    /**
     * Gets the invaders visibility.
     * Is used to determine if the invader is still in the game.
     *
     * @return  <code>true</code> if the invader is visible
     *          <code>false</code> else
     */
    public boolean getVisibility() {
        return isVisible;
    }

    /**
     * Gets the invaders bounding box.
     *
     * @return  the invaders bounding box
     */
    public RectF getRect() {
        return rect;
    }

    /**
     * Gets the fist bitmap used to display the invader (arms up state).
     *
     * @return  the arms up bitmap
     */
    public Bitmap getBitmap() {
        return bitmap1;
    }

    /**
     * Gets the second bitma used to display the invader (arms down state).
     *
     * @return  the arms down bitmap
     */
    public Bitmap getBitmap2() {
        return bitmap2;
    }

    /**
     * Gets the invaders x-position.
     *
     * @return  the x-position
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the invaders y-position.
     *
     * @return  the y-position
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the invaders size in x-direction.
     *
     * @return  the invaders length
     */
    public float getLength() {
        return length;
    }

    /**
     * Updates the invaders position depending on its speed and the time
     * passed since the last update.
     *
     * @param fps   the time since the last update
     */
    public void update(long fps) {
        if(shipMoving == LEFT) {
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT) {
            x = x + shipSpeed / fps;
        }

        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }

    /**
     * Drops the invader down one row and reverses its moving direction.
     * In addition the ships speed is increased
     */
    public void dropDownAndReverse() {
        if(shipMoving == LEFT) {
            shipMoving = RIGHT;
        } else {
            shipMoving = LEFT;
        }

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }

    /**
     * Determines if an invader shoots a bullet at the player ship.
     * If the invader is above the player ship, i.e. their bounding boxes intersect in x-direction,
     * the invader has a one in 150 chance to fire a bullet.
     * If the invader did not fire a bullet or is not above the player ship, the invader has a one
     * in 2000 chance to fire a bullet.
     *
     * @param playerShipX       the player ship's x-position
     * @param playerShipLength  the player ship's length in x-direction
     * @return                  <code>true</code> if the invader fired a bullet
     *                          <code>false</code> else
     * @see                     Random#next(int)
     */
    public boolean takeAim(float playerShipX, float playerShipLength) {

        int randomNumber;

        // Invader near the player
        if((playerShipX+playerShipLength > x && playerShipX+playerShipLength < x+length) ||
                (playerShipX > x && playerShipX < x+length)) {
            randomNumber = generator.nextInt(150);
            if(randomNumber == 0) {
                return true;
            }
        }

        // Invader far off or did not shoot
        randomNumber = generator.nextInt(2000);
        if(randomNumber == 0) {
            return true;
        }

        return false;
    }
}
