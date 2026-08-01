package com.chenwei.tvautoplay;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;

/** D-pad-friendly settings screen and the only place that changes the selected document. */
public final class SettingsActivity extends ComponentActivity {
    public static final String EXTRA_CHOOSE_IMMEDIATELY = "choose_immediately";

    private PlaybackConfigStore configStore;
    private TextView selectedVideo;
    private CheckBox autoStart;
    private Button soundButton;
    private Button scaleButton;
    private boolean pickerStarted;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleVideoPickerResult(result.getResultCode(), result.getData())
        );
        setContentView(R.layout.activity_settings);
        configStore = new PlaybackConfigStore(this);

        selectedVideo = findViewById(R.id.selected_video_value);
        autoStart = findViewById(R.id.auto_start_checkbox);
        soundButton = findViewById(R.id.sound_button);
        scaleButton = findViewById(R.id.scale_button);

        findViewById(R.id.select_video_button).setOnClickListener(view -> launchVideoPicker());
        findViewById(R.id.clear_video_button).setOnClickListener(view -> confirmClearVideo());
        findViewById(R.id.back_to_player_button).setOnClickListener(view -> finish());
        autoStart.setOnClickListener(view ->
                configStore.setAutoStartEnabled(autoStart.isChecked()));
        soundButton.setOnClickListener(view -> toggleSound());
        scaleButton.setOnClickListener(view -> toggleScaleMode());

        refreshValues();
        boolean chooseImmediately = getIntent().getBooleanExtra(EXTRA_CHOOSE_IMMEDIATELY, false);
        if (chooseImmediately && savedInstanceState == null) {
            selectedVideo.post(this::launchVideoPicker);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshValues();
    }

    private void handleVideoPickerResult(int resultCode, Intent data) {
        pickerStarted = false;
        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri uri = data.getData();
        if (uri == null) {
            Toast.makeText(this, R.string.invalid_video_selection, Toast.LENGTH_LONG).show();
            return;
        }

        if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            Toast.makeText(this, R.string.persist_permission_failed, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException exception) {
            Toast.makeText(this, R.string.persist_permission_failed, Toast.LENGTH_LONG).show();
            return;
        }

        releasePreviousGrant(configStore.load().videoUri(), uri);
        configStore.saveVideo(uri, UriAccess.displayName(this, uri));
        setResult(RESULT_OK);
        finish();
    }

    private void launchVideoPicker() {
        if (pickerStarted) {
            return;
        }
        pickerStarted = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("video/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "video/*",
                "application/x-matroska",
                "application/vnd.apple.mpegurl",
                "application/x-mpegURL"
        });
        try {
            videoPickerLauncher.launch(intent);
        } catch (RuntimeException exception) {
            pickerStarted = false;
            Toast.makeText(this, R.string.file_picker_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void toggleSound() {
        PlaybackConfiguration current = configStore.load();
        configStore.setSoundEnabled(!current.soundEnabled());
        refreshValues();
    }

    private void toggleScaleMode() {
        PlaybackConfiguration current = configStore.load();
        configStore.setScaleMode(current.scaleMode() == ScaleMode.FIT
                ? ScaleMode.ZOOM
                : ScaleMode.FIT);
        refreshValues();
    }

    private void confirmClearVideo() {
        if (!configStore.load().hasVideo()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_video_title)
                .setMessage(R.string.clear_video_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.clear, (dialog, which) -> clearVideo())
                .show();
    }

    private void clearVideo() {
        String previousUri = configStore.load().videoUri();
        configStore.clearVideo();
        releaseGrant(previousUri);
        refreshValues();
    }

    private void releasePreviousGrant(String previousValue, Uri selectedUri) {
        if (previousValue == null || previousValue.equals(selectedUri.toString())) {
            return;
        }
        releaseGrant(previousValue);
    }

    private void releaseGrant(String uriValue) {
        if (uriValue == null) {
            return;
        }
        Uri uri = Uri.parse(uriValue);
        List<UriPermission> grants = getContentResolver().getPersistedUriPermissions();
        for (UriPermission grant : grants) {
            if (grant.getUri().equals(uri) && grant.isReadPermission()) {
                try {
                    getContentResolver().releasePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                    // The provider may have revoked the grant already; clearing settings is enough.
                }
                return;
            }
        }
    }

    private void refreshValues() {
        PlaybackConfiguration configuration = configStore.load();
        selectedVideo.setText(configuration.hasVideo()
                ? configuration.displayName()
                : getString(R.string.not_selected));
        autoStart.setChecked(configuration.autoStartEnabled());
        soundButton.setText(configuration.soundEnabled()
                ? R.string.sound_on
                : R.string.sound_off);
        scaleButton.setText(configuration.scaleMode() == ScaleMode.FIT
                ? R.string.scale_fit
                : R.string.scale_zoom);
    }
}
