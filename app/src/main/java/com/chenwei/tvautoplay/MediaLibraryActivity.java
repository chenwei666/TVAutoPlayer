package com.chenwei.tvautoplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** D-pad-friendly built-in library for videos indexed on TV storage and attached USB volumes. */
public final class MediaLibraryActivity extends ComponentActivity {
    public static final String EXTRA_APPEND_SELECTION = "append_selection";
    private static final String STATE_PERMISSION_REQUESTED = "permission_requested";
    private static final String STATE_SELECTED_URIS = "selected_uris";
    private static final String STATE_SELECTION_INITIALIZED = "selection_initialized";

    private final ExecutorService scanExecutor = Executors.newSingleThreadExecutor();
    private final List<PlaylistItem> catalog = new ArrayList<>();
    private final List<String> selectedUrisInOrder = new ArrayList<>();

    private PlaybackConfigStore configStore;
    private MediaStoreVideoRepository repository;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private TextView statusText;
    private ListView videoList;
    private Button confirmButton;
    private Button permissionButton;
    private boolean appendSelection;
    private boolean permissionRequested;
    private boolean selectionInitialized;
    private boolean destroyed;
    private int scanSequence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> handlePermissionResult()
        );
        setContentView(R.layout.activity_media_library);

        configStore = new PlaybackConfigStore(this);
        repository = new MediaStoreVideoRepository();
        appendSelection = getIntent().getBooleanExtra(EXTRA_APPEND_SELECTION, false);
        permissionRequested = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_PERMISSION_REQUESTED, false);
        if (savedInstanceState != null) {
            List<String> restoredUris = savedInstanceState.getStringArrayList(STATE_SELECTED_URIS);
            if (restoredUris != null) {
                selectedUrisInOrder.addAll(restoredUris);
            }
            selectionInitialized = savedInstanceState.getBoolean(
                    STATE_SELECTION_INITIALIZED,
                    false
            );
        }

        statusText = findViewById(R.id.media_library_status);
        videoList = findViewById(R.id.media_video_list);
        confirmButton = findViewById(R.id.confirm_media_selection_button);
        permissionButton = findViewById(R.id.grant_media_permission_button);

        confirmButton.setOnClickListener(view -> saveSelection());
        videoList.setOnItemClickListener((parent, view, position, id) -> {
            String uri = catalog.get(position).uri();
            if (videoList.isItemChecked(position)) {
                if (!selectedUrisInOrder.contains(uri)) {
                    selectedUrisInOrder.add(uri);
                }
            } else {
                selectedUrisInOrder.remove(uri);
            }
            confirmButton.setEnabled(!selectedUrisInOrder.isEmpty());
        });
        findViewById(R.id.refresh_media_library_button).setOnClickListener(view -> ensureAccessAndScan());
        permissionButton.setOnClickListener(view -> requestMediaPermission());
        findViewById(R.id.cancel_media_library_button).setOnClickListener(view -> finish());

        if (MediaPermissionHelper.hasReadAccess(this)) {
            scanVideos();
        } else if (!permissionRequested) {
            requestMediaPermission();
        } else {
            showPermissionRequired();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_PERMISSION_REQUESTED, permissionRequested);
        outState.putStringArrayList(STATE_SELECTED_URIS, new ArrayList<>(selectedUrisInOrder));
        outState.putBoolean(STATE_SELECTION_INITIALIZED, selectionInitialized);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        scanSequence++;
        scanExecutor.shutdownNow();
        super.onDestroy();
    }

    private void ensureAccessAndScan() {
        if (MediaPermissionHelper.hasReadAccess(this)) {
            scanVideos();
        } else {
            requestMediaPermission();
        }
    }

    private void requestMediaPermission() {
        permissionRequested = true;
        permissionLauncher.launch(MediaPermissionHelper.requiredPermissions());
    }

    private void handlePermissionResult() {
        if (MediaPermissionHelper.hasReadAccess(this)) {
            scanVideos();
        } else {
            showPermissionRequired();
        }
    }

    private void showPermissionRequired() {
        catalog.clear();
        videoList.setAdapter(null);
        videoList.setVisibility(View.GONE);
        confirmButton.setEnabled(false);
        permissionButton.setVisibility(View.VISIBLE);
        statusText.setText(R.string.media_permission_required);
        permissionButton.requestFocus();
    }

    private void scanVideos() {
        permissionButton.setVisibility(View.GONE);
        confirmButton.setEnabled(false);
        videoList.setVisibility(View.GONE);
        statusText.setText(R.string.media_library_scanning);
        int requestedScan = ++scanSequence;
        scanExecutor.execute(() -> {
            List<PlaylistItem> scanned;
            try {
                scanned = repository.queryVideos(getApplicationContext());
            } catch (RuntimeException exception) {
                scanned = null;
            }
            List<PlaylistItem> result = scanned;
            runOnUiThread(() -> {
                if (requestedScan == scanSequence) {
                    showScanResult(result);
                }
            });
        });
    }

    private void showScanResult(List<PlaylistItem> result) {
        if (destroyed || isFinishing()) {
            return;
        }
        catalog.clear();
        if (result == null) {
            videoList.setAdapter(null);
            videoList.setVisibility(View.GONE);
            confirmButton.setEnabled(false);
            statusText.setText(R.string.media_library_scan_failed);
            findViewById(R.id.refresh_media_library_button).requestFocus();
            return;
        }
        catalog.addAll(result);
        if (catalog.isEmpty()) {
            videoList.setAdapter(null);
            videoList.setVisibility(View.GONE);
            confirmButton.setEnabled(false);
            statusText.setText(R.string.media_library_empty);
            findViewById(R.id.refresh_media_library_button).requestFocus();
            return;
        }

        List<String> labels = new ArrayList<>();
        for (PlaylistItem item : catalog) {
            labels.add(item.displayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_media_video,
                android.R.id.text1,
                labels
        );
        videoList.setAdapter(adapter);
        videoList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        videoList.setVisibility(View.VISIBLE);
        statusText.setText(getString(R.string.media_library_found, catalog.size()));
        restoreSelection();
        confirmButton.setEnabled(!selectedUrisInOrder.isEmpty());
        videoList.requestFocus();
    }

    private void restoreSelection() {
        Set<String> availableUris = new HashSet<>();
        for (PlaylistItem item : catalog) {
            availableUris.add(item.uri());
        }
        for (int index = selectedUrisInOrder.size() - 1; index >= 0; index--) {
            if (!availableUris.contains(selectedUrisInOrder.get(index))) {
                selectedUrisInOrder.remove(index);
            }
        }

        if (!selectionInitialized) {
            selectionInitialized = true;
            if (!appendSelection) {
                for (PlaylistItem item : configStore.load().playlist()) {
                    if (availableUris.contains(item.uri())) {
                        selectedUrisInOrder.add(item.uri());
                    }
                }
            }
        }

        Set<String> selectedUris = new HashSet<>(selectedUrisInOrder);
        for (int index = 0; index < catalog.size(); index++) {
            videoList.setItemChecked(index, selectedUris.contains(catalog.get(index).uri()));
        }
    }

    private void saveSelection() {
        List<PlaylistItem> selected = new ArrayList<>();
        for (String selectedUri : selectedUrisInOrder) {
            for (PlaylistItem item : catalog) {
                if (item.uri().equals(selectedUri)) {
                    selected.add(item);
                    break;
                }
            }
        }
        if (selected.isEmpty()) {
            Toast.makeText(this, R.string.media_library_select_at_least_one, Toast.LENGTH_LONG).show();
            return;
        }

        PlaybackConfiguration current = configStore.load();
        List<PlaylistItem> updated = appendSelection
                ? PlaylistEditor.append(current.playlist(), selected)
                : PlaylistEditor.replace(selected);
        if (!appendSelection) {
            UriAccess.releaseRemovedPersistedReadPermissions(this, current.playlist(), updated);
        }
        configStore.savePlaylist(updated);
        setResult(RESULT_OK);
        finish();
    }
}
