package com.chenwei.tvautoplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Best-effort TV startup entry point; OEM background-launch policies still apply. */
public final class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String ACTION_QUICK_BOOT = "android.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !ACTION_QUICK_BOOT.equals(action)) {
            return;
        }

        PlaybackConfiguration configuration = new PlaybackConfigStore(context).load();
        boolean canRead = UriAccess.canRead(context, configuration.videoUri());
        if (!PlaybackPolicy.shouldLaunchOnBoot(configuration, canRead)) {
            Log.i(TAG, "Boot launch skipped because playback is not ready or auto-start is off");
            return;
        }

        Intent launchIntent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(launchIntent);
        } catch (RuntimeException exception) {
            Log.e(TAG, "TV firmware blocked boot playback launch", exception);
        }
    }
}
