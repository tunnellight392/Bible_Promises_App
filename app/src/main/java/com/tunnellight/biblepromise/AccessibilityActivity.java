package com.tunnellight.biblepromise;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Lets the user adjust the verse reading text: its size and font family. Changes
 * are saved immediately via {@link AccessibilityPrefs} and shown in a live
 * preview; {@link MainActivity} re-applies them when it resumes.
 */
public class AccessibilityActivity extends AppCompatActivity {

    /** Base size (sp) of the preview text at the "Medium" (1.0x) scale. */
    private static final float PREVIEW_BASE_SP = 20f;

    private TextView previewText;
    private TextView sizeValue;
    private String[] sizeLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);

        previewText = findViewById(R.id.previewText);
        sizeValue = findViewById(R.id.sizeValue);
        SeekBar sizeSeekBar = findViewById(R.id.sizeSeekBar);
        RadioGroup fontStyleGroup = findViewById(R.id.fontStyleGroup);

        sizeLabels = getResources().getStringArray(R.array.font_size_labels);

        // Reflect the saved preferences in the controls.
        sizeSeekBar.setProgress(AccessibilityPrefs.getSizeIndex(this));
        checkFontRadio(fontStyleGroup, AccessibilityPrefs.getFontType(this));
        updatePreview();

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AccessibilityPrefs.setSizeIndex(AccessibilityActivity.this, progress);
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        fontStyleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            AccessibilityPrefs.setFontType(this, fontTypeFor(checkedId));
            updatePreview();
        });
    }

    /** Applies the current size + font preference to the preview text. */
    private void updatePreview() {
        int sizeIndex = AccessibilityPrefs.getSizeIndex(this);
        float scale = AccessibilityPrefs.SIZE_SCALES[sizeIndex];

        Typeface typeface = AccessibilityPrefs.typeface(
                AccessibilityPrefs.getFontType(this), Typeface.ITALIC);
        previewText.setTypeface(typeface);
        previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, PREVIEW_BASE_SP * scale);

        sizeValue.setText(sizeLabels[sizeIndex]);
    }

    private void checkFontRadio(RadioGroup group, int fontType) {
        switch (fontType) {
            case AccessibilityPrefs.FONT_SANS_SERIF:
                group.check(R.id.fontSansSerif);
                break;
            case AccessibilityPrefs.FONT_MONOSPACE:
                group.check(R.id.fontMonospace);
                break;
            case AccessibilityPrefs.FONT_SERIF:
            default:
                group.check(R.id.fontSerif);
                break;
        }
    }

    private int fontTypeFor(int checkedId) {
        if (checkedId == R.id.fontSansSerif) {
            return AccessibilityPrefs.FONT_SANS_SERIF;
        }
        if (checkedId == R.id.fontMonospace) {
            return AccessibilityPrefs.FONT_MONOSPACE;
        }
        return AccessibilityPrefs.FONT_SERIF;
    }
}
