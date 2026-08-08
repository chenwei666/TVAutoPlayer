package com.chenwei.tvautoplay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

/** Full-screen TV playback surface for mixed image/video playlists. */
@OptIn(markerClass = UnstableApi.class)
public final class MainActivity extends ComponentActivity {
    private PlayerView playerView;
    private View emptyPanel;
    private View errorPanel;
    private TextView emptyMessage;
    private TextView errorMessage;
    private PlaybackConfigStore configStore;
    private ExoPlayer player;
    private boolean settingsOpen;
    private boolean selectionPromptAttempted;
    private boolean unreadableNoticeShown;
    private int consecutivePlaybackFailures;
    private int activePlaylistSize;
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    settingsOpen = false;
                    startConfiguredPlayback();
                }
        );
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                openSettings(false);
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        enterImmersiveMode();

        configStore = new PlaybackConfigStore(this);
        playerView = findViewById(R.id.player_view);
        emptyPanel = findViewById(R.id.empty_panel);
        errorPanel = findViewById(R.id.error_panel);
        emptyMessage = findViewById(R.id.empty_message);
        errorMessage = findViewById(R.id.error_message);

        Button chooseMedia = findViewById(R.id.choose_video_button);
        Button openSettings = findViewById(R.id.open_settings_button);
        Button retry = findViewById(R.id.retry_button);
        Button errorSettings = findViewById(R.id.error_settings_button);

        chooseMedia.setOnClickListener(view -> openSettings(true));
        openSettings.setOnClickListener(view -> openSettings(false));
        errorSettings.setOnClickListener(view -> openSettings(false));
        retry.setOnClickListener(view -> retryPlayback());
    }

    @Override
    protected void onStart() {
        super.onStart();
        startConfiguredPlayback();
    }

    @Override
    protected void onStop() {
        releasePlayer();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            openSettings(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void startConfiguredPlayback() {
        PlaybackConfiguration configuration = configStore.load();
        List<PlaylistItem> readableItems = UriAccess.readableItems(
                this,
                configuration.playlist()
        );
        PlaybackDecision decision = PlaybackPolicy.decide(
                configuration,
                readableItems.size()
        );
        if (decision != PlaybackDecision.READY) {
            releasePlayer();
            showSelectionRequired(decision);
            if (!settingsOpen && !selectionPromptAttempted) {
                openSettings(true);
            }
            return;
        }

        if (!unreadableNoticeShown && readableItems.size() < configuration.mediaCount()) {
            unreadableNoticeShown = true;
            Toast.makeText(
                    this,
                    getResources().getQuantityString(
                            R.plurals.unreadable_media_skipped,
                            configuration.mediaCount() - readableItems.size(),
                            configuration.mediaCount() - readableItems.size()
                    ),
                    Toast.LENGTH_LONG
            ).show();
        }

        showPlayer();
        releasePlayer();
        consecutivePlaybackFailures = 0;
        activePlaylistSize = readableItems.size();
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(C.USAGE_MEDIA)
                                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                                .build(),
                        true
                )
                .build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setVolume(configuration.soundEnabled() ? 1f : 0f);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                handlePlaybackError(error);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    consecutivePlaybackFailures = 0;
                    errorPanel.setVisibility(View.GONE);
                }
            }
        });

        playerView.setResizeMode(configuration.scaleMode() == ScaleMode.ZOOM
                ? AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                : AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setPlayer(player);
        List<MediaItem> mediaItems = new ArrayList<>(readableItems.size());
        for (PlaylistItem item : readableItems) {
            mediaItems.add(PlaybackMediaItemFactory.create(
                    item,
                    configuration.imageDuration()
            ));
        }
        player.setMediaItems(mediaItems);
        player.prepare();
        player.play();

        if (!configStore.isSettingsHintShown()) {
            Toast.makeText(this, R.string.settings_key_hint, Toast.LENGTH_LONG).show();
            configStore.markSettingsHintShown();
        }
    }

    private void retryPlayback() {
        if (player == null) {
            startConfiguredPlayback();
            return;
        }
        consecutivePlaybackFailures = 0;
        errorPanel.setVisibility(View.GONE);
        player.prepare();
        player.play();
    }

    private void showPlayer() {
        playerView.setVisibility(View.VISIBLE);
        emptyPanel.setVisibility(View.GONE);
        errorPanel.setVisibility(View.GONE);
    }

    private void showSelectionRequired(PlaybackDecision decision) {
        playerView.setVisibility(View.GONE);
        errorPanel.setVisibility(View.GONE);
        emptyPanel.setVisibility(View.VISIBLE);
        emptyMessage.setText(decision == PlaybackDecision.PERMISSION_REQUIRED
                ? R.string.playlist_permission_lost
                : R.string.no_playlist_selected);
    }

    private void showPlaybackError(PlaybackException error) {
        errorMessage.setText(getString(R.string.playback_error_detail, error.getErrorCodeName()));
        errorPanel.setVisibility(View.VISIBLE);
        findViewById(R.id.retry_button).requestFocus();
    }

    private void handlePlaybackError(PlaybackException error) {
        consecutivePlaybackFailures++;
        if (player != null
                && activePlaylistSize > 1
                && consecutivePlaybackFailures < activePlaylistSize) {
            int currentIndex = Math.max(player.getCurrentMediaItemIndex(), 0);
            int nextIndex = (currentIndex + 1) % activePlaylistSize;
            player.seekTo(nextIndex, 0L);
            player.prepare();
            player.play();
            return;
        }
        showPlaybackError(error);
    }

    private void openSettings(boolean chooseImmediately) {
        if (settingsOpen) {
            return;
        }
        if (chooseImmediately) {
            selectionPromptAttempted = true;
        }
        settingsOpen = true;
        Intent intent = new Intent(this, SettingsActivity.class)
                .putExtra(SettingsActivity.EXTRA_CHOOSE_IMMEDIATELY, chooseImmediately);
        settingsLauncher.launch(intent);
    }

    private void releasePlayer() {
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
        activePlaylistSize = 0;
        consecutivePlaybackFailures = 0;
    }

    @SuppressWarnings("deprecation")
    private void enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
            return;
        }
        enterLegacyImmersiveMode();
    }

    @SuppressWarnings("deprecation")
    private void enterLegacyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}
