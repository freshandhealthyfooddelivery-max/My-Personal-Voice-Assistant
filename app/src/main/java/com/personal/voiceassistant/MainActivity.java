package com.personal.voiceassistant;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.Gravity;
import android.view.View;
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
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        getWindow().setNavigationBarColor(Color.parseColor("#000000"));
        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        isEn = prefs.getBoolean("ui_english", false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#000000"));

        // Samsung One UI 7 Header
        LinearLayout top = new LinearLayout(this);
        top.setPadding(48, 56, 48, 28);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout tBox = new LinearLayout(this);
        tBox.setOrientation(LinearLayout.VERTICAL);
        tBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView t1 = new TextView(this);
        t1.setText("Galaxy AI");
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(30);
        t1.setTypeface(null, Typeface.BOLD);

        TextView t2 = new TextView(this);
        t2.setText("✦ Crafted by NAHIM  •  One UI Ultra");
        t2.setTextColor(Color.parseColor("#3E91FF"));
        t2.setTextSize(12);
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
        lg.setColor(Color.parseColor("#1C1C1E"));
        lg.setCornerRadius(50f);
        lg.setStroke(2, Color.parseColor("#2C2C2E"));
        lBtn.setBackground(lg);
        lBtn.setOnClickListener(v -> { isEn = !isEn; prefs.edit().putBoolean("ui_english", isEn).apply(); recreate(); });
        top.addView(lBtn);
        root.addView(top);

        ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setPadding(36, 8, 36, 32);
        sc.addView(contentArea);
        root.addView(sc);

        // Floating One UI Pill Dock
        LinearLayout dockWrap = new LinearLayout(this);
        dockWrap.setPadding(32, 10, 32, 24);
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(16, 12, 16, 12);
        GradientDrawable ng = new GradientDrawable();
        ng.setColor(Color.parseColor("#161618"));
        ng.setCornerRadius(60f);
        ng.setStroke(2, Color.parseColor("#2C2C2E"));
        nav.setBackground(ng);
        tab(nav, isEn ? "Home" : "হোম", 0);
        tab(nav, isEn ? "Comm" : "কল/মেসেজ", 1);
        tab(nav, isEn ? "Guard" : "নিরাপত্তা", 2);
        tab(nav, isEn ? "Islam" : "ক্যালেন্ডার", 3);
        tab(nav, isEn ? "System" : "সিস্টেম", 4);
        dockWrap.addView(nav);
        root.addView(dockWrap);

        setContentView(root);
        render(0);
        safeStartService();
    }

    private void safeStartService() {
        try {
            if (prefs.getBoolean("master_switch", false)) startService(new Intent(this, AssistantBackgroundService.class));
        } catch (Exception ignored) {}
    }

    private void tab(LinearLayout b, String l, int i) {
        Button bt = new Button(this);
        bt.setText(l);
        bt.setTextColor(Color.parseColor("#F2F2F7"));
        bt.setTextSize(11);
        bt.setTypeface(null, Typeface.BOLD);
        bt.setAllCaps(false);
        bt.setBackgroundColor(Color.TRANSPARENT);
        bt.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        bt.setOnClickListener(v -> render(i));
        b.addView(bt);
    }

    private void render(int t) {
        contentArea.removeAllViews();
        if (t == 0) {
            // Galaxy AI Orb Hero Card
            LinearLayout orbCard = card("#161618");
            orbCard.setGravity(Gravity.CENTER_HORIZONTAL);
            Button aiOrb = new Button(this);
            aiOrb.setText("✦\n" + (isEn ? "Tap to Ask AI" : "কথা বলতে চাপ দিন"));
            aiOrb.setTextColor(Color.WHITE);
            aiOrb.setTextSize(14);
            aiOrb.setTypeface(null, Typeface.BOLD);
            aiOrb.setAllCaps(false);
            GradientDrawable og = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor("#3E91FF"), Color.parseColor("#8A4FFF")});
            og.setShape(GradientDrawable.OVAL);
            aiOrb.setBackground(og);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(260, 260);
            op.setMargins(0, 12, 0, 20);
            aiOrb.setLayoutParams(op);
            ObjectAnimator pulse = ObjectAnimator.ofFloat(aiOrb, "scaleX", 0.96f, 1.04f);
            pulse.setDuration(1400); pulse.setRepeatCount(ValueAnimator.INFINITE); pulse.setRepeatMode(ValueAnimator.REVERSE); pulse.start();
            aiOrb.setOnClickListener(v -> { try { startActivity(new Intent(this, VoiceAssistActivity.class)); } catch (Exception ignored) {} });
            orbCard.addView(aiOrb);

            boolean on = prefs.getBoolean("master_switch", false);
            TextView st = new TextView(this);
            st.setText((on ? (isEn ? "● GALAXY AI ACTIVE" : "● গ্যালাক্সি এআই সচল") : (isEn ? "○ STANDBY MODE" : "○ বন্ধ আছে")) + "  •  ⚡ " + getBat());
            st.setTextColor(on ? Color.parseColor("#30D158") : Color.parseColor("#8E8E93"));
            st.setTextSize(12);
            st.setTypeface(null, Typeface.BOLD);
            orbCard.addView(st);
            sw(orbCard, isEn ? "Master AI Engine" : "মাস্টার এআই ইঞ্জিন (অন/অফ)", "master_switch", false, true);
            contentArea.addView(orbCard);

            // Quick Actions Card
            LinearLayout qCard = card("#161618");
            hdr(qCard, isEn ? "Instant Voice Briefings" : "তাৎক্ষণিক ভয়েস তথ্য");
            btn(qCard, isEn ? "📅 Bangla Date, Month & Day" : "📅 বাংলা তারিখ, মাস ও বার শুনুন", "#1C1C1E", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getBanglaDateInfo(this)));
            btn(qCard, isEn ? "🌙 Arabic Hijri Calendar" : "🌙 আরবি হিজরি তারিখ ও সন শুনুন", "#1C1C1E", x -> SystemEventReceiver.speak(this, SmartCalendarWeather.getHijriDateInfo(this)));
            btn(qCard, isEn ? "🌦️ Live Weather Forecast" : "🌦️ বর্তমান আবহাওয়ার বিস্তারিত শুনুন", "#1C1C1E", x -> SmartCalendarWeather.fetchAndSpeakWeather(this));
            btn(qCard, isEn ? "⚡ Set Power-Button Assistant" : "⚡ পাওয়ার বাটনে ভয়েস অ্যাসিস্ট্যান্ট সেট করুন", "#2563EB", x -> { try { startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)); } catch (Exception ignored) {} });
            contentArea.addView(qCard);
        } else if (t == 1) {
            LinearLayout c = card("#161618");
            hdr(c, isEn ? "Smart Calls & Messages" : "স্মার্ট কল ও মেসেজ সেটিংস");
            sw(c, isEn ? "SMS Sender Announcer" : "SMS প্রেরকের নাম বা নাম্বার বলা", "sms_switch", false, false);
            sw(c, isEn ? "Read Full SMS Content" : "সম্পূর্ণ SMS পড়ে শোনানো", "sms_full_read", false, false);
            sw(c, isEn ? "App Notifications (WA/FB)" : "সব অ্যাপের নোটিফিকেশন বলা", "app_notif_switch", false, false);
            sw(c, isEn ? "Headphone Privacy Guard" : "হেডফোন প্রাইভেসি মোড", "headphone_privacy", false, false);
            sw(c, isEn ? "Incoming Call Announcer" : "ইনকামিং কলের নাম বা নাম্বার বলা", "call_switch", false, false);
            sw(c, isEn ? "Bike Riding Auto-Speaker" : "বাইক রাইডিং মোড (অটো স্পিকার)", "riding_mode", false, false);
            sw(c, isEn ? "Auto Un-Silent (1 Hour)" : "সাইলেন্ট করলে ১ ঘণ্টা পর সাউন্ড অন", "auto_unsilent", false, false);
            EditText n = inp(isEn ? "Number (017...)" : "মোবাইল নাম্বার লিখুন");
            EditText s = inp(isEn ? "Redial Seconds (15)" : "কত সেকেন্ড পর কেটে আবার দেবে");
            c.addView(n); c.addView(s);
            btn(c, isEn ? "▶ Start Auto-Redial Loop" : "▶ অটো-কল লুপ শুরু করুন", "#30D158", x -> {
                String num = n.getText().toString().trim(), sec = s.getText().toString().trim();
                if (!num.isEmpty() && !sec.isEmpty()) {
                    prefs.edit().putBoolean("autocall_active", true).apply();
                    Intent i = new Intent(this, AssistantBackgroundService.class); i.setAction("START_AUTOCALL"); i.putExtra("number", num); i.putExtra("seconds", Integer.parseInt(sec)); startService(i);
                }
            });
            btn(c, isEn ? "⏹ Stop Auto-Redial" : "⏹ অটো-কল বন্ধ করুন", "#FF453A", x -> { prefs.edit().putBoolean("autocall_active", false).apply(); Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show(); });
            contentArea.addView(c);
        } else if (t == 2) {
            LinearLayout c = card("#161618");
            hdr(c, isEn ? "Device Guard & Sensors" : "ব্যাটারি, সেন্সর ও নিরাপত্তা");
                        sw(c, isEn ? "Charger & Battery % Voice" : "চার্জার ও ব্যাটারি পার্সেন্টেজ বলা", "battery_switch", false, false);
            sw(c, isEn ? "100% Full Charge Alarm" : "১০০% চার্জ পূর্ণ হলে সুরক্ষা অ্যালার্ম", "full_charge_alarm", false, false);
            sw(c, isEn ? "Anti-Theft Charger Siren" : "চার্জার চুরি সাইরেন", "theft_alarm", false, false);
            sw(c, isEn ? "Shake Phone for Flashlight" : "ফোন ঝাঁকালে ফ্ল্যাশলাইট অন/অফ", "shake_torch", false, false);
            sw(c, isEn ? "Wi-Fi & Data Drop Alert" : "ওয়াইফাই ও ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
            sw(c, isEn ? "90% Storage Full Warning" : "মেমোরি ৯০% পূর্ণ হলে সতর্কতা", "storage_warn", false, false);
            contentArea.addView(c);
        } else if (t == 3) {
            LinearLayout c = card("#161618");
            hdr(c, isEn ? "Namaz & Custom Calendars" : "নামাজ, রমজান ও ক্যালেন্ডার");
            sw(c, isEn ? "5 Waqt Namaz Voice Alarm" : "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", false, false);
            sw(c, isEn ? "Friday 8AM Special Alert" : "প্রতি শুক্রবার সকাল ৮টায় বিশেষ বার্তা", "friday_alert", false, false);
            btn(c, isEn ? "Upload Ramadan File (.txt)" : "রমজান ক্যালেন্ডার ফাইল (.txt) দিন", "#1C1C1E", x -> pick(201));
            btn(c, isEn ? "Upload Bangla Calendar (.txt)" : "বাংলা ক্যালেন্ডার ফাইল (.txt) দিন", "#1C1C1E", x -> pick(202));
            btn(c, isEn ? "Upload Arabic Hijri (.txt)" : "আরবি হিজরি ফাইল (.txt) দিন", "#1C1C1E", x -> pick(203));
            contentArea.addView(c);
        } else {
            LinearLayout c = card("#161618");
            hdr(c, isEn ? "System, Voice & Permissions" : "ঘড়ি, কণ্ঠ ও সিস্টেম পারমিশন");
            sw(c, isEn ? "Night Sleep Mode (12AM-5AM)" : "রাতের স্লিপ মোড (১২টা-৫টা নিরব)", "sleep_mode", false, false);
            sw(c, isEn ? "7AM Morning Briefing" : "সকাল ৭টায় গুড মর্নিং ব্রিফিং", "morning_brief", false, false);
            LinearLayout r1 = new LinearLayout(this);
            pill(r1, "15m", "#2C2C2E", x -> prefs.edit().putInt("clock_interval", 15).apply());
            pill(r1, "30m", "#2C2C2E", x -> prefs.edit().putInt("clock_interval", 30).apply());
            pill(r1, "1h", "#2C2C2E", x -> prefs.edit().putInt("clock_interval", 60).apply());
            pill(r1, "Off", "#1C1C1E", x -> prefs.edit().putInt("clock_interval", 0).apply());
            c.addView(r1);
            LinearLayout r2 = new LinearLayout(this); r2.setPadding(0, 14, 0, 14);
            pill(r2, "বাংলা", "#3E91FF", x -> { prefs.edit().putString("voice_lang", "bn").apply(); SystemEventReceiver.speak(this, "বাংলা কণ্ঠ"); });
            pill(r2, "UK", "#3E91FF", x -> { prefs.edit().putString("voice_lang", "en_uk").apply(); SystemEventReceiver.speak(this, "UK English"); });
            pill(r2, "Female", "#BF5AF2", x -> { prefs.edit().putBoolean("voice_male", false).apply(); SystemEventReceiver.speak(this, "মেয়ে কণ্ঠ"); });
            pill(r2, "Male", "#5E5CE6", x -> { prefs.edit().putBoolean("voice_male", true).apply(); SystemEventReceiver.speak(this, "ছেলে কণ্ঠ"); });
            c.addView(r2);
            btn(c, isEn ? "1. Grant All System Permissions" : "১. সব ফোনের পারমিশন দিন", "#FF9F0A", x -> reqPerms());
            btn(c, isEn ? "2. Enable Notification Access" : "২. নোটিফিকেশন অ্যাক্সেস অন করুন", "#FF375F", x -> { try { startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); } catch (Exception ignored) {} });
            contentArea.addView(c);
        }
    }

    private String getBat() { Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); return i != null ? i.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) + "%" : "100%"; }
    private void pick(int c) { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, c); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null && req >= 201 && req <= 203 && data.getData() != null) {
            String f = (req == 201) ? "ramadan.txt" : (req == 202 ? "bangla_cal.txt" : "arabic_cal.txt");
            try { InputStream in = getContentResolver().openInputStream(data.getData()); FileOutputStream out = new FileOutputStream(new File(getExternalFilesDir(null), f)); int b; while ((b = in.read()) != -1) out.write(b); in.close(); out.close(); Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
        }
    }

    private LinearLayout card(String bg) { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(40, 34, 40, 34); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(bg)); g.setCornerRadius(56f); l.setBackground(g); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 0, 0, 26); l.setLayoutParams(p); return l; }
    private void hdr(LinearLayout p, String t) { TextView v = new TextView(this); v.setText(t); v.setTextColor(Color.parseColor("#8E8E93")); v.setTextSize(13); v.setTypeface(null, Typeface.BOLD); v.setPadding(0, 0, 0, 16); p.addView(v); }
    private void sw(LinearLayout p, String t, String k, boolean d, boolean m) { Switch s = new Switch(this); s.setText(t); s.setTextColor(Color.WHITE); s.setTextSize(15); s.setPadding(0, 16, 0, 16); s.setChecked(prefs.getBoolean(k, d)); s.setOnCheckedChangeListener((b, c) -> { prefs.edit().putBoolean(k, c).apply(); if (m) { render(0); if (c) safeStartService(); else try { stopService(new Intent(this, AssistantBackgroundService.class)); } catch (Exception ignored) {} } }); p.addView(s); }
    private void pill(LinearLayout r, String t, String c, View.OnClickListener l) { Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(12); b.setAllCaps(false); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(45f); b.setBackground(g); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 96, 1f); p.setMargins(6, 0, 6, 0); b.setLayoutParams(p); b.setOnClickListener(l); r.addView(b); }
    private EditText inp(String h) { EditText e = new EditText(this); e.setHint(h); e.setHintTextColor(Color.parseColor("#636366")); e.setTextColor(Color.WHITE); e.setPadding(28, 22, 28, 22); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#1C1C1E")); g.setCornerRadius(32f); e.setBackground(g); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 8, 0, 12); e.setLayoutParams(p); return e; }
    private void btn(LinearLayout p, String t, String c, View.OnClickListener l) { Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(14); b.setAllCaps(false); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor(c)); g.setCornerRadius(36f); b.setBackground(g); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 115); lp.setMargins(0, 10, 0, 8); b.setLayoutParams(lp); b.setOnClickListener(l); p.addView(b); }
    private void reqPerms() {
        List<String> p = new ArrayList<>(); p.add(Manifest.permission.RECEIVE_SMS); p.add(Manifest.permission.READ_SMS); p.add(Manifest.permission.READ_PHONE_STATE); p.add(Manifest.permission.CALL_PHONE); p.add(Manifest.permission.ANSWER_PHONE_CALLS); p.add(Manifest.permission.READ_CONTACTS); p.add(Manifest.permission.RECORD_AUDIO); p.add(Manifest.permission.CAMERA);
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS);
        String[] a = new String[p.size()]; for (int i = 0; i < p.size(); i++) a[i] = p.get(i);
        ActivityCompat.requestPermissions(this, a, 101);
    }
}

