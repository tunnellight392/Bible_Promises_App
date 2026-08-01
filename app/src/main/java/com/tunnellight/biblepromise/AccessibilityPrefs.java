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

    /**
     * Verse-body multipliers for Malayalam. Malayalam stacks vowel marks above and
     * below the line, so the script is taller and its (often longer) verses would
     * overflow the fixed verse area at the larger sizes; these smaller scales keep
     * even the largest setting on screen. Same length/order as {@link #SIZE_SCALES}.
     */
    static final float[] MALAYALAM_SIZE_SCALES = {0.7f, 0.8f, 0.85f, 0.9f};

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

    /**
     * The multiplier for the verse body text at the current size setting, which
     * depends on the Bible version — Malayalam ({@link BibleVersionPrefs#MALAYALAM})
     * uses {@link #MALAYALAM_SIZE_SCALES} so its taller script still fits the screen.
     */
    static float getVerseSizeScale(Context context, int bibleVersion) {
        int index = getSizeIndex(context);
        return bibleVersion == BibleVersionPrefs.MALAYALAM
                ? MALAYALAM_SIZE_SCALES[index]
                : SIZE_SCALES[index];
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
