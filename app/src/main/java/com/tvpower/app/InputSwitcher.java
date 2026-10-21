package com.tvpower.app;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Finds the TV's HDMI (pass-through) inputs and switches to one. */
final class InputSwitcher {
    private InputSwitcher() {}

    private static long lastSwitch = 0;

    static List<TvInputInfo> inputs(Context c) {
        List<TvInputInfo> out = new ArrayList<>();
        try {
            TvInputManager m = (TvInputManager) c.getSystemService(Context.TV_INPUT_SERVICE);
            if (m == null) return out;
            for (TvInputInfo i : m.getTvInputList()) {
                if (i.isPassthroughInput()) out.add(i);
            }
        } catch (Exception ignored) {
        }
        final Context ctx = c;
        Collections.sort(out, new Comparator<TvInputInfo>() {
            @Override
            public int compare(TvInputInfo a, TvInputInfo b) {
                return label(ctx, a).compareToIgnoreCase(label(ctx, b));
            }
        });
        return out;
    }

    static String label(Context c, TvInputInfo i) {
        CharSequence l = null;
        try {
            l = i.loadCustomLabel(c);
            if (l == null || l.length() == 0) l = i.loadLabel(c);
        } catch (Exception ignored) {
        }
        return (l == null || l.length() == 0) ? i.getId() : l.toString();
    }

    /** The input to use: the saved one, or the first HDMI input found. */
    static String chosenId(Context c) {
        String saved = Prefs.inputId(c);
        List<TvInputInfo> all = inputs(c);
        if (saved != null) {
            for (TvInputInfo i : all) {
                if (i.getId().equals(saved)) return saved;
            }
        }
        return all.isEmpty() ? null : all.get(0).getId();
    }

    static boolean switchTo(Context c, String inputId) {
        if (inputId == null) return false;
        try {
            Intent in = new Intent(Intent.ACTION_VIEW,
                    TvContract.buildChannelUriForPassthroughInput(inputId));
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(in);
            lastSwitch = SystemClock.elapsedRealtime();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Used on wake-up. Ignores repeats within a few seconds. */
    static void switchSaved(Context c) {
        if (SystemClock.elapsedRealtime() - lastSwitch < 8000 && lastSwitch != 0) return;
        switchTo(c, chosenId(c));
    }
}
