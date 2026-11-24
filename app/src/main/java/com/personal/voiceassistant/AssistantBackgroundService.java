package com.personal.voiceassistant;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.hardware.camera2.CameraManager;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.telecom.TelecomManager;
import java.text.SimpleDateFormat;
import java.util.*;

public class AssistantBackgroundService extends Service implements SensorEventListener {
    private Handler handler;
    private int lastBat = -1;
    private SensorManager sm;
    private long lastShake = 0;
    private boolean torch = false;

    private final BroadcastReceiver batRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            SharedPreferences p = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
            if (!p.getBoolean("master_switch", false)) return;
            int lvl = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean chg = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL;
            if (lvl != lastBat && p.getBoolean("battery_switch", false)) {
                if (chg && (lvl == 20 || lvl == 50 || lvl == 80 || lvl == 100)) {
                    lastBat = lvl; SystemEventReceiver.speak(ctx, "ব্যাটারি " + lvl + " পার্সেন্ট চার্জ হয়েছে");
                    if (lvl == 100 && p.getBoolean("full_charge_alarm", false)) new ToneGenerator(AudioManager.STREAM_ALARM, 100).startTone(ToneGenerator.TONE_PROP_BEEP2, 3000);
                } else if (!chg && (lvl == 80 || lvl == 60 || lvl == 40 || lvl == 20 || lvl == 10)) {
                    lastBat = lvl; SystemEventReceiver.speak(ctx, "ব্যাটারি চার্জ " + lvl + " পার্সেন্ট");
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            NotificationChannel ch = new NotificationChannel("galaxy_ai_core", "Galaxy AI Core", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "galaxy_ai_core").setContentTitle("Galaxy AI Pro").setContentText("Crafted by NAHIM").setSmallIcon(android.R.drawable.ic_btn_speak_now).build();
            startForeground(1001, n);
        } catch (Exception ignored) {}
        try { registerReceiver(batRec, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); } catch (Exception ignored) {}
        try {
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sm != null) sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception ignored) {}
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override public void run() { tick(); handler.postDelayed(this, 60000); }
        }, 10000);
    }

    private void tick() {
        SharedPreferences p = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        if (!p.getBoolean("master_switch", false)) return;
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MINUTE), h = c.get(Calendar.HOUR_OF_DAY);
        if (p.getBoolean("sleep_mode", false) && h >= 0 && h < 5) return;
        int iv = p.getInt("clock_interval", 0);
        if (iv > 0 && m % iv == 0) SystemEventReceiver.speak(this, "এখন সময় " + new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()));
        if (p.getBoolean("friday_alert", false) && c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && h == 8 && m == 0) SystemEventReceiver.speak(this, "আজ পবিত্র শুক্রবার");
        if (p.getBoolean("morning_brief", false) && h == 7 && m == 0) SystemEventReceiver.speak(this, "শুভ সকাল। " + SmartCalendarWeather.getBanglaDateInfo(this));
        if (p.getBoolean("namaz_alarm", false)) {
            String hhmm = String.format(Locale.US, "%02d:%02d", h, m);
            if (hhmm.equals("05:00")) SystemEventReceiver.speak(this, "ফজরের নামাজের সময় হয়েছে");
            if (hhmm.equals("13:00")) SystemEventReceiver.speak(this, "জোহরের নামাজের সময় হয়েছে");
            if (hhmm.equals("16:15")) SystemEventReceiver.speak(this, "আসরের নামাজের সময় হয়েছে");
            if (hhmm.equals("18:05")) SystemEventReceiver.speak(this, "মাগরিবের নামাজের সময় হয়েছে");
            if (hhmm.equals("19:30")) SystemEventReceiver.speak(this, "এশার নামাজের সময় হয়েছে");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        SharedPreferences p = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        if (!p.getBoolean("master_switch", false) || !p.getBoolean("shake_torch", false)) return;
        double a = Math.sqrt(e.values*e.values + e.values*e.values + e.values*e.values) - SensorManager.GRAVITY_EARTH;
        if (a > 14 && System.currentTimeMillis() - lastShake > 1500) {
            lastShake = System.currentTimeMillis();
            try { CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE); torch = !torch; cm.setTorchMode(cm.getCameraIdList(), torch); } catch (Exception ignored) {}
        }
    }
    @Override public void onAccuracyChanged(Sensor s, int a) {}
    @Override public int onStartCommand(Intent i, int f, int s) {
        if (i != null && "START_AUTOCALL".equals(i.getAction())) autoCall(i.getStringExtra("number"), i.getIntExtra("seconds", 15));
        return START_STICKY;
    }
    private void autoCall(String num, int sec) {
        SharedPreferences p = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        if (!p.getBoolean("autocall_active", false)) return;
        try { Intent ci = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + num)); ci.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(ci); } catch (Exception ignored) {}
        handler.postDelayed(() -> {
            if (p.getBoolean("autocall_active", false)) {
                try { TelecomManager tm = (TelecomManager) getSystemService(TELECOM_SERVICE); if (tm != null) tm.endCall(); } catch (Exception ignored) {}
                handler.postDelayed(() -> autoCall(num, sec), 2500);
            }
        }, sec * 1000L);
    }
    @Override public void onDestroy() { super.onDestroy(); try { unregisterReceiver(batRec); } catch (Exception ignored) {} }
    @Override public IBinder onBind(Intent i) { return null; }
}
