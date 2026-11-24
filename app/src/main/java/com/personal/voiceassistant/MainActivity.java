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
import android.speech.RecognizerIntent;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private LinearLayout contentArea;
    private boolean isEn = false;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#070B14"));
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        isEn = prefs.getBoolean("ui_english", false);

        LinearLayout mainRoot = new LinearLayout(this);
        mainRoot.setOrientation(LinearLayout.VERTICAL);
        mainRoot.setBackgroundColor(Color.parseColor("#070B14"));

        // Top Premium Header
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(40, 45, 40, 25);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView appTitle = new TextView(this);
        appTitle.setText("AI Voice Pro");
        appTitle.setTextColor(Color.WHITE);
        appTitle.setTextSize(23);
        appTitle.setTypeface(null, Typeface.BOLD);
        titleBox.addView(appTitle);

        TextView devTag = new TextView(this);
        devTag.setText("✦ Crafted by NAHIM");
        devTag.setTextColor(Color.parseColor("#FBBF24"));
        devTag.setTextSize(11);
        devTag.setTypeface(null, Typeface.BOLD);
        titleBox.addView(devTag);
        topBar.addView(titleBox);

        Button langToggle = new Button(this);
        langToggle.setText(isEn ? "বাংলা" : "English");
        langToggle.setTextColor(Color.WHITE);
        langToggle.setTextSize(12);
        langToggle.setAllCaps(false);
        GradientDrawable lg = new GradientDrawable();
        lg.setColor(Color.parseColor("#1E293B"));
        lg.setCornerRadius(30f);
        lg.setStroke(2, Color.parseColor("#38BDF8"));
        langToggle.setBackground(lg);
        langToggle.setOnClickListener(v -> {
            isEn = !isEn;
            prefs.edit().putBoolean("ui_english", isEn).apply();
            recreate();
        });
        topBar.addView(langToggle);
        mainRoot.addView(topBar);

        // Scrollable Content Area
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(36, 10, 36, 30);
        scroll.addView(contentArea);
        mainRoot.addView(scroll);

        // Bottom Navigation Bar
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setBackgroundColor(Color.parseColor("#0F172A"));
        navBar.setPadding(12, 16, 12, 20);
        addNavTab(navBar, isEn ? "Home" : "হোম", 0);
        addNavTab(navBar, isEn ? "Call/Msg" : "কল/মেসেজ", 1);
        addNavTab(navBar, isEn ? "Power/Sec" : "নিরাপত্তা", 2);
        addNavTab(navBar, isEn ? "Calendar" : "ক্যালেন্ডার", 3);
        addNavTab(navBar, isEn ? "Voice/Sys" : "সেটিংস", 4);
        mainRoot.addView(navBar);

        setContentView(mainRoot);
        renderTab(0);
        if (prefs.getBoolean("master_switch", false)) startService(new Intent(this, AssistantBackgroundService.class));
    }

    private void addNavTab(LinearLayout bar, String label, int idx) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        b.setOnClickListener(v -> { currentTab = idx; renderTab(idx); });
        bar.addView(b);
    }
    private void renderTab(int tab) {
        contentArea.removeAllViews();
        if (tab == 0) {
            LinearLayout hero = card("#111827", "#38BDF8");
            boolean on = prefs.getBoolean("master_switch", false);
            TextView st = new TextView(this);
            st.setText(on ? (isEn ? "● SYSTEM ACTIVE" : "● সিস্টেম চালু আছে") : (isEn ? "○ STANDBY MODE" : "○ সিস্টেম বন্ধ আছে"));
            st.setTextColor(on ? Color.parseColor("#10B981") : Color.parseColor("#94A3B8"));
            st.setTypeface(null, Typeface.BOLD);
            hero.addView(st);
            sw(hero, isEn ? "Master Power Switch" : "মাস্টার পাওয়ার সুইচ (সব অন/অফ)", "master_switch", false, true);
            contentArea.addView(hero);

            LinearLayout vc = card("#111827", "#1E293B");
            hdr(vc, isEn ? "🎙️ Live Voice Ask & Instant Info" : "🎙️ ভয়েস কমান্ড ও তাৎক্ষণিক তথ্য");
            btn(vc, isEn ? "🎤 Ask by Voice (Date / Weather)" : "🎤 মুখে জিজ্ঞেস করুন (তারিখ / আবহাওয়া)", "#2563EB", v -> startVoiceAsk());
            btn(vc, isEn ? "📅 Speak Bangla Date & Day" : "📅 বাংলা তারিখ, মাস ও বার শুনুন", "#0D9488", v -> SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this)));
            btn(vc, isEn ? "🌙 Speak Arabic Hijri Date" : "🌙 আরবি হিজরি তারিখ ও সন শুনুন", "#7C3AED", v -> SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this)));
            btn(vc, isEn ? "🌦️ Speak Live Weather Report" : "🌦️ বর্তমান আবহাওয়ার বিস্তারিত শুনুন", "#0284C7", v -> SmartCalendarWeather.fetchAndSpeakWeather(this));
            contentArea.addView(vc);
        } else if (tab == 1) {
            LinearLayout c1 = card("#111827", "#1E293B");
            hdr(c1, isEn ? "💬 SMS & App Notifications" : "💬 SMS ও অ্যাপ নোটিফিকেশন সেটিংস");
            sw(c1, isEn ? "SMS Voice Announcer" : "SMS আসলে ভয়েসে বলবে", "sms_switch", true, false);
            sw(c1, isEn ? "Read Full SMS Content" : "সম্পূর্ণ SMS পড়ে শোনানো", "sms_full_read", false, false);
            sw(c1, isEn ? "All App Notifications (FB/WA)" : "সব অ্যাপের নোটিফিকেশন বলা", "app_notif_switch", true, false);
            sw(c1, isEn ? "Headphone Privacy Mode" : "হেডফোন প্রাইভেসি মোড", "headphone_privacy", false, false);
            contentArea.addView(c1);

            LinearLayout c2 = card("#111827", "#1E293B");
            hdr(c2, isEn ? "📞 Smart Call & Auto-Dialer" : "📞 কল অ্যানাউন্সার ও অটো-কল");
            sw(c2, isEn ? "Incoming Call Name/Number" : "ইনকামিং কলের নাম বা নাম্বার বলা", "call_switch", true, false);
            sw(c2, isEn ? "Bike Riding Auto-Speaker" : "বাইক রাইডিং মোড (অটো স্পিকার)", "riding_mode", false, false);
            sw(c2, isEn ? "Auto Un-Silent after 1 Hour" : "সাইলেন্ট করলে ১ ঘণ্টা পর সাউন্ড অন", "auto_unsilent", false, false);
            EditText nIn = inp(isEn ? "Phone Number (017...)" : "মোবাইল নাম্বার লিখুন");
            EditText sIn = inp(isEn ? "Redial Seconds (10)" : "কত সেকেন্ড পর কাটবে");
            c2.addView(nIn); c2.addView(sIn);
            btn(c2, isEn ? "Start Auto-Call" : "অটো-কল শুরু করুন", "#10B981", v -> {
                String n = nIn.getText().toString().trim(), s = sIn.getText().toString().trim();
                if (!n.isEmpty() && !s.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + n)));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(this, s + "s done", Toast.LENGTH_SHORT).show(), Integer.parseInt(s) * 1000L);
                }
            });
            contentArea.addView(c2);
        } else if (tab == 2) {
            LinearLayout c3 = card("#111827", "#1E293B");
            hdr(c3, isEn ? "🛡️ Battery, Network & Anti-Theft" : "🛡️ ব্যাটারি, নেটওয়ার্ক ও নিরাপত্তা");
            sw(c3, isEn ? "Charger & Battery % Voice" : "চার্জার ও ব্যাটারি পার্সেন্টেজ বলা", "battery_switch", true, false);
            sw(c3, isEn ? "Anti-Theft Charger Alarm" : "চার্জার চুরি অ্যালার্ম (খুললেই বাজবে)", "theft_alarm", false, false);
            sw(c3, isEn ? "Wi-Fi & Data Drop Alert" : "ওয়াইফাই ও ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
            sw(c3, isEn ? "90% Storage Full Warning" : "মেমোরি ৯০% পূর্ণ হলে সতর্কতা", "storage_warn", true, false);
            contentArea.addView(c3);
        } else if (tab == 3) {
            LinearLayout c4 = card("#111827", "#1E293B");
            hdr(c4, isEn ? "🕌 Namaz, Ramadan & Custom Calendars" : "🕌 নামাজ, রমজান ও ক্যালেন্ডার ফাইল");
            sw(c4, isEn ? "5 Waqt Namaz Voice Alarm" : "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", true, false);
            btn(c4, isEn ? "Upload Ramadan File (.txt)" : "রমজান ক্যালেন্ডার ফাইল (.txt) দিন", "#8B5CF6", v -> pickFile(201));
            btn(c4, isEn ? "Upload Bangla Calendar (.txt)" : "বাংলা ক্যালেন্ডার ফাইল (.txt) দিন", "#0D9488", v -> pickFile(202));
            btn(c4, isEn ? "Upload Arabic Hijri File (.txt)" : "আরবি হিজরি ফাইল (.txt) দিন", "#D97706", v -> pickFile(203));
            contentArea.addView(c4);
        } else if (tab == 4) {
            LinearLayout c5 = card("#111827", "#1E293B");
            hdr(c5, isEn ? "⚙️ Clock, Voice & Permissions" : "⚙️ ঘড়ি, ভয়েস ও পারমিশন");
            sw(c5, isEn ? "Night Sleep Mode (12AM - 5AM)" : "রাতের স্লিপ মোড (১২টা - ৫টা চুপ)", "sleep_mode", true, false);
            sw(c5, isEn ? "7AM Morning Briefing" : "সকাল ৭টায় গুড মর্নিং ব্রিফিং", "morning_brief", true, false);
            LinearLayout r1 = new LinearLayout(this);
            pill(r1, "15m", "#2563EB", v -> setClock(15)); pill(r1, "30m", "#2563EB", v -> setClock(30));
            pill(r1, "1h", "#2563EB", v -> setClock(60)); pill(r1, "Off", "#334155", v -> setClock(0));
            c5.addView(r1);
            LinearLayout r2 = new LinearLayout(this); r2.setPadding(0, 14, 0, 10);
            pill(r2, "বাংলা", "#3B82F6", v -> setLang("bn")); pill(r2, "UK", "#3B82F6", v -> setLang("en_uk"));
            pill(r2, "Girl", "#DB2777", v -> setMale(false)); pill(r2, "Boy", "#475569", v -> setMale(true));
            c5.addView(r2);
            btn(c5, isEn ? "1. Grant Phone & SMS Permissions" : "১. কল ও SMS পারমিশন দিন", "#F59E0B", v -> reqPerms());
            btn(c5, isEn ? "2. Enable Notification Access" : "২. নোটিফিকেশন অ্যাক্সেস অন করুন", "#EC4899", v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
            contentArea.addView(c5);
        }
    }

    private void startVoiceAsk() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
        try { startActivityForResult(i, 303); } catch (Exception e) { Toast.makeText(this, "Voice search unavailable", Toast.LENGTH_SHORT).show(); }
    }

    private void pickFile(int code) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, code);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null) return;
        if (req == 303) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String q = matches.get(0).toLowerCase();
                if (q.contains("আরবি") || q.contains("হিজরি") || q.contains("arabic")) SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this));
                else if (q.contains("বাংলা") || q.contains("তারিখ") || q.contains("date")) SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this));
                else if (q.contains("আবহাওয়া") || q.contains("weather")) SmartCalendarWeather.fetchAndSpeakWeather(this);
                else SystemEventReceiver.speak(this, SmartCalendarWeather.getEnglishAndDayInfo());
            }
        } else if (req >= 201 && req <= 203 && data.getData() != null) {
            String fname = (req == 201) ? "ramadan.txt" : (req == 202 ? "bangla_cal.txt" : "arabic_cal.txt");
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                FileOutputStream out = new FileOutputStream(new java.io.File(getExternalFilesDir(null), fname));
                byte[] buf = new byte; int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close(); out.close();
                Toast.makeText(this, "ফাইল যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show();
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
        s.setOnCheckedChangeListener((b, c) -> { prefs.edit().putBoolean(k, c).apply(); if (m) { renderTab(0); Intent i = new Intent(this, AssistantBackgroundService.class); if (c) startService(i); else stopService(i); } });
        p.addView(s);
                }
