package com.gamecodeschool.spaceinvaders;

import android.graphics.RectF;

/**
 * Represents a single defensive shelter brick. An array of Bricks forms a single shelter
 * and multiple shelters will be placed in front of the player ship.
 *
 * @version %I%, %G%
 */
public class DefenceBrick {

    private RectF rect;

    private boolean isVisible;

    /**
     * Creates a single defensive shelter brick at a position depending on its respective
     * shelter and index within this shelter.
     *
     * @param row           the bricks shelter row index
     * @param column        the bricks shelter column index
     * @param shelterNumber the bricks shelter number
     * @param screenX       the device screen size in x-direction
     * @param screenY       the device screen size in y-direction
     * @see                 RectF
     */
    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY) {

        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        // Bullets can slip through this padding.
        // Set padding to zero to remove this behavior.
        int brickPadding = 1;

        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY /8 * 2);

        rect = new RectF(column * width + brickPadding +
                (shelterPadding * shelterNumber) +
                shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (shelterPadding * shelterNumber) +
                        shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
    }

    /**
     *Gets the bricks bounding box.
     *
     * @return  the bounding box
     */
    public RectF getRect() {
        return this.rect;
    }

    /**
     * Sets the brick invisible.
     * This is used to flag the brick as no longer in the game.
     */
    public void setInvisible() {
        isVisible = false;
    }

    /**
     * Returns the bricks visibility.
     * This is used to determine if the brick is still in the game.
     *
     * @return  the bricks visibility
     */
    public boolean getVisibility() {
        return isVisible;
    }
}
