package com.tvpower.app;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

/**
 * Always-on helper (it is an accessibility service, so Android keeps it alive).
 *  1. Can lock the screen / open the power menu when asked.
 *  2. Watches for the screen turning on and then switches to the chosen HDMI input.
 */
public class PowerService extends AccessibilityService {

    static volatile PowerService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered = false;

    private final BroadcastReceiver screenOn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (!Prefs.wakeSwitch(ctx)) return;
            // Give the TV a moment to finish waking up first.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputSwitcher.switchSaved(PowerService.this);
                }
            }, 2000);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        if (!registered) {
            registerReceiver(screenOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
            registered = true;
        }
        // If the TV has just (re)started, also switch once it is up.
        if (SystemClock.elapsedRealtime() < 150000 && Prefs.wakeSwitch(this)) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputSwitcher.switchSaved(PowerService.this);
                }
            }, 8000);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        cleanup();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        instance = null;
        if (registered) {
            try {
                unregisterReceiver(screenOn);
            } catch (Exception ignored) {
            }
            registered = false;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used.
    }

    @Override
    public void onInterrupt() {
        // Not used.
    }

    static boolean isRunning() {
        return instance != null;
    }

    static boolean lockScreen() {
        PowerService s = instance;
        return s != null && s.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
    }

    static boolean powerMenu() {
        PowerService s = instance;
        return s != null && s.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
    }
}
