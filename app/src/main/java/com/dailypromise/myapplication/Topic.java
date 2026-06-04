package com.dailypromise.myapplication;

import java.util.List;

/** A named group of verses, e.g. "Anxiety & Worry" or "Hope". */
final class Topic {

    final String name;
    final List<Verse> verses;

    Topic(String name, List<Verse> verses) {
        this.name = name;
        this.verses = verses;
    }
}
