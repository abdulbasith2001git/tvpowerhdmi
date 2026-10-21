package com.tvpower.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Tiny settings store shared by the screen and the background service. */
final class Prefs {
    private Prefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("tvpower", Context.MODE_PRIVATE);
    }

    static int countdown(Context c) {
        return sp(c).getInt("countdown", 3);
    }

    static void setCountdown(Context c, int seconds) {
        sp(c).edit().putInt("countdown", seconds).apply();
    }

    /** 0 = lock via accessibility, 1 = lock via device admin, 2 = power menu */
    static int method(Context c) {
        return sp(c).getInt("method", 0);
    }

    static void setMethod(Context c, int m) {
        sp(c).edit().putInt("method", m).apply();
    }

    static String inputId(Context c) {
        return sp(c).getString("input", null);
    }

    static void setInputId(Context c, String id) {
        sp(c).edit().putString("input", id).apply();
    }

    static boolean wakeSwitch(Context c) {
        return sp(c).getBoolean("wake", true);
    }

    static void setWakeSwitch(Context c, boolean on) {
        sp(c).edit().putBoolean("wake", on).apply();
    }
}
