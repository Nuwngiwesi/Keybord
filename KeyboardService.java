package com.example.kb;

import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.widget.*;

public class KeyboardService extends InputMethodService {
    boolean shift = false, emoji = false;
    LinearLayout root;

    static final String EMOJIS = "😀😃😄😁😆😅😂🤣😊😇🙂😉😍🥰😘😋😎🤩🥳😏😒😞😔😢😭😤😡🤬😱😨😰🤔🤗🙄😴🤒🤮👍👎👏🙌🙏💪👋✌️❤️🧡💛💚💙💜🖤💔🔥✨🎉🎂🎁🌹🌞🌙⭐🍕🍔🍟🍺☕⚽🎮🚗✈️📱💻";

    @Override
    public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1E1E1E);
        build();
        return root;
    }

    void build() {
        root.removeAllViews();
        if (emoji) buildEmoji(); else buildLetters();
    }

    Button key(String label, float w, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(18);
        b.setPadding(0, 0, 0, 0);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 130, w);
        p.setMargins(3, 3, 3, 3);
        b.setLayoutParams(p);
        return b;
    }

    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    void type(String s) {
        getCurrentInputConnection().commitText(s, 1);
    }

    void buildLetters() {
        String[] rows = {"1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm"};
        for (int i = 0; i < rows.length; i++) {
            LinearLayout r = row();
            if (i == 3) r.addView(key(shift ? "⬆" : "⇧", 1.5f, v -> { shift = !shift; build(); }));
            for (char c : rows[i].toCharArray()) {
                final String s = shift ? String.valueOf(c).toUpperCase() : String.valueOf(c);
                r.addView(key(s, 1f, v -> {
                    type(s);
                    if (shift) { shift = false; build(); }
                }));
            }
            if (i == 3) r.addView(key("⌫", 1.5f, v -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)));
            root.addView(r);
        }
        LinearLayout b = row();
        b.addView(key("😀", 1.3f, v -> { emoji = true; build(); }));
        b.addView(key(",", 1f, v -> type(",")));
        b.addView(key("space", 4f, v -> type(" ")));
        b.addView(key(".", 1f, v -> type(".")));
        b.addView(key("⏎", 1.3f, v -> sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)));
        root.addView(b);
    }

    void buildEmoji() {
        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(-1, 520));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout r = null;
        int n = 0;
        for (int i = 0; i < EMOJIS.length(); ) {
            int cp = EMOJIS.codePointAt(i);
            int len = Character.charCount(cp);
            String e = EMOJIS.substring(i, i + len);
            i += len;
            // keep variation selector with previous emoji
            while (i < EMOJIS.length() && EMOJIS.charAt(i) == '\uFE0F') { e += "\uFE0F"; i++; }
            if (n % 8 == 0) { r = row(); col.addView(r); }
            final String s = e;
            r.addView(key(s, 1f, v -> type(s)));
            n++;
        }
        sv.addView(col);
        root.addView(sv);
        LinearLayout b = row();
        b.addView(key("ABC", 1.5f, v -> { emoji = false; build(); }));
        b.addView(key("space", 4f, v -> type(" ")));
        b.addView(key("⌫", 1.5f, v -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)));
        root.addView(b);
    }
}
