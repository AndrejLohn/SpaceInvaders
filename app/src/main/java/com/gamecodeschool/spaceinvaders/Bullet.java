package com.gamecodeschool.spaceinvaders;

import android.graphics.RectF;

/**
 * Represents a single bullet. Includes all functionality to fire the bullet and to perform
 * collision detection. The player will have one bullet thus will be able to only fire a single
 * shot at a time. For the invaders an array of bullets will be prepared to allow them to fire more
 * rapidly.
 *
 * @version %I%, %G%
 */
public class Bullet {

    public final int UP = 0;
    public final int DOWN = 1;


    private RectF rect;
    private float x;
    private float y;
    private int width = 1;
    private int height;

    int heading = -1;
    float speed =  350;

    private boolean isActive;

    /**
     * Creates the bullet. Its size is set depending on the devices screen height.
     *
     * @param screenY   the device screen size in y-direction
     */
    public Bullet(int screenY) {
        height = screenY / 20;
        isActive = false;

        rect = new RectF();
    }

    /**
     * Gets the bullets bounding box.
     *
     * @return  the bullets bounding box
     */
    public RectF getRect() {
        return  rect;
    }

    /**
     * Gets the bullets activity status. This is used to determine if the player can fire a bullet
     * and to limit the amount of invader bullets on the screen.
     *
     * @return  <code>true</code> if the bullet is active, i.e. on the screen
     *          <code>false</code> else
     */
    public boolean getStatus() {
        return isActive;
    }

    /**
     * Flags the bullet as inactive. This will be called after the bullet hits a target or the
     * screen edge.
     */
    public void setInactive() {
        isActive = false;
    }

    /**
     * Gets the bullet tips y-coordinate. This depends on the bullets direction of travel and
     * thus will be the top most point for a bullet fired py the player or the bottom most point
     * for an invader bullet.
     *
     * @return  the bullet tips y-coordinate
     */
    public float getImpactPointY() {
        if(heading == DOWN) {
            return y + height;
        } else {
            return  y;
        }
    }

    /**
     * Fires a ready bullet from a given starting point into a given direction.
     *
     * @param startX    the starting point x-coordinate
     * @param startY    the starting point y-coordinate
     * @param direction the shooting direction
     *                  0 = UP
     *                  1 = DOWN
     * @return          <code>true</code> if the bullet was fired
     *                  <code>false</code> else
     */
    public boolean shoot(float startX, float startY, int direction) {
        if(!isActive) {
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }
        return false;
    }

    /**
     * Updates the bullets position based on its speed and the time passed since the last update.
     *
     * @param fps   the time since the last update
     */
    public void update(long fps) {
        if (heading == UP) {
            y = y - speed / fps;
        } else {
            y = y + speed / fps;
        }

        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
}
