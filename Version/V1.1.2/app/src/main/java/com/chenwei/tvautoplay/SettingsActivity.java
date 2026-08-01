package com.chenwei.tvautoplay;

import android.app.AlertDialog;
import android.content.ClipData;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** D-pad-friendly settings screen and the only place that changes the selected document. */
public final class SettingsActivity extends ComponentActivity {
    public static final String EXTRA_CHOOSE_IMMEDIATELY = "choose_immediately";
    private static final String STATE_PICKER_STARTED = "picker_started";
    private static final String STATE_APPEND_SELECTION = "append_selection";

    private PlaybackConfigStore configStore;
    private TextView selectedVideo;
    private CheckBox autoStart;
    private Button soundButton;
    private Button scaleButton;
    private boolean pickerStarted;
    private boolean appendSelection;
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
        if (savedInstanceState != null) {
            pickerStarted = savedInstanceState.getBoolean(STATE_PICKER_STARTED, false);
            appendSelection = savedInstanceState.getBoolean(STATE_APPEND_SELECTION, false);
        }

        selectedVideo = findViewById(R.id.selected_video_value);
        autoStart = findViewById(R.id.auto_start_checkbox);
        soundButton = findViewById(R.id.sound_button);
        scaleButton = findViewById(R.id.scale_button);

        findViewById(R.id.select_video_button).setOnClickListener(
                view -> launchVideoPicker(false)
        );
        findViewById(R.id.append_video_button).setOnClickListener(
                view -> launchVideoPicker(true)
        );
        findViewById(R.id.clear_video_button).setOnClickListener(view -> confirmClearVideo());
        findViewById(R.id.back_to_player_button).setOnClickListener(view -> finish());
        autoStart.setOnClickListener(view ->
                configStore.setAutoStartEnabled(autoStart.isChecked()));
        soundButton.setOnClickListener(view -> toggleSound());
        scaleButton.setOnClickListener(view -> toggleScaleMode());

        refreshValues();
        boolean chooseImmediately = getIntent().getBooleanExtra(EXTRA_CHOOSE_IMMEDIATELY, false);
        if (chooseImmediately && savedInstanceState == null) {
            selectedVideo.post(() -> launchVideoPicker(false));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_PICKER_STARTED, pickerStarted);
        outState.putBoolean(STATE_APPEND_SELECTION, appendSelection);
        super.onSaveInstanceState(outState);
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

        List<Uri> selectedUris = selectedUris(data);
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, R.string.invalid_video_selection, Toast.LENGTH_LONG).show();
            return;
        }

        if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            Toast.makeText(this, R.string.persist_permission_failed, Toast.LENGTH_LONG).show();
            return;
        }
        List<PlaylistItem> selectedItems = new ArrayList<>();
        int failedCount = 0;
        for (Uri uri : selectedUris) {
            try {
                getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                if (UriAccess.canRead(this, uri.toString())) {
                    selectedItems.add(new PlaylistItem(
                            uri.toString(),
                            UriAccess.displayName(this, uri)
                    ));
                } else {
                    failedCount++;
                    releaseGrant(uri.toString());
                }
            } catch (SecurityException exception) {
                failedCount++;
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, R.string.persist_permission_failed, Toast.LENGTH_LONG).show();
            return;
        }

        PlaybackConfiguration current = configStore.load();
        List<PlaylistItem> updated = appendSelection
                ? PlaylistEditor.append(current.playlist(), selectedItems)
                : PlaylistEditor.replace(selectedItems);
        if (!appendSelection) {
            releaseRemovedGrants(current.playlist(), updated);
        }
        configStore.savePlaylist(updated);
        if (failedCount > 0) {
            Toast.makeText(
                    this,
                    getString(R.string.playlist_selection_partial, selectedItems.size(), failedCount),
                    Toast.LENGTH_LONG
            ).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    private void launchVideoPicker(boolean append) {
        if (pickerStarted) {
            return;
        }
        pickerStarted = true;
        appendSelection = append;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("video/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
                .setTitle(R.string.clear_playlist_title)
                .setMessage(R.string.clear_playlist_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.clear, (dialog, which) -> clearVideo())
                .show();
    }

    private void clearVideo() {
        List<PlaylistItem> previousItems = configStore.load().playlist();
        configStore.clearPlaylist();
        for (PlaylistItem item : previousItems) {
            releaseGrant(item.uri());
        }
        refreshValues();
    }

    private void releaseRemovedGrants(
            List<PlaylistItem> previousItems,
            List<PlaylistItem> updatedItems
    ) {
        Set<String> retainedUris = new LinkedHashSet<>();
        for (PlaylistItem item : updatedItems) {
            retainedUris.add(item.uri());
        }
        for (PlaylistItem item : previousItems) {
            if (!retainedUris.contains(item.uri())) {
                releaseGrant(item.uri());
            }
        }
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
                ? formatPlaylist(configuration.playlist())
                : getString(R.string.not_selected));
        autoStart.setChecked(configuration.autoStartEnabled());
        soundButton.setText(configuration.soundEnabled()
                ? R.string.sound_on
                : R.string.sound_off);
        scaleButton.setText(configuration.scaleMode() == ScaleMode.FIT
                ? R.string.scale_fit
                : R.string.scale_zoom);
    }

    private String formatPlaylist(List<PlaylistItem> playlist) {
        StringBuilder value = new StringBuilder(
                getString(R.string.selected_video_count, playlist.size())
        );
        for (int index = 0; index < playlist.size(); index++) {
            value.append('\n')
                    .append(index + 1)
                    .append(". ")
                    .append(playlist.get(index).displayName());
        }
        return value.toString();
    }

    private List<Uri> selectedUris(Intent data) {
        Set<String> uniqueValues = new LinkedHashSet<>();
        List<Uri> uris = new ArrayList<>();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int index = 0; index < clipData.getItemCount(); index++) {
                addUri(uris, uniqueValues, clipData.getItemAt(index).getUri());
            }
        } else {
            addUri(uris, uniqueValues, data.getData());
        }
        return uris;
    }

    private void addUri(List<Uri> uris, Set<String> uniqueValues, Uri uri) {
        if (uri != null && uniqueValues.add(uri.toString())) {
            uris.add(uri);
        }
    }
}
