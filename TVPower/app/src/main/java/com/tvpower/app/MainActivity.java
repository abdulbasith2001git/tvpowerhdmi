package com.tvpower.app;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * One simple screen:
 *   [ Power Off ]  -> shows a big countdown, then turns the screen off
 *   settings: off method, countdown length, which HDMI input to jump to on wake
 */
public class MainActivity extends Activity {

    private static final String[] METHODS = {
            "Lock screen (Accessibility)",
            "Lock screen (Device admin)",
            "Open power menu"
    };
    private static final int[] COUNTS = {2, 3, 5, 10};

    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView btnOff, btnMethod, btnCount, btnInput, btnWake, btnTest, status;
    private FrameLayout overlay;
    private TextView overlayNum;

    private boolean counting = false;
    private int remaining = 0;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            remaining--;
            if (remaining <= 0) {
                stopCountdown();
                performOff();
            } else {
                overlayNum.setText(String.valueOf(remaining));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        btnOff.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If something else comes in front, drop the countdown.
        if (counting) stopCountdown();
    }

    // ------------------------------------------------------------------ UI

    private TextView makeButton(String text, int normalColor, final Runnable action) {
        final int normal = normalColor;
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(22);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(24), dp(14), dp(24), dp(14));
        t.setFocusable(true);
        t.setClickable(true);

        final GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(normal);
        t.setBackground(bg);

