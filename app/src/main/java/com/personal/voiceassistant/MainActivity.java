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
