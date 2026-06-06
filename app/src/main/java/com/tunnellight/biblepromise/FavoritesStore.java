package com.tunnellight.biblepromise;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Persists the user's favorited verses in SharedPreferences. Verses are keyed by
 * their reference (e.g. "Psalm 23:1"), which stays stable even if the bundled
 * collection is reordered.
 */
final class FavoritesStore {

    private static final String PREFS = "promises_prefs";
    private static final String KEY_FAVORITES = "favorite_refs";

    private FavoritesStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** The references of every favorited verse (a copy, safe to read). */
    static Set<String> references(Context context) {
        return new HashSet<>(prefs(context).getStringSet(KEY_FAVORITES, Collections.emptySet()));
    }

    static boolean isFavorite(Context context, Verse verse) {
        return references(context).contains(verse.reference);
    }

    /** Flips the favorite state of {@code verse}. */
    static void toggle(Context context, Verse verse) {
        Set<String> refs = references(context);
        if (!refs.add(verse.reference)) {
            refs.remove(verse.reference);
        }
        prefs(context).edit().putStringSet(KEY_FAVORITES, refs).apply();
    }
}
