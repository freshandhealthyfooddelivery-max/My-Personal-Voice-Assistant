package com.personal.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private static final int PICK_RAMADAN_FILE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(45, 50, 45, 50);

        TextView title = new TextView(this);
        title.setText("পার্সোনাল ভয়েস অ্যাসিস্ট্যান্ট (Pro)");
        title.setTextSize(21);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        // 1. Master Switch
        addSwitch(layout, "মাস্টার সুইচ (সব সিস্টেম অন/অফ)", "master_switch", false, true);

        // 2. Individual Feature Switches
        addSwitch(layout, "SMS ভয়েস অ্যানাউন্সার", "sms_switch", true, false);
        addSwitch(layout, "সম্পূর্ণ SMS পড়ে শোনানো (অফ থাকলে শুধু নাম বলবে)", "sms_full_read", false, false);
        addSwitch(layout, "ইনকামিং কল নাম/নাম্বার বলা", "call_switch", true, false);
        addSwitch(layout, "ব্যাটারি চার্জ ও পার্সেন্টেজ অ্যালার্ট", "battery_switch", true, false);

        // 3. Speaking Clock Interval
        TextView clockLbl = new TextView(this);
        clockLbl.setText("\nস্পিকিং ক্লক সময় নির্বাচন করুন:");
        layout.addView(clockLbl);

        LinearLayout clockRow = new LinearLayout(this);
        clockRow.setOrientation(LinearLayout.HORIZONTAL);
        addClockBtn(clockRow, "১৫ মি.", 15);
        addClockBtn(clockRow, "৩০ মি.", 30);
        addClockBtn(clockRow, "১ ঘণ্টা", 60);
        addClockBtn(clockRow, "বন্ধ", 0);
        layout.addView(clockRow);

        // 4. Voice Language Selection
        TextView langLbl = new TextView(this);
        langLbl.setText("\nভয়েস ভাষা নির্বাচন করুন:");
        layout.addView(langLbl);

        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        addLangBtn(langRow, "শুদ্ধ বাংলা", "bn");
        addLangBtn(langRow, "UK English", "en_uk");
        addLangBtn(langRow, "US English", "en_us");
        layout.addView(langRow);

        // 5. Custom Timer Auto-Call
        TextView callLbl = new TextView(this);
        callLbl.setText("\nঅটো-কল সিস্টেম (নাম্বার ও সেকেন্ড দিন):");
        layout.addView(callLbl);

        EditText numInput = new EditText(this);
        numInput.setHint("মোবাইল নাম্বার লিখুন (যেমন: 017...)");
        layout.addView(numInput);

        EditText secInput = new EditText(this);
        secInput.setHint("কত সেকেন্ড পর কল কেটে আবার দেবে (যেমন: 10)");
        layout.addView(secInput);

        Button startAutoCall = new Button(this);
        startAutoCall.setText("অটো-কল শুরু করুন");
        startAutoCall.setOnClickListener(v -> {
            String num = numInput.getText().toString().trim();
            String secStr = secInput.getText().toString().trim();
            if (!num.isEmpty() && !secStr.isEmpty()) {
                int sec = Integer.parseInt(secStr);
                startAutoCallLoop(num, sec);
            } else {
                Toast.makeText(this, "নাম্বার ও সেকেন্ড লিখুন", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(startAutoCall);

        // 6. Ramadan Calendar File Upload
        Button ramadanBtn = new Button(this);
        ramadanBtn.setText("রমজান ক্যালেন্ডার ফাইল (.txt) যুক্ত করুন");
        ramadanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            startActivityForResult(intent, PICK_RAMADAN_FILE);
        });
        layout.addView(ramadanBtn);

        // 7. Permissions Button
        Button permBtn = new Button(this);
        permBtn.setText("সব পারমিশন দিন (প্রথমবার আবশ্যক)");
        permBtn.setOnClickListener(v -> requestAllPermissions());
        layout.addView(permBtn);

        scroll.addView(layout);
        setContentView(scroll);

        if (prefs.getBoolean("master_switch", false)) {
            startService(new Intent(this, AssistantBackgroundService.class));
        }
    }

    private void addSwitch(LinearLayout parent, String label, String key, boolean def, boolean isMaster) {
        Switch sw = new Switch(this);
        sw.setText(label);
        sw.setPadding(0, 15, 0, 15);
        sw.setChecked(prefs.getBoolean(key, def));
        sw.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            if (isMaster) {
                Intent srv = new Intent(this, AssistantBackgroundService.class);
                if (checked) startService(srv);
                else stopService(srv);
            }
        });
        parent.addView(sw);
    }

    private void addClockBtn(LinearLayout row, String label, int mins) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(v -> {
            prefs.edit().putInt("clock_interval", mins).apply();
            Toast.makeText(this, "ঘড়ি সেট হয়েছে: " + label, Toast.LENGTH_SHORT).show();
        });
        row.addView(b);
    }

    private void addLangBtn(LinearLayout row, String label, String code) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(v -> {
            prefs.edit().putString("voice_lang", code).apply();
            SystemEventReceiver.speak(this, "ভয়েস পরিবর্তন করা হয়েছে");
        });
        row.addView(b);
    }

    private void startAutoCallLoop(String number, int seconds) {
        Toast.makeText(this, "অটো-কল চালু হয়েছে", Toast.LENGTH_SHORT).show();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, seconds + " সেকেন্ড পূর্ণ হয়েছে", Toast.LENGTH_SHORT).show();
        }, seconds * 1000L);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_RAMADAN_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                File outFile = new File(getExternalFilesDir(null), "ramadan.txt");
                FileOutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
                out.close();
                Toast.makeText(this, "রমজান ফাইল সফলভাবে যুক্ত হয়েছে!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "ফাইল যুক্ত করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestAllPermissions() {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.RECEIVE_SMS);
        perms.add(Manifest.permission.READ_SMS);
        perms.add(Manifest.permission.READ_PHONE_STATE);
        perms.add(Manifest.permission.CALL_PHONE);
        perms.add(Manifest.permission.READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        String[] arr = new String[perms.size()];
        for (int i = 0; i < perms.size(); i++) arr[i] = perms.get(i);
        ActivityCompat.requestPermissions(this, arr, 101);
    }
}
