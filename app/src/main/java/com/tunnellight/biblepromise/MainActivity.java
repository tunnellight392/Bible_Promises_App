package com.tunnellight.biblepromise;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Promises — shows a daily uplifting WEB verse over a nature background.
 * The verse is stable for the whole day; swipe left/right to move between
 * verses (changing the photo too), "Next Verse" jumps to a random verse and
 * photo, "Browse" opens the topic list, and "Share" sends the current verse
 * to other apps as an image over its background photo.
 */
public class MainActivity extends AppCompatActivity {

    /** Minimum horizontal travel / speed for a swipe to count (in px). */
    private static final float SWIPE_MIN_DISTANCE = 100f;
    private static final float SWIPE_MIN_VELOCITY = 120f;

    /** Nature photo backgrounds cycled through on swipe. */
    private static final int[] BACKGROUNDS = {
            R.drawable.nature_1, R.drawable.nature_2, R.drawable.nature_3,
            R.drawable.nature_4, R.drawable.nature_5, R.drawable.nature_6
    };

    /** Base text sizes (sp) at the "Medium" (1.0x) accessibility scale. */
    private static final float VERSE_BASE_SP = 26f;
    private static final float REFERENCE_BASE_SP = 16f;
    private static final float QUOTE_BASE_SP = 72f;

    private final VerseRepository repository = new VerseRepository();
    private final Random random = new Random();
    private final ExecutorService shareExecutor = Executors.newSingleThreadExecutor();

    private TextView verseText;
    private TextView verseReference;
    private TextView decorativeQuote;
    private TextView attributionText;
    private View verseBlock;
    private ImageView backgroundImage;
    private ImageButton favoriteButton;
    private int currentIndex;
    private int bgIndex;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verseText = findViewById(R.id.verseText);
        verseReference = findViewById(R.id.verseReference);
        decorativeQuote = findViewById(R.id.decorativeQuote);
        attributionText = findViewById(R.id.attributionText);
        verseBlock = findViewById(R.id.verseBlock);
        backgroundImage = findViewById(R.id.backgroundImage);
        MaterialButton anotherButton = findViewById(R.id.anotherButton);
        MaterialButton browseButton = findViewById(R.id.browseButton);
        ImageButton shareButton = findViewById(R.id.shareButton);
        ImageButton menuButton = findViewById(R.id.menuButton);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Make sure the notification channel exists for the Settings screen.
        NotificationScheduler.createChannel(this);

        currentIndex = repository.indexForDate(LocalDate.now());
        bindVerse(repository.get(currentIndex), false);

        // Start on a photo that's stable for the day.
        bgIndex = (int) (LocalDate.now().toEpochDay() % BACKGROUNDS.length);
        backgroundImage.setImageResource(BACKGROUNDS[bgIndex]);

        anotherButton.setOnClickListener(v -> {
            currentIndex = repository.randomIndexExcluding(currentIndex);
            bindVerse(repository.get(currentIndex), true);
            bgIndex = randomBackgroundExcluding(bgIndex);
            crossfadeBackground(BACKGROUNDS[bgIndex]);
        });

        browseButton.setOnClickListener(v ->
                browseLauncher.launch(new Intent(this, BrowseActivity.class)));

        shareButton.setOnClickListener(v -> shareVerse(repository.get(currentIndex), v));

        favoriteButton.setOnClickListener(v -> {
            FavoritesStore.toggle(this, repository.get(currentIndex));
            updateFavoriteIcon();
        });

        menuButton.setOnClickListener(this::showOverflowMenu);

