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
        getWindow().setStatusBarColor(Color.parseColor("#050811"));
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        isEn = prefs.getBoolean("ui_english", false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#050811"));

        // Flagship Header
        LinearLayout top = new LinearLayout(this);
        top.setPadding(42, 48, 42, 24);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout tBox = new LinearLayout(this);
        tBox.setOrientation(LinearLayout.VERTICAL);
        tBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView t1 = new TextView(this);
        t1.setText("AI Voice Pro Ultra");
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(24);
        t1.setTypeface(null, Typeface.BOLD);

        TextView t2 = new TextView(this);
        t2.setText("✦ Crafted by NAHIM  •  v3.0");
        t2.setTextColor(Color.parseColor("#38BDF8"));
        t2.setTextSize(11);
        t2.setTypeface(null, Typeface.BOLD);
        tBox.addView(t1);
        tBox.addView(t2);
        top.addView(tBox);

        Button lBtn = new Button(this);
        lBtn.setText(isEn ? "বাংলা" : "English");
        lBtn.setTextColor(Color.WHITE);
        lBtn.setTextSize(12);
        lBtn.setAllCaps(false);
        GradientDrawable lg = new GradientDrawable();
        lg.setColor(Color.parseColor("#111827"));
        lg.setCornerRadius(32f);
        lg.setStroke(2, Color.parseColor("#38BDF8"));
        lBtn.setBackground(lg);
        lBtn.setOnClickListener(v -> {
            isEn = !isEn;
            prefs.edit().putBoolean("ui_english", isEn).apply();
            recreate();
        });
        top.addView(lBtn);
        root.addView(top);

        ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(36, 10, 36, 30);
        sc.addView(contentArea);
        root.addView(sc);

        // Modern Bottom Navigation
        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.parseColor("#0B1120"));
        nav.setPadding(10, 14, 10, 20);
        tab(nav, isEn ? "Home" : "হোম", 0);
        tab(nav, isEn ? "Call/Msg" : "কল/মেসেজ", 1);
        tab(nav, isEn ? "Security" : "নিরাপত্তা", 2);
        tab(nav, isEn ? "Calendar" : "ক্যালেন্ডার", 3);
        tab(nav, isEn ? "System" : "সিস্টেম", 4);
        root.addView(nav);

        setContentView(root);
        render(0);
        if (prefs.getBoolean("master_switch", false)) {
            startForegroundService(new Intent(this, AssistantBackgroundService.class));
        }
    }

    private void tab(LinearLayout b, String l, int i) {
        Button bt = new Button(this);
        bt.setText(l);
        bt.setTextColor(Color.parseColor("#E2E8F0"));
        bt.setTextSize(11);
        bt.setAllCaps(false);
        bt.setBackgroundColor(Color.TRANSPARENT);
        bt.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        bt.setOnClickListener(v -> render(i));
        b.addView(bt);
    }
    private void render(int t) {
        contentArea.removeAllViews();
        if (t == 0) {
            LinearLayout h = card("#0F172A", "#38BDF8");
            boolean on = prefs.getBoolean("master_switch", false);
            TextView st = new TextView(this);
            st.setText((on ? (isEn ? "● ENGINE ACTIVE" : "● সিস্টেম সচল") : (isEn ? "○ STANDBY" : "○ বন্ধ আছে")) + "  |  ⚡ " + getBatInfo());
            st.setTextColor(on ? Color.parseColor("#10B981") : Color.parseColor("#94A3B8"));
            st.setTextSize(12);
            st.setTypeface(null, Typeface.BOLD);
            h.addView(st);
            sw(h, isEn ? "Master AI Power Switch" : "মাস্টার এআই পাওয়ার সুইচ (অন/অফ)", "master_switch", false, true);
            contentArea.addView(h);

            LinearLayout v = card("#0F172A", "#1E293B");
            hdr(v, isEn ? "🎙️ Instant Voice & Lock-Screen AI" : "🎙️ তাৎক্ষণিক ভয়েস ও লাইভ তথ্য");
            btn(v, isEn ? "🎤 Ask Anything by Voice" : "🎤 মুখে জিজ্ঞেস করুন (তারিখ / আবহাওয়া / টর্চ)", "#2563EB", x -> startActivity(new Intent(this, VoiceAssistActivity.class)));
            btn(v, isEn ? "📅 Speak Bangla Date & Day" : "📅 বাংলা তারিখ, মাস ও বার শুনুন", "#0D9488", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this)));
            btn(v, isEn ? "🌙 Speak Arabic Hijri Date" : "🌙 আরবি হিজরি তারিখ ও সন শুনুন", "#7C3AED", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this)));
            btn(v, isEn ? "🌦️ Live Balaganj / Local Weather" : "🌦️ বর্তমান আবহাওয়ার বিস্তারিত শুনুন", "#0284C7", x -> SmartCalendarWeather.fetchAndSpeakWeather(this));
            btn(v, isEn ? "⚡ Set as Default Lock-Screen Assistant" : "⚡ পাওয়ার বাটনে ভয়েস অ্যাসিস্ট্যান্ট সেট করুন", "#4F46E5", x -> startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)));
            contentArea.addView(v);
        } else if (t == 1) {
            LinearLayout c = card("#0F172A", "#1E293B");
            hdr(c, isEn ? "💬 Smart Messages & Notifications" : "💬 মেসেজ ও নোটিফিকেশন কন্ট্রোল");
            sw(c, isEn ? "SMS Sender Announcer" : "SMS প্রেরকের নাম/নাম্বার বলা", "sms_switch", false, false);
            sw(c, isEn ? "Read Full SMS Body" : "সম্পূর্ণ SMS পড়ে শোনানো", "sms_full_read", false, false);
            sw(c, isEn ? "All App Notifications (WA/FB)" : "সব অ্যাপের নোটিফিকেশন বলা", "app_notif_switch", false, false);
            sw(c, isEn ? "Headphone-Only Privacy Mode" : "হেডফোন প্রাইভেসি মোড (শুধু ইয়ারফোনে বলবে)", "headphone_privacy", false, false);
            contentArea.addView(c);

            LinearLayout c2 = card("#0F172A", "#1E293B");
            hdr(c2, isEn ? "📞 Smart Call & Auto-Redial Loop" : "📞 কল অ্যানাউন্সার ও অটো-কল লুপ");
            sw(c2, isEn ? "Incoming Call Name/Number" : "ইনকামিং কলের নাম বা নাম্বার বলা", "call_switch", false, false);
            sw(c2, isEn ? "Bike Riding Auto-Speaker" : "বাইক রাইডিং মোড (কল ধরলেই স্পিকার)", "riding_mode", false, false);
            sw(c2, isEn ? "Auto Un-Silent after 1 Hour" : "সাইলেন্ট করলে ১ ঘণ্টা পর সাউন্ড অন", "auto_unsilent", false, false);

            EditText n = inp(isEn ? "Target Number (017...)" : "মোবাইল নাম্বার লিখুন (017...)");
            EditText s = inp(isEn ? "Redial Interval Seconds (15)" : "কত সেকেন্ড পর কেটে আবার দেবে (15)");
            c2.addView(n); c2.addView(s);

            btn(c2, isEn ? "▶ Start Continuous Auto-Call" : "▶ অটো-কল লুপ শুরু করুন", "#10B981", x -> {
                String num = n.getText().toString().trim(), sec = s.getText().toString().trim();
                if (!num.isEmpty() && !sec.isEmpty()) {
                    prefs.edit().putBoolean("autocall_active", true).apply();
                    Intent i = new Intent(this, AssistantBackgroundService.class);
                    i.setAction("START_AUTOCALL");
                    i.putExtra("number", num);
                    i.putExtra("seconds", Integer.parseInt(sec));
                    startForegroundService(i);
                }
            });
            btn(c2, isEn ? "⏹ Stop Auto-Call" : "⏹ অটো-কল বন্ধ করুন", "#EF4444", x -> {
                prefs.edit().putBoolean("autocall_active", false).apply();
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
            });
            contentArea.addView(c2);
        } else if (t == 2) {
            LinearLayout c = card("#0F172A", "#1E293B");
            hdr(c, isEn ? "🛡️ Power, Sensors & Anti-Theft" : "🛡️ ব্যাটারি, সেন্সর ও অ্যান্টি-থেফট");
            sw(c, isEn ? "Charger & Battery % Alerts" : "চার্জার ও ব্যাটারি পার্সেন্টেজ বলা", "battery_switch", false, false);
            sw(c, isEn ? "100% Full Charge Protection Alarm" : "১০০% চার্জ পূর্ণ হলে সুরক্ষা অ্যালার্ম", "full_charge_alarm", false, false);
            sw(c, isEn ? "Anti-Theft Charger Siren" : "চার্জার চুরি সাইরেন (খুললেই বাজবে)", "theft_alarm", false, false);
            sw(c, isEn ? "Shake Phone for Flashlight" : "ফোন ঝাঁকালে ফ্ল্যাশলাইট অন/অফ", "shake_torch", false, false);
            sw(c, isEn ? "Wi-Fi & Data Drop Alert" : "ওয়াইফাই ও ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
            sw(c, isEn ? "90% Storage Full Warning" : "মেমোরি ৯০% পূর্ণ হলে সতর্কতা", "storage_warn", false, false);
            contentArea.addView(c);
        } else if (t == 3) {
            LinearLayout c = card("#0F172A", "#1E293B");
            hdr(c, isEn ? "🕌 Namaz, Friday & Custom Calendars" : "🕌 নামাজ, শুক্রবার ও ক্যালেন্ডার ফাইল");
            sw(c, isEn ? "5 Waqt Namaz Voice Alarm" : "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", false, false);
            sw(c, isEn ? "Friday 8AM Special Alert" : "প্রতি শুক্রবার সকাল ৮টায় বিশেষ বার্তা", "friday_alert", false, false);
            btn(c, isEn ? "Upload Ramadan File (.txt)" : "রমজান ক্যালেন্ডার ফাইল (.txt) দিন", "#8B5CF6", x -> pick(201));
            btn(c, isEn ? "Upload Bangla Calendar (.txt)" : "বাংলা ক্যালেন্ডার ফাইল (.txt) দিন", "#0D9488", x -> pick(202));
            btn(c, isEn ? "Upload Arabic Hijri File (.txt)" : "আরবি হিজরি ফাইল (.txt) দিন", "#D97706", x -> pick(203));
            contentArea.addView(c);
        } else {
            LinearLayout c = card("#0F172A", "#1E293B");
            hdr(c, isEn ? "⚙️ Clock, Neural Voice & Permissions" : "⚙️ ঘড়ি, ভয়েস ও সিস্টেম পারমিশন");
            sw(c, isEn ? "Night Sleep Mode (12AM - 5AM)" : "রাতের স্লিপ মোড (১২টা - ৫টা নিরব)", "sleep_mode", false, false);
            sw(c, isEn ? "7AM Morning Briefing" : "সকাল ৭টায় গুড মর্নিং ব্রিফিং", "morning_brief", false, false);
            LinearLayout r1 = new LinearLayout(this);
            pill(r1, "15m", "#2563EB", x -> prefs.edit().putInt("clock_interval", 15).apply());
            pill(r1, "30m", "#2563EB", x -> prefs.edit().putInt("clock_interval", 30).apply());
            pill(r1, "1h", "#2563EB", x -> prefs.edit().putInt("clock_interval", 60).apply());
            pill(r1, "Off", "#334155", x -> prefs.edit().putInt("clock_interval", 0).apply());
            c.addView(r1);
            LinearLayout r2 = new LinearLayout(this); r2.setPadding(0, 14, 0, 10);
            pill(r2, "বাংলা", "#3B82F6", x -> { prefs.edit().putString("voice_lang", "bn").apply(); SystemEventReceiver.speak(this, "বাংলা ভয়েস নির্বাচিত হয়েছে"); });
            pill(r2, "UK", "#3B82F6", x -> { prefs.edit().putString("voice_lang", "en_uk").apply(); SystemEventReceiver.speak(this, "UK English selected"); });
            pill(r2, "Female", "#DB2777", x -> { prefs.edit().putBoolean("voice_male", false).apply(); SystemEventReceiver.speak(this, "মেয়ে কণ্ঠ সেট হয়েছে"); });
            pill(r2, "Male", "#475569", x -> { prefs.edit().putBoolean("voice_male", true).apply(); SystemEventReceiver.speak(this, "ছেলে কণ্ঠ সেট হয়েছে"); });
            c.addView(r2);
            btn(c, isEn ? "1. Grant All Phone & Sensor Permissions" : "১. সব ফোনের পারমিশন দিন", "#F59E0B", x -> reqPerms());
            btn(c, isEn ? "2. Enable Notification Access" : "২. নোটিফিকেশন অ্যাক্সেস অন করুন", "#EC4899", x -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
            contentArea.addView(c);
        }
    }

    private String getBatInfo() {
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return "100%";
        int lvl = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
        float temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) / 10f;
        return lvl + "% (" + temp + "°C)";
    }

    private void pick(int c) { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, c); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null && req >= 201 && req <= 203 && data.getData() != null) {
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
