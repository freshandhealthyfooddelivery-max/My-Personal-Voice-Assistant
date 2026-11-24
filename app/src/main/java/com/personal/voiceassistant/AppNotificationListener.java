package com.personal.voiceassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class AppNotificationListener extends NotificationListenerService {
    private long lastTime = 0;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("master_switch", false) || !prefs.getBoolean("app_notif_switch", true)) return;

        String pkg = sbn.getPackageName();
        if (pkg == null || pkg.equals(getPackageName()) || pkg.contains("systemui")) return;

        if (prefs.getBoolean("headphone_privacy", false)) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null && !am.isWiredHeadsetOn() && !am.isBluetoothA2dpOn()) return;
        }

        long now = System.currentTimeMillis();
        if (now - lastTime < 3000) return;
        lastTime = now;

        String appName = getAppName(pkg);
        SystemEventReceiver.speak(this, "নোটিফিকেশন ফ্রম " + appName + ", প্লিজ চেক");
    }

    private String getAppName(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (Exception e) {
            return "অ্যাপ";
        }
    }
          }