        t.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean focused) {
                bg.setColor(focused ? Color.WHITE : normal);
                t.setTextColor(focused ? Color.BLACK : Color.WHITE);
            }
        });
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(7), 0, dp(7));
        t.setLayoutParams(lp);
        return t;
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_VERTICAL);
        col.setPadding(dp(160), dp(30), dp(160), dp(30));

        TextView title = new TextView(this);
        title.setText("TV Power");
        title.setTextSize(34);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        title.setPadding(dp(6), 0, 0, dp(14));
        col.addView(title);

        btnOff = makeButton("⏻   Power Off", 0xFF9B1C1C, new Runnable() {
            @Override
            public void run() {
                startCountdown();
            }
        });
        btnOff.setTextSize(28);
        btnOff.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(btnOff);

        btnMethod = makeButton("", 0xFF1E1E1E, new Runnable() {
            @Override
            public void run() {
                Prefs.setMethod(MainActivity.this, (Prefs.method(MainActivity.this) + 1) % METHODS.length);
                refresh();
            }
        });
        col.addView(btnMethod);

        btnCount = makeButton("", 0xFF1E1E1E, new Runnable() {
            @Override
            public void run() {
                int cur = Prefs.countdown(MainActivity.this);
                int next = COUNTS[0];
                for (int i = 0; i < COUNTS.length; i++) {
                    if (COUNTS[i] == cur) {
                        next = COUNTS[(i + 1) % COUNTS.length];
                        break;
                    }
                }
                Prefs.setCountdown(MainActivity.this, next);
                refresh();
            }
        });
        col.addView(btnCount);

        btnInput = makeButton("", 0xFF1E1E1E, new Runnable() {
            @Override
            public void run() {
                cycleInput();
            }
        });
        col.addView(btnInput);

        btnWake = makeButton("", 0xFF1E1E1E, new Runnable() {
            @Override
            public void run() {
                Prefs.setWakeSwitch(MainActivity.this, !Prefs.wakeSwitch(MainActivity.this));
                refresh();
            }
        });
        col.addView(btnWake);

        btnTest = makeButton("Test: switch to that input now", 0xFF1E1E1E, new Runnable() {
            @Override
            public void run() {
                String id = InputSwitcher.chosenId(MainActivity.this);
                if (id == null || !InputSwitcher.switchTo(MainActivity.this, id)) {
                    toast("Could not switch. No HDMI input found?");
                }
            }
        });
        col.addView(btnTest);

        status = new TextView(this);
        status.setTextSize(15);
        status.setTextColor(0x99FFFFFF);
        status.setPadding(dp(6), dp(14), 0, 0);
        col.addView(status);

        root.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Countdown overlay (hidden until Power Off is pressed)
        overlay = new FrameLayout(this);
        overlay.setBackgroundColor(0xF2000000);
        overlay.setVisibility(View.GONE);

        LinearLayout oc = new LinearLayout(this);
        oc.setOrientation(LinearLayout.VERTICAL);
        oc.setGravity(Gravity.CENTER);

        TextView line1 = new TextView(this);
        line1.setText("TV turning off in");
        line1.setTextSize(30);
        line1.setTextColor(0xCCFFFFFF);
        line1.setGravity(Gravity.CENTER);
        oc.addView(line1);

        overlayNum = new TextView(this);
        overlayNum.setTextSize(150);
        overlayNum.setTextColor(Color.WHITE);
        overlayNum.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        overlayNum.setGravity(Gravity.CENTER);
        oc.addView(overlayNum);

        TextView line3 = new TextView(this);
        line3.setText("Press Back or OK to cancel");
        line3.setTextSize(20);
        line3.setTextColor(0x88FFFFFF);
        line3.setGravity(Gravity.CENTER);
        oc.addView(line3);

        overlay.addView(oc, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);
    }

    private void refresh() {
        btnMethod.setText("Off method:   " + METHODS[Prefs.method(this)]);
        btnCount.setText("Countdown:   " + Prefs.countdown(this) + " seconds");

        String id = InputSwitcher.chosenId(this);
        String label = "no HDMI input found";
        if (id != null) {
            for (TvInputInfo i : InputSwitcher.inputs(this)) {
                if (i.getId().equals(id)) {
                    label = InputSwitcher.label(this, i);
                    break;
                }
            }
        }
        btnInput.setText("When TV turns on, go to:   " + label);
        btnWake.setText("Auto-switch when TV turns on:   " + (Prefs.wakeSwitch(this) ? "ON" : "OFF"));

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean admin = dpm != null && dpm.isAdminActive(new ComponentName(this, AdminReceiver.class));
        status.setText("Helper service: " + (PowerService.isRunning() ? "running" : "NOT running (run the setup file)")
                + "     Device admin: " + (admin ? "on" : "off"));
    }

    private void cycleInput() {
        List<TvInputInfo> all = InputSwitcher.inputs(this);
        if (all.isEmpty()) {
            toast("No HDMI inputs found on this TV");
            return;
        }
        String cur = InputSwitcher.chosenId(this);
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(cur)) {
                idx = i;
                break;
            }
        }
        Prefs.setInputId(this, all.get((idx + 1) % all.size()).getId());
        refresh();
    }

    // ----------------------------------------------------------- countdown

    private void startCountdown() {
        if (counting) return;
        counting = true;
        remaining = Prefs.countdown(this);
        overlayNum.setText(String.valueOf(remaining));
        overlay.setVisibility(View.VISIBLE);
        handler.postDelayed(tick, 1000);
    }

    private void stopCountdown() {
        counting = false;
        handler.removeCallbacks(tick);
        overlay.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (counting) {
            int k = e.getKeyCode();
            if (k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_DPAD_CENTER
                    || k == KeyEvent.KEYCODE_ENTER) {
                if (e.getAction() == KeyEvent.ACTION_DOWN) {
                    stopCountdown();
                    toast("Cancelled");
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(e);
    }

    // ----------------------------------------------------------------- off

    private void performOff() {
        boolean ok;
        switch (Prefs.method(this)) {
            case 1:
                ok = lockWithAdmin();
                break;
            case 2:
                ok = PowerService.powerMenu();
                break;
            default:
                ok = PowerService.lockScreen();
                break;
        }
        if (!ok) {
            toast("That method is not ready. Check the status line, or try another method.");
        }
    }

    private boolean lockWithAdmin() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(this, AdminReceiver.class);
            if (dpm != null && dpm.isAdminActive(admin)) {
                dpm.lockNow();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
