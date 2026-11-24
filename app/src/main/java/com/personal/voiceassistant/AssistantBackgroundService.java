package com.personal.voiceassistant;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StatFs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AssistantBackgroundService extends Service {
    private Handler handler;
    private int lastSpokenBattery = -1;
    private boolean storageWarned = false;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
            if (!prefs.getBoolean("master_switch", false) || !prefs.getBoolean("battery_switch", true)) return;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

            if (level != lastSpokenBattery) {
                if (isCharging && (level == 20 || level == 50 || level == 80 || level == 100)) {
                    lastSpokenBattery = level;
                    int remMins = (100 - level);
                    SystemEventReceiver.speak(context, "ব্যাটারি " + level + " পার্সেন্ট হয়েছে। সম্পূর্ণ হতে আনুমানিক " + remMins + " মিনিট লাগবে");
                } else if (!isCharging && (level == 80 || level == 60 || level == 40 || level == 20)) {
                    lastSpokenBattery = level;
                    SystemEventReceiver.speak(context, "সতর্কতা, ফোনের চার্জ এখন " + level + " পার্সেন্ট আছে");
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        handler = new Handler(Looper.getMainLooper());
        startClockLoop();
    }

    private void startClockLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkTimeAndEvents();
                handler.postDelayed(this, 60000);
            }
        }, 10000);
    }

    private void checkTimeAndEvents() {
        SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("master_switch", false)) return;

        Calendar cal = Calendar.getInstance();
        int min = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String nowHHMM = String.format(Locale.US, "%02d:%02d", hour, min);

        // Sleep Mode Check (12 AM to 5 AM)
        boolean isSleepTime = prefs.getBoolean("sleep_mode", true) && (hour >= 0 && hour < 5);

        int interval = prefs.getInt("clock_interval", 30);
        if (!isSleepTime && interval > 0 && (min % interval == 0)) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            SystemEventReceiver.speak(this, "টাইম ইজ " + sdf.format(new Date()));
        }

        if (dayOfWeek == Calendar.FRIDAY && hour == 8 && min == 0) {
            SystemEventReceiver.speak(this, "আজ পবিত্র শুক্রবার");
        }

        // 5 Waqt Namaz Check
        if (prefs.getBoolean("namaz_alarm", true)) {
            if (nowHHMM.equals(prefs.getString("fajr_time", "05:00"))) SystemEventReceiver.speak(this, "ফজরের নামাজের সময় হয়েছে");
            if (nowHHMM.equals(prefs.getString("dhuhr_time", "13:00"))) SystemEventReceiver.speak(this, "জোহরের নামাজের সময় হয়েছে");
            if (nowHHMM.equals(prefs.getString("asr_time", "16:15"))) SystemEventReceiver.speak(this, "আসরের নামাজের সময় হয়েছে");
            if (nowHHMM.equals(prefs.getString("maghrib_time", "18:05"))) SystemEventReceiver.speak(this, "মাগরিবের নামাজের সময় হয়েছে");
            if (nowHHMM.equals(prefs.getString("isha_time", "19:30"))) SystemEventReceiver.speak(this, "এশার নামাজের সময় হয়েছে");
        }

        // Morning Briefing & Ramadan Calendar at 7:00 AM
        if (hour == 7 && min == 0) {
            if (prefs.getBoolean("morning_brief", true)) {
                String dateStr = new SimpleDateFormat("dd MMMM", Locale.US).format(new Date());
                SystemEventReceiver.speak(this, "আসসালামু আলাইকুম, শুভ সকাল। আজ " + dateStr);
            }
            readRamadanFile();
        }

        // Storage Health Check
        if (prefs.getBoolean("storage_warn", true) && !storageWarned) {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getBlockCountLong() * stat.getBlockSizeLong();
            long free = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            if (total > 0 && ((total - free) * 100 / total) >= 90) {
                storageWarned = true;
                SystemEventReceiver.speak(this, "সতর্কতা, ফোনের স্টোরেজ ৯০ পার্সেন্ট পূর্ণ হয়ে গেছে");
            }
        }
    }

    private void readRamadanFile() {
        try {
            File file = new File(getExternalFilesDir(null), "ramadan.txt");
            if (!file.exists()) return;
            String today = new SimpleDateFormat("dd-MM", Locale.US).format(new Date());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(today)) {
                    SystemEventReceiver.speak(this, line.substring(today.length() + 1).trim());
                    break;
                }
            }
            br.close();
        } catch (Exception ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
