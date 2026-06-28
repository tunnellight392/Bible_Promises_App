package com.tunnellight.biblepromise;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Stores and applies the user's light/dark theme choice. The values are the
 * AppCompatDelegate night-mode constants, so applying is a direct passthrough.
 */
final class ThemePrefs {

    private static final String PREFS = "promises_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    /** Selectable options, in the order shown in the chooser dialog. */
    static final int[] MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, // System
            AppCompatDelegate.MODE_NIGHT_NO,            // Light
            AppCompatDelegate.MODE_NIGHT_YES            // Dark
    };

    private ThemePrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static int getMode(Context context) {
        return prefs(context).getInt(
                KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /** Saves the chosen night mode and applies it immediately (recreates activities). */
    static void setMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /** Applies the saved preference; call from Application.onCreate(). */
    static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(getMode(context));
    }

    /** Index into {@link #MODES} (and the label array) for the saved mode. */
    static int getSelectedIndex(Context context) {
        int mode = getMode(context);
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i] == mode) {
                return i;
            }
        }
        return 0; // default to "System"
    }
}