        // Swipe left/right anywhere on the screen to move between verses.
        GestureDetector gestureDetector = new GestureDetector(this, new SwipeListener());
        findViewById(R.id.rootView).setOnTouchListener(
                (v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shareExecutor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-apply font preferences in case they changed in the Accessibility screen.
        applyTextPreferences();
        // Reflect the current Bible version (it may have changed in Settings).
        attributionText.setText(BibleVersionPrefs.attributionRes(BibleVersionPrefs.get(this)));
        bindVerse(repository.get(currentIndex), false);
    }

    /** Applies the saved Accessibility font size and family to the verse text. */
    private void applyTextPreferences() {
        float scale = AccessibilityPrefs.getSizeScale(this);
        int fontType = AccessibilityPrefs.getFontType(this);
        // The verse body uses a version-aware scale so Malayalam (taller script) fits.
        float verseScale = AccessibilityPrefs.getVerseSizeScale(this, BibleVersionPrefs.get(this));

        verseText.setTypeface(AccessibilityPrefs.typeface(fontType, Typeface.ITALIC));
        verseText.setTextSize(TypedValue.COMPLEX_UNIT_SP, VERSE_BASE_SP * verseScale);

        verseReference.setTypeface(AccessibilityPrefs.typeface(fontType, Typeface.BOLD));
        verseReference.setTextSize(TypedValue.COMPLEX_UNIT_SP, REFERENCE_BASE_SP * scale);

        decorativeQuote.setTypeface(AccessibilityPrefs.typeface(fontType, Typeface.NORMAL));
        decorativeQuote.setTextSize(TypedValue.COMPLEX_UNIT_SP, QUOTE_BASE_SP * scale);
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

    /** Steps to the next (+1) or previous (-1) verse, wrapping around. */
    private void navigate(int direction) {
        int size = repository.size();
        currentIndex = ((currentIndex + direction) % size + size) % size;
        bindVerse(repository.get(currentIndex), false);
        slideIn(verseBlock, direction);

        // Move to a different background photo, in the swipe direction.
        bgIndex = ((bgIndex + direction) % BACKGROUNDS.length + BACKGROUNDS.length)
                % BACKGROUNDS.length;
        crossfadeBackground(BACKGROUNDS[bgIndex]);
    }

    /** A random background index different from {@code current} (when possible). */
    private int randomBackgroundExcluding(int current) {
        if (BACKGROUNDS.length <= 1) {
            return 0;
        }
        int next;
        do {
            next = random.nextInt(BACKGROUNDS.length);
        } while (next == current);
        return next;
    }

    /** Smoothly crossfades the background photo to {@code resId}. */
    private void crossfadeBackground(int resId) {
        Drawable current = backgroundImage.getDrawable();
        Drawable next = ContextCompat.getDrawable(this, resId);
        if (current == null || next == null) {
            backgroundImage.setImageResource(resId);
            return;
        }
        TransitionDrawable transition = new TransitionDrawable(new Drawable[]{current, next});
        transition.setCrossFadeEnabled(true);
        backgroundImage.setImageDrawable(transition);
        transition.startTransition(350);
    }

    private void bindVerse(Verse verse, boolean animate) {
        int version = BibleVersionPrefs.get(this);
        String tag = getString(BibleVersionPrefs.tagRes(version));
        verseText.setText(verse.text(version));
        verseReference.setText(getString(R.string.verse_reference_format,
                BibleBooks.localizedReference(verse.reference, version), tag));
        updateFavoriteIcon();

        if (animate) {
            crossFadeIn(verseText);
            crossFadeIn(verseReference);
        }
    }

    /** Reflects whether the current verse is favorited in the heart button. */
    private void updateFavoriteIcon() {
        boolean isFavorite = FavoritesStore.isFavorite(this, repository.get(currentIndex));
        favoriteButton.setImageResource(
                isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        favoriteButton.setContentDescription(
                getString(isFavorite ? R.string.remove_favorite : R.string.add_favorite));
    }

    private void crossFadeIn(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(450).start();
    }

    /** Slides a view in from the side the user swiped toward, fading in. */
    private void slideIn(View view, int direction) {
        float width = view.getWidth() > 0
                ? view.getWidth()
                : getResources().getDisplayMetrics().widthPixels;
        view.setTranslationX(direction > 0 ? width : -width);
        view.setAlpha(0f);
        view.animate().translationX(0f).alpha(1f).setDuration(280).start();
    }

    /** Detects horizontal flings and turns them into verse navigation. */
    private final class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true; // claim the gesture so onFling is delivered
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            if (e1 == null) {
                return false;
            }
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (Math.abs(dx) > Math.abs(dy)
                    && Math.abs(dx) > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {
                navigate(dx < 0 ? 1 : -1); // swipe left = next, swipe right = previous
                return true;
            }
            return false;
        }
    }

    /**
     * Shares the verse as an image of the card (photo + verse, as on screen),
     * with the verse text alongside for apps that use a caption.
     */
    private void shareVerse(Verse verse, View shareButton) {
        int version = BibleVersionPrefs.get(this);
        String tag = getString(BibleVersionPrefs.tagRes(version));
        String text = verse.forSharing(version, tag) + "\n\nvia " + getString(R.string.app_name);

        Bitmap card = ShareCardRenderer.render(this, BACKGROUNDS[bgIndex],
                verseText, verseReference, decorativeQuote, attributionText);
        shareButton.setEnabled(false);
        shareExecutor.execute(() -> {
            Uri uri = saveShareImage(card);
            card.recycle();
            runOnUiThread(() -> {
                shareButton.setEnabled(true);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.verse_of_the_day));
                intent.putExtra(Intent.EXTRA_TEXT, text);
                if (uri != null) {
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setClipData(ClipData.newRawUri(null, uri)); // chooser preview
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setType("text/plain"); // fall back to text if the image failed
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_verse_via)));
            });
        });
    }

    /** Writes the card to the shared-images cache folder; null on failure. */
    @Nullable
    private Uri saveShareImage(Bitmap card) {
        File dir = new File(getCacheDir(), "shared_images");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            return null;
        }
        File file = new File(dir, "promise.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            card.compress(Bitmap.CompressFormat.JPEG, 92, out);
        } catch (IOException e) {
            return null;
        }
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }
}
