package com.dailypromise.myapplication;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

/** Lets the user enable a daily verse notification and choose its time. */
public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial notificationSwitch;
    private TextView timeValue;

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

        updateTimeLabel();

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
    }

    private void showTimePicker() {
        int hour = NotificationScheduler.getHour(this);
        int minute = NotificationScheduler.getMinute(this);
        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            NotificationScheduler.setTime(this, selectedHour, selectedMinute);
            updateTimeLabel();
            if (NotificationScheduler.isEnabled(this)) {
                NotificationScheduler.schedule(this); // re-arm at the new time
            }
        }, hour, minute, DateFormat.is24HourFormat(this)).show();
    }

    private void updateTimeLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, NotificationScheduler.getHour(this));
        calendar.set(Calendar.MINUTE, NotificationScheduler.getMinute(this));
        timeValue.setText(DateFormat.getTimeFormat(this).format(calendar.getTime()));
    }
}
