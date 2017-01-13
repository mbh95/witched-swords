package com.comp460;

import com.badlogic.gdx.Gdx;

/**
 * Created by matthewhammond on 1/12/17.
 */
public class Settings {

    public static int WINDOW_WIDTH = 1280;
    public static int WINDOW_HEIGHT = 720;

    public static void load() {
        Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}
