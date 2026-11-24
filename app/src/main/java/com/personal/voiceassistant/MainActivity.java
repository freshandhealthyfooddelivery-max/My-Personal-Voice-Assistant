package com.personal.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
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
    private TextView statusBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#0B0F19"));
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0B0F19"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 50, 40, 60);

        TextView title = new TextView(this);
        title.setText("Voice Assistant Pro");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 25);
        root.addView(title);

        // Hero Card
        LinearLayout hero = card("#1E293B", "#38BDF8");
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        statusBadge = new TextView(this);
        boolean active = prefs.getBoolean("master_switch", false);
        updateBadge(active);
        top.addView(statusBadge);
        TextView bat = new TextView(this);
        bat.setText("   ⚡ " + getBat() + "%");
        bat.setTextColor(Color.parseColor("#94A3B8"));
        top.addView(bat);
        hero.addView(top);
        sw(hero, "মাস্টার পাওয়ার সুইচ (অন/অফ)", "master_switch", false, true);
        root.addView(hero);

        // Card 1: SMS & Notif
        LinearLayout c1 = card("#131A2A", "#1E293B");
        hdr(c1, "💬 মেসেজ ও নোটিফিকেশন");
        sw(c1, "SMS ভয়েস অ্যানাউন্সার", "sms_switch", true, false);
        sw(c1, "সম্পূর্ণ SMS পড়ে শোনানো", "sms_full_read", false, false);
        sw(c1, "সব অ্যাপের নোটিফিকেশন বলা", "app_notif_switch", true, false);
        sw(c1, "হেডফোন প্রাইভেসি মোড", "headphone_privacy", false, false);
        root.addView(c1);

        // Card 2: Call & Riding
        LinearLayout c2 = card("#131A2A", "#1E293B");
        hdr(c2, "📞 স্মার্ট কল ও রাইডিং মোড");
        sw(c2, "ইনকামিং কলের নাম/নাম্বার বলা", "call_switch", true, false);
        sw(c2, "রাইডিং মোড (অটো স্পিকার)", "riding_mode", false, false);
        sw(c2, "সাইলেন্ট করলে ১ ঘণ্টা পর সাউন্ড অন", "auto_unsilent", false, false);
        EditText numIn = inp("অটো-কল নাম্বার (017...)");
        EditText secIn = inp("কত সেকেন্ড পর কাটবে (10)");
        c2.addView(numIn);
        c2.addView(secIn);
        btn(c2, "অটো-কল শুরু করুন", "#10B981", v -> {
            String n = numIn.getText().toString().trim();
            String s = secIn.getText().toString().trim();
            if (!n.isEmpty() && !s.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + n));
                startActivity(i);
                new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(this, s + " সেকেন্ড পূর্ণ", Toast.LENGTH_SHORT).show(), Integer.parseInt(s) * 1000L);
            }
        });
        root.addView(c2);

        // Card 3: Power & Security
        LinearLayout c3 = card("#131A2A", "#1E293B");
        hdr(c3, "🛡️ পাওয়ার, নেটওয়ার্ক ও নিরাপত্তা");
        sw(c3, "চার্জার ও ব্যাটারি অ্যালার্ট", "battery_switch", true, false);
        sw(c3, "চার্জার চুরি অ্যালার্ম", "theft_alarm", false, false);
        sw(c3, "Wi-Fi ও ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
        sw(c3, "মেমোরি ৯০% স্টোরেজ ওয়ার্নিং", "storage_warn", true, false);
        root.addView(c3);

        // Card 4: Clock & Voice
        LinearLayout c4 = card("#131A2A", "#1E293B");
        hdr(c4, "⏰ স্পিকিং ক্লক ও ভয়েস");
        sw(c4, "রাতের স্লিপ মোড (১২টা - ৫টা)", "sleep_mode", true, false);
        sw(c4, "সকাল ৭টায় গুড মর্নিং ব্রিফিং", "morning_brief", true, false);
        LinearLayout r1 = new LinearLayout(this);
        pill(r1, "১৫ মি.", "#2563EB", v -> setClock(15));
        pill(r1, "৩০ মি.", "#2563EB", v -> setClock(30));
        pill(r1, "১ ঘণ্টা", "#2563EB", v -> setClock(60));
        pill(r1, "বন্ধ", "#334155", v -> setClock(0));
        c4.addView(r1);
        LinearLayout r2 = new LinearLayout(this);
        r2.setPadding(0, 15, 0, 0);
        pill(r2, "বাংলা", "#3B82F6", v -> setLang("bn"));
        pill(r2, "UK Eng", "#3B82F6", v -> setLang("en_uk"));
        pill(r2, "মেয়ে কণ্ঠ", "#DB2777", v -> setMale(false));
        pill(r2, "ছেলে কণ্ঠ", "#475569", v -> setMale(true));
        c4.addView(r2);
        root.addView(c4);

        // Card 5: Namaz & Ramadan
        LinearLayout c5 = card("#131A2A", "#1E293B");
        hdr(c5, "🕌 নামাজ ও রমজান ক্যালেন্ডার");
        sw(c5, "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", true, false);
        EditText fIn = inp("ফজর (05:00)"); fIn.setText(prefs.getString("fajr_time", "05:00")); c5.addView(fIn);
        EditText dIn = inp("জোহর (13:00)"); dIn.setText(prefs.getString("dhuhr_time", "13:00")); c5.addView(dIn);
        EditText aIn = inp("আসর (16:15)"); aIn.setText(prefs.getString("asr_time", "16:15")); c5.addView(aIn);
        EditText mIn = inp("মাগরিব (18:05)"); mIn.setText(prefs.getString("maghrib_time", "18:05")); c5.addView(mIn);
        EditText iIn = inp("এশা (19:30)"); iIn.setText(prefs.getString("isha_time", "19:30")); c5.addView(iIn);
        btn(c5, "নামাজের সময় সেভ করুন", "#0EA5E9", v -> {
            prefs.edit().putString("fajr_time", fIn.getText().toString()).putString("dhuhr_time", dIn.getText().toString()).putString("asr_time", aIn.getText().toString()).putString("maghrib_time", mIn.getText().toString()).putString("isha_time", iIn.getText().toString()).apply();
            Toast.makeText(this, "সেভ হয়েছে", Toast.LENGTH_SHORT).show();
        });
        btn(c5, "রমজান ফাইল (.txt) যুক্ত করুন", "#8B5CF6", v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("text/plain");
            startActivityForResult(i, 202);
        });
        root.addView(c5);

        // Card 6: Permissions
        LinearLayout c6 = card("#131A2A", "#1E293B");
        hdr(c6, "⚙️ সিস্টেম পারমিশন");
        btn(c6, "১. কল ও SMS পারমিশন দিন", "#F59E0B", v -> reqPerms());
        btn(c6, "২. নোটিফিকেশন অ্যাক্সেস অন করুন", "#EC4899", v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        root.addView(c6);

        scroll.addView(root);
        setContentView(scroll);
        if (active) startService(new Intent(this, AssistantBackgroundService.class));
    }

    private LinearLayout card(String bg, String st) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(36, 32, 36, 32);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(bg)); g.setCornerRadius(32f); g.setStroke(2, Color.parseColor(st));
        l.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, 24); l.setLayoutParams(p);
        return l;
    }

    private void hdr(LinearLayout p, String t) {
        TextView v = new TextView(this); v.setText(t); v.setTextColor(Color.WHITE); v.setTextSize(16); v.setTypeface(null, Typeface.BOLD); v.setPadding(0, 0, 0, 14); p.addView(v);
    }

    private void sw(LinearLayout p, String t, String k, boolean d, boolean m) {
        Switch s = new Switch(this); s.setText(t); s.setTextColor(Color.parseColor("#E2E8F0")); s.setPadding(0, 14, 0, 14); s.setChecked(prefs.getBoolean(k, d));
        s.setOnCheckedChangeListener((b, c) -> {
            prefs.edit().putBoolean(k, c).apply();
            if (m) { updateBadge(c); Intent i = new Intent(this, AssistantBackgroundService.class); if (c) startService(i); else stopService(i); }
        });
        p.addView(s);
    }

    private void updateBadge(boolean a) {
        statusBadge.setText(a ? " ● ACTIVE " : " ○ STANDBY ");
        statusBadge.setTextColor(a ? Color.parseColor("#10B981") : Color.parseColor("#94A3B8"));
        GradientDrawable g = new GradientDrawable(); g.setColor(a ? Color.parseColor("#064E3B") : Color.parseColor("#0F172A")); g.setCornerRadius(40f);
        statusBadge.setBackground(g); statusBadge.setPadding(12, 6, 12, 6);
    }

    private void pill(LinearLayout r, String t, String c, android.view.View.OnClickListener l) {
        Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(11); b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(35f); b.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 90, 1f); p.setMargins(5, 0, 5, 0); b.setLayoutParams(p);
        b.setOnClickListener(l); r.addView(b);
    }

    private EditText inp(String h) {
        EditText e = new EditText(this); e.setHint(h); e.setHintTextColor(Color.GRAY); e.setTextColor(Color.WHITE); e.setTextSize(13); e.setPadding(24, 18, 24, 18);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#0B0F19")); g.setCornerRadius(20f); g.setStroke(2, Color.parseColor("#334155")); e.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 6, 0, 10); e.setLayoutParams(p); return e;
    }

    private void btn(LinearLayout p, String t, String c, android.view.View.OnClickListener l) {
        Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(24f); b.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 105); lp.setMargins(0, 10, 0, 6); b.setLayoutParams(lp);
        b.setOnClickListener(l); p.addView(b);
    }

    private void setClock(int m) { prefs.edit().putInt("clock_interval", m).apply(); Toast.makeText(this, "সেট হয়েছে", Toast.LENGTH_SHORT).show(); }
    private void setLang(String c) { prefs.edit().putString("voice_lang", c).apply(); SystemEventReceiver.speak(this, "ভয়েস পরিবর্তন হয়েছে"); }
    private void setMale(boolean m) { prefs.edit().putBoolean("voice_male", m).apply(); SystemEventReceiver.speak(this, m ? "ছেলে কণ্ঠ" : "মেয়ে কণ্ঠ"); }
    private int getBat() { Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); return i != null ? i.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) : 100; }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 202 && res == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                FileOutputStream out = new FileOutputStream(new File(getExternalFilesDir(null), "ramadan.txt"));
                byte[] buf = new byte; int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close(); out.close();
                Toast.makeText(this, "রমজান ফাইল যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        }
    }

    private void reqPerms() {
        List<String> p = new ArrayList<>();
        p.add(Manifest.permission.RECEIVE_SMS); p.add(Manifest.permission.READ_SMS); p.add(Manifest.permission.READ_PHONE_STATE); p.add(Manifest.permission.CALL_PHONE); p.add(Manifest.permission.READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS);
        String[] a = new String[p.size()];
        for (int i = 0; i < p.size(); i++) a[i] = p.get(i);
        ActivityCompat.requestPermissions(this, a, 101);
    }
}
