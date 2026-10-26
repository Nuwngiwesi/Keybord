package com.example.kb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int p = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(p, p * 2, p, p);
        l.setBackgroundColor(0xFF121212);

        TextView title = new TextView(this);
        title.setText("My Keyboard");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        l.addView(title);

        status = new TextView(this);
        status.setTextSize(16);
        status.setTextColor(0xFFCCCCCC);
        status.setPadding(0, p, 0, p);
        l.addView(status);

        Button b1 = new Button(this);
        b1.setText("1. Keyboard eka Enable karanna");
        b1.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        l.addView(b1);

        Button b2 = new Button(this);
        b2.setText("2. Keyboard eka Select karanna");
        b2.setOnClickListener(v -> {
            InputMethodManager m = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            m.showInputMethodPicker();
        });
        l.addView(b2);

        EditText test = new EditText(this);
        test.setHint("3. Methana type karala test karanna");
        test.setHintTextColor(0xFF777777);
        test.setTextColor(0xFFFFFFFF);
        test.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        test.setMinLines(4);
        test.setGravity(Gravity.TOP);
        l.addView(test);

        setContentView(l);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager m = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        boolean enabled = false;
        for (InputMethodInfo i : m.getEnabledInputMethodList()) {
            if (i.getPackageName().equals(getPackageName())) enabled = true;
        }
        String def = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        boolean selected = def != null && def.startsWith(getPackageName() + "/");
        status.setText("Enabled: " + (enabled ? "✅" : "❌") + "\nSelected: " + (selected ? "✅" : "❌"));
    }
}
