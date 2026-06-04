package com.dailypromise.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;

/**
 * Promises — shows a daily uplifting NIV verse over a nature background.
 * The verse is stable for the whole day; "Another verse" reveals more, and
 * "Share" passes the current verse to other apps.
 */
public class MainActivity extends AppCompatActivity {

    private final VerseRepository repository = new VerseRepository();

    private TextView verseText;
    private TextView verseReference;
    private int currentIndex;

    /** Receives the verse chosen on the Browse screen and displays it. */
    private final ActivityResultLauncher<Intent> browseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    int index = data.getIntExtra(BrowseActivity.EXTRA_VERSE_INDEX, currentIndex);
                    currentIndex = index;
                    bindVerse(repository.get(index), true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verseText = findViewById(R.id.verseText);
        verseReference = findViewById(R.id.verseReference);
        MaterialButton anotherButton = findViewById(R.id.anotherButton);
        MaterialButton browseButton = findViewById(R.id.browseButton);
        MaterialButton shareButton = findViewById(R.id.shareButton);

        currentIndex = repository.indexForDate(LocalDate.now());
        bindVerse(repository.get(currentIndex), false);

        anotherButton.setOnClickListener(v -> {
            currentIndex = repository.randomIndexExcluding(currentIndex);
            bindVerse(repository.get(currentIndex), true);
        });

        browseButton.setOnClickListener(v ->
                browseLauncher.launch(new Intent(this, BrowseActivity.class)));

        shareButton.setOnClickListener(v -> shareVerse(repository.get(currentIndex)));
    }

    private void bindVerse(Verse verse, boolean animate) {
        verseText.setText(verse.text);
        verseReference.setText("— " + verse.reference + " (WEB)");

        if (animate) {
            crossFadeIn(verseText);
            crossFadeIn(verseReference);
        }
    }

    private void crossFadeIn(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(450).start();
    }

    private void shareVerse(Verse verse) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.verse_of_the_day));
        intent.putExtra(Intent.EXTRA_TEXT,
                verse.forSharing() + "\n\nvia " + getString(R.string.app_name));
        startActivity(Intent.createChooser(intent, getString(R.string.share_verse_via)));
    }
}
