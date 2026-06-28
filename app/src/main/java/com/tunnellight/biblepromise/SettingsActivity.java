package com.tunnellight.biblepromise;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

/** Lets the user enable a daily verse notification and choose its time. */
public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial notificationSwitch;
    private TextView timeValue;
    private TextView themeValue;

    private final ActivityResultLauncher<String> requestNotificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    NotificationScheduler.enable(this);
                } else {
                    notificationSwitch.setChecked(false);
                    Toast.makeText(this, R.string.notifications_blocked, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationSwitch = findViewById(R.id.notificationSwitch);
        timeValue = findViewById(R.id.timeValue);
        themeValue = findViewById(R.id.themeValue);

        updateTimeLabel();
        updateThemeLabel();

        notificationSwitch.setChecked(NotificationScheduler.isEnabled(this));
        notificationSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            if (!isChecked) {
                NotificationScheduler.disable(this);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                NotificationScheduler.enable(this);
            }
        });

        findViewById(R.id.timeRow).setOnClickListener(v -> showTimePicker());

        findViewById(R.id.themeRow).setOnClickListener(v -> showThemeChooser());

        findViewById(R.id.accessibilityRow).setOnClickListener(v ->
                startActivity(new Intent(this, AccessibilityActivity.class)));
    }

    /** Lets the user pick System / Light / Dark; applying recreates the activity. */
    private void showThemeChooser() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.theme_choose)
                .setSingleChoiceItems(
                        R.array.theme_options,
                        ThemePrefs.getSelectedIndex(this),
                        (dialog, which) -> {
                            ThemePrefs.setMode(this, ThemePrefs.MODES[which]);
                            updateThemeLabel();
                            dialog.dismiss();
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateThemeLabel() {
        String[] options = getResources().getStringArray(R.array.theme_options);
        themeValue.setText(options[ThemePrefs.getSelectedIndex(this)]);
    }

    private void showTimePicker() {
        int clockFormat = DateFormat.is24HourFormat(this)
                ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(NotificationScheduler.getHour(this))
                .setMinute(NotificationScheduler.getMinute(this))
                .setTitleText(R.string.notification_time)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            NotificationScheduler.setTime(this, picker.getHour(), picker.getMinute());
            updateTimeLabel();
            if (NotificationScheduler.isEnabled(this)) {
                NotificationScheduler.schedule(this); // re-arm at the new time
            }
        });
        picker.show(getSupportFragmentManager(), "verse_time_picker");
    }

    private void updateTimeLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, NotificationScheduler.getHour(this));
        calendar.set(Calendar.MINUTE, NotificationScheduler.getMinute(this));
        timeValue.setText(DateFormat.getTimeFormat(this).format(calendar.getTime()));
    }
}
