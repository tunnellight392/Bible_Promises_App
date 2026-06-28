package com.tunnellight.biblepromise;

import android.app.Application;

/** Applies the saved theme preference before any activity is shown. */
public class PromisesApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePrefs.apply(this);
    }
}
