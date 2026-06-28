package com.tunnellight.biblepromise;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

/**
 * Stores the user's reading preferences from the Accessibility screen: a font
 * size scale and a font family, applied to the verse text on the main screen.
 */
final class AccessibilityPrefs {

    private static final String PREFS = "promises_prefs";
    private static final String KEY_SIZE_INDEX = "font_size_index";
    private static final String KEY_FONT_TYPE = "font_type";

    /** Size multipliers applied to the verse's base text sizes. */
    static final float[] SIZE_SCALES = {0.85f, 1.0f, 1.2f, 1.45f};
    static final int DEFAULT_SIZE_INDEX = 1; // "Medium"

    /** Font family options; indices are persisted, so keep their order stable. */
    static final int FONT_SERIF = 0;
    static final int FONT_SANS_SERIF = 1;
    static final int FONT_MONOSPACE = 2;
    static final int DEFAULT_FONT_TYPE = FONT_SERIF;

    private AccessibilityPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static int getSizeIndex(Context context) {
        int index = prefs(context).getInt(KEY_SIZE_INDEX, DEFAULT_SIZE_INDEX);
        return clampSizeIndex(index);
    }

    static void setSizeIndex(Context context, int index) {
        prefs(context).edit().putInt(KEY_SIZE_INDEX, clampSizeIndex(index)).apply();
    }

    /** The multiplier to apply to base text sizes for the current preference. */
    static float getSizeScale(Context context) {
        return SIZE_SCALES[getSizeIndex(context)];
    }

    static int getFontType(Context context) {
        int type = prefs(context).getInt(KEY_FONT_TYPE, DEFAULT_FONT_TYPE);
        return (type >= FONT_SERIF && type <= FONT_MONOSPACE) ? type : DEFAULT_FONT_TYPE;
    }

    static void setFontType(Context context, int fontType) {
        prefs(context).edit().putInt(KEY_FONT_TYPE, fontType).apply();
    }

    /** Builds a typeface for {@code fontType}, preserving the given style (e.g. italic). */
    static Typeface typeface(int fontType, int style) {
        String family;
        switch (fontType) {
            case FONT_SANS_SERIF:
                family = "sans-serif";
                break;
            case FONT_MONOSPACE:
                family = "monospace";
                break;
            case FONT_SERIF:
            default:
                family = "serif";
                break;
        }
        return Typeface.create(family, style);
    }

    private static int clampSizeIndex(int index) {
        if (index < 0) {
            return 0;
        }
        if (index >= SIZE_SCALES.length) {
            return SIZE_SCALES.length - 1;
        }
        return index;
    }
}
