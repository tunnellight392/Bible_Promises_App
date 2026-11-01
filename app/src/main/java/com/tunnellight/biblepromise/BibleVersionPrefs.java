package com.tunnellight.biblepromise;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the user's chosen Bible version (translation) for verse text. The
 * stored value doubles as the index into the {@code bible_version_options}
 * label array shown in the chooser dialog.
 */
final class BibleVersionPrefs {

    private static final String PREFS = "promises_prefs";
    private static final String KEY_VERSION = "bible_version";

    /** World English Bible (public domain), the default. */
    static final int WEB = 0;
    /** Malayalam Sathyavedapusthakam / Old Version (public domain). */
    static final int MALAYALAM = 1;
    /** King James Version (public domain). */
    static final int KJV = 2;

    private BibleVersionPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** The selected version; also the index into the label array. */
    static int get(Context context) {
        return prefs(context).getInt(KEY_VERSION, WEB);
    }

    static void set(Context context, int version) {
        prefs(context).edit().putInt(KEY_VERSION, version).apply();
    }

    /** Short translation tag shown after a reference, e.g. "WEB" or "സത്യവേദപുസ്തകം". */
    static int tagRes(int version) {
        switch (version) {
            case MALAYALAM:
                return R.string.translation_tag_malayalam;
            case KJV:
                return R.string.translation_tag_kjv;
            default:
                return R.string.translation_tag_web;
        }
    }

    /** The scripture attribution line for the given version. */
    static int attributionRes(int version) {
        switch (version) {
            case MALAYALAM:
                return R.string.scripture_attribution_malayalam;
            case KJV:
                return R.string.scripture_attribution_kjv;
            default:
                return R.string.scripture_attribution;
        }
    }
}
