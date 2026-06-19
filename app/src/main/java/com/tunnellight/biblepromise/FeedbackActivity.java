package com.tunnellight.biblepromise;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/**
 * Lets the user send feedback, feature requests, or new verse suggestions
 * straight to the app's email address via their email app.
 */
public class FeedbackActivity extends AppCompatActivity {

    private EditText feedbackInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackInput = findViewById(R.id.feedbackInput);
        MaterialButton sendButton = findViewById(R.id.sendFeedbackButton);

        sendButton.setOnClickListener(v -> sendFeedback());
    }

    /** Opens the user's email app pre-filled with their feedback. */
    private void sendFeedback() {
        String message = feedbackInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, R.string.feedback_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Encode the subject and body into the mailto URI itself: some email
        // apps (e.g. Gmail) ignore EXTRA_SUBJECT/EXTRA_TEXT for ACTION_SENDTO.
        String mailto = "mailto:" + getString(R.string.feedback_email)
                + "?subject=" + Uri.encode(getString(R.string.feedback_email_subject))
                + "&body=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(mailto));
        // Keep the extras too, for apps that prefer them.
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.feedback_no_email_app, Toast.LENGTH_LONG).show();
        }
    }
}
