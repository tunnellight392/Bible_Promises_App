package com.tunnellight.biblepromise;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps the English book-name prefix of a verse reference to its Malayalam
 * Sathyavedapusthakam name, so "Isaiah 61:3" can display as its Malayalam form
 * when that version is selected. The English reference stays the stable key for
 * favorites; only the on-screen book name changes (chapter:verse digits are kept).
 */
final class BibleBooks {

    private static final Map<String, String> MALAYALAM = new HashMap<>();

    static {
        MALAYALAM.put("Numbers", "സംഖ്യാപുസ്തകം");
        MALAYALAM.put("Deuteronomy", "ആവര്‍ത്തനപുസ്തകം");
        MALAYALAM.put("Joshua", "യോശുവ");
        MALAYALAM.put("Nehemiah", "നെഹെമ്യാവ്");
        MALAYALAM.put("Psalm", "സങ്കീര്‍ത്തനങ്ങള്‍");
        MALAYALAM.put("Proverbs", "സദൃശവാക്യങ്ങള്‍");
        MALAYALAM.put("Isaiah", "യെശയ്യാവ്");
        MALAYALAM.put("Jeremiah", "യിരെമ്യാവ്");
        MALAYALAM.put("Lamentations", "വിലാപങ്ങള്‍");
        MALAYALAM.put("Micah", "മീഖാ");
        MALAYALAM.put("Zephaniah", "സെഫന്യാവ്");
        MALAYALAM.put("Matthew", "മത്തായി");
        MALAYALAM.put("Mark", "മര്‍ക്കൊസ്");
        MALAYALAM.put("John", "യോഹന്നാന്‍");
        MALAYALAM.put("Acts", "അപ്പൊ. പ്രവൃത്തികള്‍");
        MALAYALAM.put("Romans", "റോമര്‍");
        MALAYALAM.put("1 Corinthians", "1 കൊരിന്ത്യര്‍");
        MALAYALAM.put("2 Corinthians", "2 കൊരിന്ത്യര്‍");
        MALAYALAM.put("Galatians", "ഗലാത്യര്‍");
        MALAYALAM.put("Ephesians", "എഫെസ്യര്‍");
        MALAYALAM.put("Philippians", "ഫിലിപ്പിയര്‍");
        MALAYALAM.put("Colossians", "കൊലൊസ്സ്യര്‍");
        MALAYALAM.put("1 Thessalonians", "1 തെസ്സലൊനീക്യര്‍");
        MALAYALAM.put("2 Timothy", "2 തിമൊഥെയൊസ്");
        MALAYALAM.put("Hebrews", "എബ്രായര്‍");
        MALAYALAM.put("James", "യാക്കോബ്");
        MALAYALAM.put("1 Peter", "1 പത്രൊസ്");
        MALAYALAM.put("1 John", "1 യോഹന്നാന്‍");
        MALAYALAM.put("Revelation", "വെളിപ്പാട്");
    }

    private BibleBooks() {
    }

    /**
     * The reference formatted for display in the given Bible version. For Malayalam
     * the book-name prefix is localized; otherwise (or if the book is unmapped) the
     * English reference is returned unchanged.
     */
    static String localizedReference(String reference, int version) {
        if (version != BibleVersionPrefs.MALAYALAM) {
            return reference;
        }
        int split = reference.lastIndexOf(' ');
        if (split < 0) {
            return reference;
        }
        String malayalam = MALAYALAM.get(reference.substring(0, split));
        return malayalam == null ? reference : malayalam + reference.substring(split);
    }
}
