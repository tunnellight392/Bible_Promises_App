package com.tunnellight.biblepromise;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Lets the user browse every verse, grouped by topic, and pick one to view. */
public class BrowseActivity extends AppCompatActivity {

    /** Extra carrying the chosen verse's global index back to MainActivity. */
    static final String EXTRA_VERSE_INDEX = "com.tunnellight.biblepromise.VERSE_INDEX";

    /** Tile background per topic, in the same order as VerseRepository's topics. */
    private static final int[] TOPIC_IMAGES = {
            R.drawable.topic_0, R.drawable.topic_1, R.drawable.topic_2, R.drawable.topic_3,
            R.drawable.topic_4, R.drawable.topic_5, R.drawable.topic_6, R.drawable.topic_7,
            R.drawable.topic_8, R.drawable.topic_9, R.drawable.topic_10, R.drawable.topic_11,
            R.drawable.topic_12, R.drawable.topic_13, R.drawable.topic_14, R.drawable.topic_15
    };

    private final VerseRepository repository = new VerseRepository();

    /** All topics (Favorites + every topic), unfiltered; the search box narrows this. */
    private final List<Topic> allTopics = new ArrayList<>();

    private ExpandableListView list;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        findViewById(R.id.menuButton).setOnClickListener(this::showOverflowMenu);

        list = findViewById(R.id.topicList);
        list.setEmptyView(findViewById(R.id.emptyView));

        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showFilteredTopics(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        list.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            Verse verse = (Verse) parent.getExpandableListAdapter()
                    .getChild(groupPosition, childPosition);
            Intent result = new Intent();
            result.putExtra(EXTRA_VERSE_INDEX, repository.indexOf(verse));
            setResult(RESULT_OK, result);
            finish();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rebuild so favorites toggled on the verse screen show up here.
        allTopics.clear();
        allTopics.addAll(buildTopics());
        showFilteredTopics(searchInput.getText().toString());
    }

    /** Shows only topics whose name contains {@code query} (case-insensitive). */
    private void showFilteredTopics(String query) {
        String trimmed = query.trim().toLowerCase(Locale.getDefault());
        List<Topic> matches;
        if (trimmed.isEmpty()) {
            matches = allTopics;
        } else {
            matches = new ArrayList<>();
            for (Topic topic : allTopics) {
                if (topic.name.toLowerCase(Locale.getDefault()).contains(trimmed)) {
                    matches.add(topic);
                }
            }
        }
        list.setAdapter(new TopicAdapter(matches));

        // While searching, expand matches so suggested topics are easy to scan.
        if (!trimmed.isEmpty()) {
            for (int i = 0; i < matches.size(); i++) {
                list.expandGroup(i);
            }
        }
    }

    /** The topic list shown in Browse: a Favorites group (if any) then all topics. */
    private List<Topic> buildTopics() {
        List<Topic> topics = new ArrayList<>();

        Set<String> favorites = FavoritesStore.references(this);
        if (!favorites.isEmpty()) {
            List<Verse> favoriteVerses = new ArrayList<>();
            for (Verse verse : repository.all()) {
                if (favorites.contains(verse.reference)) {
                    favoriteVerses.add(verse);
                }
            }
            topics.add(new Topic(getString(R.string.favorites), favoriteVerses));
        }

        topics.addAll(repository.topics());
        return topics;
    }

    /** Shows the kebab overflow menu anchored to the menu button. */
    private void showOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.main_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.action_feedback) {
                startActivity(new Intent(this, FeedbackActivity.class));
                return true;
            }
            return false;
        });
        popup.show();
    }

    /** Renders topics as groups and their verses as children. */
    private final class TopicAdapter extends BaseExpandableListAdapter {

        private final List<Topic> topics;
        private final LayoutInflater inflater = LayoutInflater.from(BrowseActivity.this);

        TopicAdapter(List<Topic> topics) {
            this.topics = topics;
        }

        @Override
        public int getGroupCount() {
            return topics.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return topics.get(groupPosition).verses.size();
        }

        @Override
        public Topic getGroup(int groupPosition) {
            return topics.get(groupPosition);
        }

        @Override
        public Verse getChild(int groupPosition, int childPosition) {
            return topics.get(groupPosition).verses.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View row = convertView != null
                    ? convertView
                    : inflater.inflate(R.layout.list_group, parent, false);

            Topic topic = getGroup(groupPosition);
            ((ImageView) row.findViewById(R.id.groupImage))
                    .setImageResource(imageFor(topic));
            ((TextView) row.findViewById(R.id.groupName)).setText(topic.name);
            ((TextView) row.findViewById(R.id.groupCount))
                    .setText(String.valueOf(topic.verses.size()));
            ((TextView) row.findViewById(R.id.groupIndicator))
                    .setText(isExpanded ? "▾" : "▸");
            return row;
        }

        /** Keeps each topic's tile image stable regardless of the Favorites group. */
        private int imageFor(Topic topic) {
            int canonical = repository.topics().indexOf(topic);
            if (canonical < 0) {
                return R.drawable.topic_favorites; // the synthetic Favorites group
            }
            return TOPIC_IMAGES[canonical % TOPIC_IMAGES.length];
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View row = convertView != null
                    ? convertView
                    : inflater.inflate(R.layout.list_child, parent, false);

            Verse verse = getChild(groupPosition, childPosition);
            ((TextView) row.findViewById(R.id.childText))
                    .setText(getString(R.string.quoted_verse_format, verse.text));
            ((TextView) row.findViewById(R.id.childReference))
                    .setText(getString(R.string.verse_reference_format, verse.reference));
            return row;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
