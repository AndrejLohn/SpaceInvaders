package com.gamecodeschool.spaceinvaders;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

/**
 * Activity for loading and displaying this space invaders game.
 * The entry point to the game. Handles the lifecycle of the game by calling
 * methods of spaceInvadersView when prompted to do so by the OS.
 *
 * @version %I%, %G%
 * @see     Activity
 */
public class SpaceInvadersActivity extends Activity {

    /**
     * The game view. Will hold this games logic and respond to user input.
     */
    SpaceInvadersView spaceInvadersView;

    /**
     * Executes on this Activities creation.
     * Sets up this SpaceInvadersView.
     *
     * @param savedInstanceState    the instance state to create or restore
     * @see                         Activity#onCreate(Bundle)
     * @see                         Activity#setContentView(View)
     * @see                         Bundle
     * @see                         View
     * @see                         Display
     * @see                         Point
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);


        spaceInvadersView = new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);

    }

    /**
     * Executes on focusing on the app.
     *
     * @see Activity#onResume()
     * @see SpaceInvadersView#resume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        spaceInvadersView.resume();
    }

    /**
     * Executes on the game losing focus or being quit.
     *
     * @see Activity#onPause()
     * @see SpaceInvadersView#pause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        spaceInvadersView.pause();
    }
}
