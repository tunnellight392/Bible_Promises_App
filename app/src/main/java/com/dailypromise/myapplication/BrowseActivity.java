package com.dailypromise.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Lets the user browse every verse, grouped by topic, and pick one to view. */
public class BrowseActivity extends AppCompatActivity {

    /** Extra carrying the chosen verse's global index back to MainActivity. */
    static final String EXTRA_VERSE_INDEX = "com.dailypromise.myapplication.VERSE_INDEX";

    private final VerseRepository repository = new VerseRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        ExpandableListView list = findViewById(R.id.topicList);
        list.setAdapter(new TopicAdapter(repository.topics()));

        list.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_VERSE_INDEX, repository.globalIndex(groupPosition, childPosition));
            setResult(RESULT_OK, result);
            finish();
            return true;
        });
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
            ((TextView) row.findViewById(R.id.groupName)).setText(topic.name);
            ((TextView) row.findViewById(R.id.groupCount))
                    .setText(String.valueOf(topic.verses.size()));
            ((TextView) row.findViewById(R.id.groupIndicator))
                    .setText(isExpanded ? "▾" : "▸");
            return row;
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
