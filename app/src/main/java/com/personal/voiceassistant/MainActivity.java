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
import android.view.Window;
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
    private TextView statusBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#0B0F19"));

        prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0B0F19"));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(42, 60, 42, 70);

        // --- TOP HEADER ---
        TextView subtitle = new TextView(this);
        subtitle.setText("ULTIMATE PERSONAL AI SYSTEM");
        subtitle.setTextColor(Color.parseColor("#38BDF8"));
        subtitle.setTextSize(11);
        subtitle.setTypeface(null, Typeface.BOLD);
        subtitle.setLetterSpacing(0.15f);
        root.addView(subtitle);

        TextView title = new TextView(this);
        title.setText("Voice Assistant Pro");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 4, 0, 32);
        root.addView(title);

        // --- HERO MASTER CARD ---
        LinearLayout heroCard = createCard("#1E293B", "#38BDF8");
        LinearLayout heroTop = new LinearLayout(this);
        heroTop.setOrientation(LinearLayout.HORIZONTAL);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);

        statusBadge = new TextView(this);
        boolean isMasterOn = prefs.getBoolean("master_switch", false);
        updateStatusBadge(isMasterOn);
        heroTop.addView(statusBadge);

        TextView batInfo = new TextView(this);
        batInfo.setText("   ⚡ Battery: " + getBatteryPercentage() + "%");
        batInfo.setTextColor(Color.parseColor("#94A3B8"));
        batInfo.setTextSize(13);
        heroTop.addView(batInfo);
        heroCard.addView(heroTop);

        addStyledSwitch(heroCard, "মাস্টার পাওয়ার সুইচ (সিস্টেম অন/অফ)", "master_switch", false, true);
        root.addView(heroCard);

        // --- CARD 1: SMS, APP NOTIFICATIONS & PRIVACY ---
        LinearLayout commCard = createCard("#131A2A", "#1E293B");
        addSectionHeader(commCard, "💬  মেসেজ, নোটিফিকেশন ও প্রাইভেসি");
        addStyledSwitch(commCard, "SMS ভয়েস অ্যানাউন্সার", "sms_switch", true, false);
        addStyledSwitch(commCard, "সম্পূর্ণ SMS পড়ে শোনানো (অফ থাকলে শুধু নাম বলবে)", "sms_full_read", false, false);
        addStyledSwitch(commCard, "সব অ্যাপের নোটিফিকেশন পড়ে শোনানো (FB/WA)", "app_notif_switch", true, false);
        addStyledSwitch(commCard, "হেডফোন প্রাইভেসি মোড (শুধু ইয়ারফোনে বলবে)", "headphone_privacy", false, false);
        root.addView(commCard);

        // --- CARD 2: SMART CALL & RIDING MODE ---
        LinearLayout callCard = createCard("#131A2A", "#1E293B");
        addSectionHeader(callCard, "📞  স্মার্ট কল ও বাইক রাইডিং মোড");
        addStyledSwitch(callCard, "ইনকামিং কলের নাম/নাম্বার বলা", "call_switch", true, false);
        addStyledSwitch(callCard, "হ্যান্ডস-ফ্রি রাইডিং মোড (কল ধরলেই অটো স্পিকার)", "riding_mode", false, false);
        addStyledSwitch(callCard, "সাইলেন্ট করলে ১ ঘণ্টা পর অটো সাউন্ড অন", "auto_unsilent", false, false);

        EditText numInput = createStyledInput("অটো-কল মোবাইল নাম্বার (যেমন: 017...)");
        callCard.addView(numInput);

        EditText secInput = createStyledInput("কত সেকেন্ড পর কেটে আবার দেবে (যেমন: 10)");
        callCard.addView(secInput);

        Button startCallBtn = createActionBtn("অটো-কল শুরু করুন", "#10B981");
        startCallBtn.setOnClickListener(v -> {
            String num = numInput.getText().toString().trim();
            String secStr = secInput.getText().toString().trim();
            if (!num.isEmpty() && !secStr.isEmpty()) {
                startAutoCallLoop(num, Integer.parseInt(secStr));
            } else {
                Toast.makeText(this, "নাম্বার ও সেকেন্ড দিন", Toast.LENGTH_SHORT).show();
            }
        });
        callCard.addView(startCallBtn);
        root.addView(callCard);

        // --- CARD 3: BATTERY, NETWORK & SECURITY ---
        LinearLayout batCard = createCard("#131A2A", "#1E293B");
        addSectionHeader(batCard, "🛡️  পাওয়ার, নেটওয়ার্ক ও নিরাপত্তা");
        addStyledSwitch(batCard, "চার্জার ও ব্যাটারি পার্সেন্টেজ অ্যালার্ট", "battery_switch", true, false);
        addStyledSwitch(batCard, "চার্জার চুরি অ্যালার্ম (খুললেই ফুল ভলিউমে বাজবে)", "theft_alarm", false, false);
        addStyledSwitch(batCard, "Wi-Fi ও মোবাইল ডাটা সংযোগ অ্যালার্ট", "network_alert", false, false);
        addStyledSwitch(batCard, "মেমোরি ৯০% পূর্ণ হলে স্টোরেজ ওয়ার্নিং", "storage_warn", true, false);
        root.addView(batCard);

        // --- CARD 4: SPEAKING CLOCK, VOICE & SLEEP MODE ---
        LinearLayout timeCard = createCard("#131A2A", "#1E293B");
        addSectionHeader(timeCard, "⏰  স্পিকিং ক্লক, স্লিপ মোড ও ভয়েস");
        addStyledSwitch(timeCard, "রাতের স্লিপ মোড (রাত ১২টা - ভোর ৫টা চুপ থাকবে)", "sleep_mode", true, false);
        addStyledSwitch(timeCard, "সকাল ৭টায় গুড মর্নিং ও তারিখ ব্রিফিং", "morning_brief", true, false);

        TextView cSub = new TextView(this);
        cSub.setText("কতক্ষণ পরপর সময় বলবে:");
        cSub.setTextColor(Color.parseColor("#94A3B8"));
        cSub.setTextSize(13);
        cSub.setPadding(0, 10, 0, 14);
        timeCard.addView(cSub);

        LinearLayout clockRow = new LinearLayout(this);
        clockRow.setOrientation(LinearLayout.HORIZONTAL);
        addPillBtn(clockRow, "১৫ মি.", 15, "#2563EB");
        addPillBtn(clockRow, "৩০ মি.", 30, "#2563EB");
        addPillBtn(clockRow, "১ ঘণ্টা", 60, "#2563EB");
        addPillBtn(clockRow, "বন্ধ", 0, "#334155");
        timeCard.addView(clockRow);

        TextView lSub = new TextView(this);
        lSub.setText("ভয়েস ভাষা ও কণ্ঠ নির্বাচন করুন:");
        lSub.setTextColor(Color.parseColor("#94A3B8"));
        lSub.setTextSize(13);
        lSub.setPadding(0, 24, 0, 14);
        timeCard.addView(lSub);

        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        addLangPillBtn(langRow, "বাংলা", "bn");
        addLangPillBtn(langRow, "UK Eng", "en_uk");
        addLangPillBtn(langRow, "US Eng", "en_us");
        timeCard.addView(langRow);

        LinearLayout genderRow = new LinearLayout(this);
        genderRow.setOrientation(LinearLayout.HORIZONTAL);
        genderRow.setPadding(0, 14, 0, 0);
        addGenderBtn(genderRow, "মেয়ে কণ্ঠ (Girl)", false);
        addGenderBtn(genderRow, "ছেলে কণ্ঠ (Boy)", true);
        timeCard.addView(genderRow);
        root.addView(timeCard);

        // --- CARD 5: NAMAZ & RAMADAN CALENDAR ---
        LinearLayout islamCard = createCard("#131A2A", "#1E293B");
        addSectionHeader(islamCard, "🕌  ৫ ওয়াক্ত নামাজ ও রমজান ক্যালেন্ডার");
        addStyledSwitch(islamCard, "৫ ওয়াক্ত নামাজের ভয়েস অ্যালার্ম", "namaz_alarm", true, false);

        EditText fajrIn = createStyledInput("ফজর (যেমন: 05:00)");
        fajrIn.setText(prefs.getString("fajr_time", "05:00"));
        islamCard.addView(fajrIn);

        EditText dhuhrIn = createStyledInput("জোহর (যেমন: 13:00)");
        dhuhrIn.setText(prefs.getString("dhuhr_time", "13:00"));
        islamCard.addView(dhuhrIn);

        EditText asrIn = createStyledInput("আসর (যেমন: 16:15)");
        asrIn.setText(prefs.getString("asr_time", "16:15"));
        islamCard.addView(asrIn);

        EditText maghribIn = createStyledInput("মাগরিব (যেমন: 18:05)");
        maghribIn.setText(prefs.getString("maghrib_time", "18:05"));
        islamCard.addView(maghribIn);

        EditText ishaIn = createStyledInput("এশা (যেমন: 19:30)");
        ishaIn.setText(prefs.getString("isha_time", "19:30"));
        islamCard.addView(ishaIn);

        Button saveNamazBtn = createActionBtn("নামাজের সময় সেভ করুন", "#0EA5E9");
        saveNamazBtn.setOnClickListener(v -> {
            prefs.edit()
                .putString("fajr_time", fajrIn.getText().toString().trim())
                .putString("dhuhr_time", dhuhrIn.getText().toString().trim())
                .putString("asr_time", asrIn.getText().toString().trim())
                .putString("maghrib_time", maghribIn.getText().toString().trim())
                .putString("isha_time", ishaIn.getText().toString().trim())
                .apply();
            Toast.makeText(this, "৫ ওয়াক্ত নামাজের সময় সেভ হয়েছে!", Toast.LENGTH_SHORT).show();
        });
        islamCard.addView(saveNamazBtn);

        Button ramadanBtn = createActionBtn("রমজান ক্যালেন্ডার ফাইল (.txt) যুক্ত করুন", "#8B5CF6");
        ramadanBtn.setOnClickListener(v -> {
