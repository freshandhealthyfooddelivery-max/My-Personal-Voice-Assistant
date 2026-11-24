package com.personal.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private LinearLayout contentArea;
    private boolean isEn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#070B14"));
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        isEn = prefs.getBoolean("ui_english", false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#070B14"));

        LinearLayout top = new LinearLayout(this);
        top.setPadding(40, 45, 40, 25); top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout tBox = new LinearLayout(this);
        tBox.setOrientation(LinearLayout.VERTICAL); tBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView t1 = new TextView(this); t1.setText("AI Voice Pro"); t1.setTextColor(Color.WHITE); t1.setTextSize(23); t1.setTypeface(null, Typeface.BOLD);
        TextView t2 = new TextView(this); t2.setText("✦ Crafted by NAHIM"); t2.setTextColor(Color.parseColor("#FBBF24")); t2.setTextSize(11); t2.setTypeface(null, Typeface.BOLD);
        tBox.addView(t1); tBox.addView(t2); top.addView(tBox);

        Button lBtn = new Button(this); lBtn.setText(isEn ? "বাংলা" : "English"); lBtn.setTextColor(Color.WHITE); lBtn.setAllCaps(false);
        GradientDrawable lg = new GradientDrawable(); lg.setColor(Color.parseColor("#1E293B")); lg.setCornerRadius(30f); lg.setStroke(2, Color.parseColor("#38BDF8")); lBtn.setBackground(lg);
        lBtn.setOnClickListener(v -> { isEn = !isEn; prefs.edit().putBoolean("ui_english", isEn).apply(); recreate(); });
        top.addView(lBtn); root.addView(top);

        ScrollView sc = new ScrollView(this); sc.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        contentArea = new LinearLayout(this); contentArea.setOrientation(LinearLayout.VERTICAL); contentArea.setPadding(36, 10, 36, 30);
        sc.addView(contentArea); root.addView(sc);

        LinearLayout nav = new LinearLayout(this); nav.setBackgroundColor(Color.parseColor("#0F172A")); nav.setPadding(10, 14, 10, 18);
        tab(nav, isEn ? "Home" : "হোম", 0); tab(nav, isEn ? "Call/Msg" : "কল/মেসেজ", 1); tab(nav, isEn ? "Security" : "নিরাপত্তা", 2);
        tab(nav, isEn ? "Calendar" : "ক্যালেন্ডার", 3); tab(nav, isEn ? "Settings" : "সেটিংস", 4);
        root.addView(nav); setContentView(root); render(0);
        if (prefs.getBoolean("master_switch", false)) startService(new Intent(this, AssistantBackgroundService.class));
    }

    private void tab(LinearLayout b, String l, int i) {
        Button bt = new Button(this); bt.setText(l); bt.setTextColor(Color.WHITE); bt.setTextSize(11); bt.setAllCaps(false); bt.setBackgroundColor(Color.TRANSPARENT);
        bt.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f)); bt.setOnClickListener(v -> render(i)); b.addView(bt);
    }

    private void render(int t) {
        contentArea.removeAllViews();
        if (t == 0) {
            LinearLayout h = card("#111827", "#38BDF8"); boolean on = prefs.getBoolean("master_switch", false);
            TextView st = new TextView(this); st.setText(on ? (isEn ? "● SYSTEM ACTIVE" : "● সিস্টেম চালু আছে") : (isEn ? "○ STANDBY" : "○ বন্ধ আছে"));
            st.setTextColor(on ? Color.parseColor("#10B981") : Color.GRAY); st.setTypeface(null, Typeface.BOLD); h.addView(st);
            sw(h, isEn ? "Master Power Switch" : "মাস্টার পাওয়ার সুইচ (অন/অফ)", "master_switch", false, true); contentArea.addView(h);
            LinearLayout v = card("#111827", "#1E293B"); hdr(v, isEn ? "🎙️ Live Voice Ask & Info" : "🎙️ ভয়েস কমান্ড ও তাৎক্ষণিক তথ্য");
            btn(v, isEn ? "🎤 Ask by Voice (Date / Weather)" : "🎤 মুখে জিজ্ঞেস করুন (তারিখ / আবহাওয়া)", "#2563EB", x -> voiceAsk());
            btn(v, isEn ? "📅 Bangla Date & Day" : "📅 বাংলা তারিখ, মাস ও বার শুনুন", "#0D9488", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this)));
            btn(v, isEn ? "🌙 Arabic Hijri Date" : "🌙 আরবি হিজরি তারিখ ও সন শুনুন", "#7C3AED", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this)));
            btn(v, isEn ? "🌦️ Live Weather Report" : "🌦️ বর্তমান আবহাওয়ার বিস্তারিত শুনুন", "#0284C7", x -> SmartCalendarWeather.fetchAndSpeakWeather(this));
            contentArea.addView(v);
        } else if (t == 1) {
            LinearLayout c = card("#111827", "#1E293B"); hdr(c, isEn ? "💬 Call & Message Settings" : "💬 কল ও মেসেজ সেটিংস");
            sw(c, isEn ? "SMS Voice Announcer" : "SMS ভয়েস অ্যানাউন্সার", "sms_switch", true, false);
            sw(c, isEn ? "Read Full SMS" : "সম্পূর্ণ SMS পড়ে শোনানো", "sms_full_read", false, false);
            sw(c, isEn ? "App Notifications (FB/WA)" : "সব অ্যাপের নোটিফিকেশন বলা", "app_notif_switch", true, false);
            sw(c, isEn ? "Headphone Privacy Mode" : "হেডফোন প্রাইভেসি মোড", "headphone_privacy", false, false);
            sw(c, isEn ? "Incoming Call Announcer" : "ইনকামিং কলের নাম/নাম্বার বলা", "call_switch", true, false);
            sw(c, isEn ? "Bike Riding Auto-Speaker" : "বাইক রাইডিং মোড (অটো স্পিকার)", "riding_mode", false, false);
            sw(c, isEn ? "Auto Un-Silent (1 Hr)" : "সাইলেন্ট করলে ১ ঘণ্টা পর সাউন্ড অন", "auto_unsilent", false, false);
            EditText n = inp(isEn ? "Number (017...)" : "মোবাইল নাম্বার"); EditText s = inp(isEn ? "Seconds (10)" : "কত সেকেন্ড পর কাটবে");
            c.addView(n); c.addView(s);
            btn(c, isEn ? "Start Auto-Call" : "অটো-কল শুরু করুন", "#10B981", x -> {
                if (!n.getText().toString().isEmpty() && !s.getText().toString().isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + n.getText().toString().trim())));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show(), Integer.parseInt(s.getText().toString().trim()) * 1000L);
                }
            }); contentArea.addView(c);
        } else if (t == 2) {
            LinearLayout c = card("#111827", "#1E293B"); hdr(c, isEn ? "🛡️ Power & Security" : "🛡️ পাওয়ার ও নিরাপত্তা");
            sw(c, isEn ? "Charger & Battery % Voice" : "চার্জার ও ব্যাটারি পার্সেন্টেজ বলা", "battery_switch", true, false);
            sw(c, isEn ? "Anti-Theft Charger Alarm" : "চার্জার চুরি অ্যালার্ম", "theft_alarm", false, false);
            sw(c, isEn ? "Wi-Fi & Data Drop Alert" : "ওয়াইফাই ও ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
            sw(c, isEn ? "90% Storage Warning" : "মেমোরি ৯০% পূর্ণ হলে সতর্কতা", "storage_warn", true, false);
            contentArea.addView(c);
        } else if (t == 3) {
            LinearLayout c = card("#111827", "#1E293B"); hdr(c, isEn ? "🕌 Namaz & Custom Calendars" : "🕌 নামাজ ও ক্যালেন্ডার ফাইল");
            sw(c, isEn ? "5 Waqt Namaz Alarm" : "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", true, false);
            btn(c, isEn ? "Upload Ramadan (.txt)" : "রমজান ফাইল (.txt) যুক্ত করুন", "#8B5CF6", x -> pick(201));
            btn(c, isEn ? "Upload Bangla Cal (.txt)" : "বাংলা ক্যালেন্ডার ফাইল (.txt) দিন", "#0D9488", x -> pick(202));
            btn(c, isEn ? "Upload Arabic Cal (.txt)" : "আরবি হিজরি ফাইল (.txt) দিন", "#D97706", x -> pick(203));
            contentArea.addView(c);
        } else {
            LinearLayout c = card("#111827", "#1E293B"); hdr(c, isEn ? "⚙️ Clock, Voice & Permissions" : "⚙️ ঘড়ি, ভয়েস ও পারমিশন");
            sw(c, isEn ? "Sleep Mode (12AM-5AM)" : "রাতের স্লিপ মোড (১২টা-৫টা চুপ)", "sleep_mode", true, false);
            sw(c, isEn ? "7AM Morning Briefing" : "সকাল ৭টায় গুড মর্নিং ব্রিফিং", "morning_brief", true, false);
            LinearLayout r1 = new LinearLayout(this);
            pill(r1, "15m", "#2563EB", x -> prefs.edit().putInt("clock_interval", 15).apply());
            pill(r1, "30m", "#2563EB", x -> prefs.edit().putInt("clock_interval", 30).apply());
            pill(r1, "1h", "#2563EB", x -> prefs.edit().putInt("clock_interval", 60).apply());
            pill(r1, "Off", "#334155", x -> prefs.edit().putInt("clock_interval", 0).apply());
            c.addView(r1);
            LinearLayout r2 = new LinearLayout(this); r2.setPadding(0, 14, 0, 10);
            pill(r2, "বাংলা", "#3B82F6", x -> { prefs.edit().putString("voice_lang", "bn").apply(); SystemEventReceiver.speak(this, "বাংলা ভয়েস"); });
            pill(r2, "UK", "#3B82F6", x -> { prefs.edit().putString("voice_lang", "en_uk").apply(); SystemEventReceiver.speak(this, "UK English"); });
            pill(r2, "Girl", "#DB2777", x -> { prefs.edit().putBoolean("voice_male", false).apply(); SystemEventReceiver.speak(this, "মেয়ে কণ্ঠ"); });
            pill(r2, "Boy", "#475569", x -> { prefs.edit().putBoolean("voice_male", true).apply(); SystemEventReceiver.speak(this, "ছেলে কণ্ঠ"); });
            c.addView(r2);
            btn(c, isEn ? "1. Grant Phone/SMS Permissions" : "১. কল ও SMS পারমিশন দিন", "#F59E0B", x -> reqPerms());
                        btn(c, isEn ? "2. Notification Access" : "২. নোটিফিকেশন অ্যাক্সেস অন করুন", "#EC4899", x -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
            contentArea.addView(c);
        }
    }

    private void voiceAsk() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
        try { startActivityForResult(i, 303); } catch (Exception ignored) {}
    }
    private void pick(int c) { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, c); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null) return;
        if (req == 303) {
            ArrayList<String> m = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (m != null && !m.isEmpty()) {
                String q = m.get(0).toLowerCase();
                if (q.contains("আরবি") || q.contains("হিজরি")) SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this));
                else if (q.contains("বাংলা") || q.contains("তারিখ")) SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this));
                else if (q.contains("আবহাওয়া") || q.contains("weather")) SmartCalendarWeather.fetchAndSpeakWeather(this);
                else SystemEventReceiver.speak(this, SmartCalendarWeather.getEnglishAndDayInfo());
            }
        } else if (req >= 201 && req <= 203 && data.getData() != null) {
            String f = (req == 201) ? "ramadan.txt" : (req == 202 ? "bangla_cal.txt" : "arabic_cal.txt");
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                FileOutputStream out = new FileOutputStream(new File(getExternalFilesDir(null), f));
                int b; while ((b = in.read()) != -1) out.write(b);
                in.close(); out.close(); Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        }
    }

    private LinearLayout card(String bg, String st) {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(36, 30, 36, 30);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(bg)); g.setCornerRadius(32f); g.setStroke(2, Color.parseColor(st));
        l.setBackground(g); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 0, 0, 24); l.setLayoutParams(p); return l;
    }
    private void hdr(LinearLayout p, String t) { TextView v = new TextView(this); v.setText(t); v.setTextColor(Color.WHITE); v.setTextSize(15); v.setTypeface(null, Typeface.BOLD); v.setPadding(0, 0, 0, 14); p.addView(v); }
    private void sw(LinearLayout p, String t, String k, boolean d, boolean m) {
        Switch s = new Switch(this); s.setText(t); s.setTextColor(Color.parseColor("#E2E8F0")); s.setPadding(0, 12, 0, 12); s.setChecked(prefs.getBoolean(k, d));
        s.setOnCheckedChangeListener((b, c) -> { prefs.edit().putBoolean(k, c).apply(); if (m) { render(0); Intent i = new Intent(this, AssistantBackgroundService.class); if (c) startService(i); else stopService(i); } });
        p.addView(s);
    }
    private void pill(LinearLayout r, String t, String c, android.view.View.OnClickListener l) {
        Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(11); b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(30f); b.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 88, 1f); p.setMargins(5, 0, 5, 0); b.setLayoutParams(p); b.setOnClickListener(l); r.addView(b);
    }
    private EditText inp(String h) {
        EditText e = new EditText(this); e.setHint(h); e.setHintTextColor(Color.GRAY); e.setTextColor(Color.WHITE); e.setPadding(24, 18, 24, 18);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#070B14")); g.setCornerRadius(20f); g.setStroke(2, Color.parseColor("#334155")); e.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 6, 0, 10); e.setLayoutParams(p); return e;
    }
    private void btn(LinearLayout p, String t, String c, android.view.View.OnClickListener l) {
        Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(24f); b.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 105); lp.setMargins(0, 10, 0, 6); b.setLayoutParams(lp); b.setOnClickListener(l); p.addView(b);
    }
    private void reqPerms() {
        List<String> p = new ArrayList<>(); p.add(Manifest.permission.RECEIVE_SMS); p.add(Manifest.permission.READ_SMS); p.add(Manifest.permission.READ_PHONE_STATE); p.add(Manifest.permission.CALL_PHONE); p.add(Manifest.permission.READ_CONTACTS); p.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS);
        String[] a = new String[p.size()];
        for (int i = 0; i < p.size(); i++) a[i] = p.get(i);
        ActivityCompat.requestPermissions(this, a, 101);
    }
}
