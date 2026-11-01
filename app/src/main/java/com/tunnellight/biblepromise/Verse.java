package com.tunnellight.biblepromise;

/** A single Scripture passage, in each supported Bible version, shown on the Promises screen. */
final class Verse {

    final String reference;
    /** World English Bible (WEB) text — the default English version. */
    final String web;
    /** Malayalam Sathyavedapusthakam (O.V.) text. */
    final String malayalam;
    /** King James Version (KJV) text. */
    final String kjv;

    Verse(String reference, String web, String malayalam, String kjv) {
        this.reference = reference;
        this.web = web;
        this.malayalam = malayalam;
        this.kjv = kjv;
    }

    /** The verse text in the given {@link BibleVersionPrefs} version. */
    String text(int version) {
        switch (version) {
            case BibleVersionPrefs.MALAYALAM:
                return malayalam;
            case BibleVersionPrefs.KJV:
                return kjv;
            default:
                return web;
        }
    }

    /**
     * Formatted for sharing, e.g. for a messaging app or social post.
     * {@code tag} is the short translation label shown after the reference (e.g. "WEB").
     */
    String forSharing(int version, String tag) {
        return "“" + text(version) + "”\n— "
                + BibleBooks.localizedReference(reference, version) + " (" + tag + ")";
    }
}
