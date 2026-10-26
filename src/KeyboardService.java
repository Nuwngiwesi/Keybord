package com.example.kb;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class KeyboardService extends InputMethodService {
    static final int BG = 0xFF1B1B1B, KEY = 0xFF3C3C3C, ACC = 0xFF2F4A38,
            TXT = 0xFFEDEDED, HINT = 0xFF9A9A9A;

    static final String EMOJIS = "😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 😉 😍 🥰 😘 😋 😎 🤩 🥳 😏 😒 😞 😔 😢 😭 😤 😡 🤬 😱 😨 😰 🤔 🤗 🙄 😴 🤒 🤮 👍 👎 👏 🙌 🙏 💪 👋 ✌️ ❤️ 🧡 💛 💚 💙 💜 🖤 💔 🔥 ✨ 🎉 🎂 🎁 🌹 🌞 🌙 ⭐ 🍕 🍔 🍟 🍺 ☕ ⚽ 🎮 🚗 ✈️ 📱 💻";

    int mode = 0; // 0 letters, 1 symbols, 2 emoji
    boolean shift = false, caps = false;
    LinearLayout root;
    final Handler h = new Handler(Looper.getMainLooper());

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(4), dp(4), dp(4), dp(8));
        build();
        return root;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (root != null) {
            mode = 0;
            shift = false;
            caps = false;
            build();
        }
    }

    void build() {
        root.removeAllViews();
        root.addView(toolbar());
        if (mode == 0) letters();
        else if (mode == 1) symbols();
        else emoji();
    }

    GradientDrawable bg(int c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(dp(8));
        return g;
    }

    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    View spacer(float w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, w));
        return v;
    }

    View key(String label, String hint, float w, int color,
             final Runnable tap, final Runnable lp, final boolean repeat) {
        FrameLayout f = new FrameLayout(this);
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, color == 0 ? dp(44) : dp(50), w);
        p.setMargins(dp(2), dp(3), dp(2), dp(3));
        f.setLayoutParams(p);
        f.setBackground(bg(color));

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(TXT);
        t.setTextSize(label.matches("[A-Za-z0-9?]{3,}") ? 14 : 20);
        t.setGravity(Gravity.CENTER);
        f.addView(t, new FrameLayout.LayoutParams(-1, -1));

        if (hint != null) {
            TextView hv = new TextView(this);
            hv.setText(hint);
            hv.setTextColor(HINT);
            hv.setTextSize(10);
            FrameLayout.LayoutParams hp =
                    new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.END);
            hp.setMargins(0, dp(2), dp(6), 0);
            f.addView(hv, hp);
        }

        final boolean[] done = {false};
        final Runnable rep = new Runnable() {
            @Override
            public void run() {
                tap.run();
                h.postDelayed(this, 50);
            }
        };
        final Runnable longR = new Runnable() {
            @Override
            public void run() {
                done[0] = true;
                if (repeat) rep.run();
                else if (lp != null) lp.run();
            }
        };
        final boolean needLong = repeat || lp != null;

        f.setOnTouchListener((v, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    done[0] = false;
                    v.setAlpha(0.6f);
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    if (needLong) h.postDelayed(longR, 400);
                    return true;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1f);
                    h.removeCallbacks(longR);
                    h.removeCallbacks(rep);
                    if (!done[0]) tap.run();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    h.removeCallbacks(longR);
                    h.removeCallbacks(rep);
                    return true;
            }
            return true;
        });
        return f;
    }

    LinearLayout toolbar() {
        LinearLayout r = row();
        r.addView(key("⚙", null, 1f, 0, () -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }, null, false));
        r.addView(key("⌨", null, 1f, 0, () -> {
            InputMethodManager m = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            m.showInputMethodPicker();
        }, null, false));
        r.addView(spacer(5f));
        r.addView(key("⌄", null, 1f, 0, () -> requestHideSelf(0), null, false));
        return r;
    }

    void type(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(s, 1);
    }

    void enter() {
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ei != null && ic != null) {
            int a = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
            boolean multi = (ei.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
            boolean noAction = (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
            if (!multi && !noAction && a != EditorInfo.IME_ACTION_NONE
                    && a != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(a);
                return;
            }
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
    }

    View backspace() {
        return key("⌫", null, 1.5f, KEY,
                () -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL), null, true);
    }

    void letters() {
        String[] rows = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
        String nums = "1234567890";
        for (int i = 0; i < 3; i++) {
            LinearLayout r = row();
            if (i == 1) r.addView(spacer(0.5f));
            if (i == 2) {
                r.addView(key(caps ? "⇪" : (shift ? "⬆" : "⇧"), null, 1.5f, KEY, () -> {
                    if (caps) {
                        caps = false;
                        shift = false;
                    } else {
                        shift = !shift;
                    }
                    build();
                }, () -> {
                    caps = true;
                    shift = true;
                    build();
                }, false));
            }
            for (int j = 0; j < rows[i].length(); j++) {
                final String c = String.valueOf(rows[i].charAt(j));
                final String out = shift ? c.toUpperCase() : c;
                final String hint = i == 0 ? String.valueOf(nums.charAt(j)) : null;
                Runnable lp = null;
                if (hint != null) lp = () -> type(hint);
                r.addView(key(out, hint, 1f, KEY, () -> {
                    type(out);
                    if (shift && !caps) {
                        shift = false;
                        build();
                    }
                }, lp, false));
            }
            if (i == 1) r.addView(spacer(0.5f));
            if (i == 2) r.addView(backspace());
            root.addView(r);
        }
        LinearLayout b = row();
        b.addView(key("?123", null, 1.5f, KEY, () -> {
            mode = 1;
            build();
        }, null, false));
        b.addView(key(",", null, 1f, KEY, () -> type(","), null, false));
        b.addView(key("😀", null, 1f, KEY, () -> {
            mode = 2;
            build();
        }, null, false));
        b.addView(key("", null, 4f, KEY, () -> type(" "), null, false));
        b.addView(key(".", null, 1f, KEY, () -> type("."), null, false));
        b.addView(key("⏎", null, 1.5f, ACC, () -> enter(), null, false));
        root.addView(b);
    }

    void symbols() {
        String[] rows = {"1234567890", "@#$%&-+()/", "*\"':;!?"};
        for (int i = 0; i < 3; i++) {
            LinearLayout r = row();
            if (i == 2) r.addView(key("=", null, 1.5f, KEY, () -> type("="), null, false));
            for (int j = 0; j < rows[i].length(); j++) {
                final String c = String.valueOf(rows[i].charAt(j));
                r.addView(key(c, null, 1f, KEY, () -> type(c), null, false));
            }
            if (i == 2) r.addView(backspace());
            root.addView(r);
        }
        LinearLayout b = row();
        b.addView(key("ABC", null, 1.5f, KEY, () -> {
            mode = 0;
            build();
        }, null, false));
        b.addView(key(",", null, 1f, KEY, () -> type(","), null, false));
        b.addView(key("", null, 5f, KEY, () -> type(" "), null, false));
        b.addView(key(".", null, 1f, KEY, () -> type("."), null, false));
        b.addView(key("⏎", null, 1.5f, ACC, () -> enter(), null, false));
        root.addView(b);
    }

    void emoji() {
        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(230)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        String[] list = EMOJIS.split(" ");
        LinearLayout r = null;
        for (int i = 0; i < list.length; i++) {
            if (i % 8 == 0) {
                r = row();
                col.addView(r);
            }
            final String e = list[i];
            r.addView(key(e, null, 1f, 0, () -> type(e), null, false));
        }
        int rem = list.length % 8;
        if (rem != 0 && r != null) {
            for (int k = rem; k < 8; k++) r.addView(spacer(1f));
        }
        sv.addView(col);
        root.addView(sv);

        LinearLayout b = row();
        b.addView(key("ABC", null, 1.5f, KEY, () -> {
            mode = 0;
            build();
        }, null, false));
        b.addView(key("", null, 4f, KEY, () -> type(" "), null, false));
        b.addView(backspace());
        root.addView(b);
    }
}
