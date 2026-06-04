package com.tunnellight.biblepromise;

/** A single Scripture passage (NIV) shown on the Promises screen. */
final class Verse {

    final String text;
    final String reference;

    Verse(String text, String reference) {
        this.text = text;
        this.reference = reference;
    }

    /** Formatted for sharing, e.g. for a messaging app or social post. */
    String forSharing() {
        return "“" + text + "”\n— " + reference + " (WEB)";
    }
}
