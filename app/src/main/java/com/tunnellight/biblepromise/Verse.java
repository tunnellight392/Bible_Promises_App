package com.tunnellight.biblepromise;

/** A single Scripture passage, in each supported Bible version, shown on the Promises screen. */
final class Verse {

    final String reference;
    /** World English Bible (WEB) text — the default English version. */
    final String web;
    /** Malayalam Sathyavedapusthakam (O.V.) text. */
    final String malayalam;

    Verse(String reference, String web, String malayalam) {
        this.reference = reference;
        this.web = web;
        this.malayalam = malayalam;
    }

    /** The verse text in the given {@link BibleVersionPrefs} version. */
    String text(int version) {
        return version == BibleVersionPrefs.MALAYALAM ? malayalam : web;
    }

    /**
     * Formatted for sharing, e.g. for a messaging app or social post.
     * {@code tag} is the short translation label shown after the reference (e.g. "WEB").
     */
    String forSharing(int version, String tag) {
        return "“" + text(version) + "”\n— " + reference + " (" + tag + ")";
    }
}
