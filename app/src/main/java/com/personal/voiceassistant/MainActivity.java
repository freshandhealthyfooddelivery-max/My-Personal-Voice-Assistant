package com.personal.voiceassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.webkit.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#040711"));
        prefs = getSharedPreferences("NahimUltraV5", MODE_PRIVATE);
        SystemEventReceiver.initEngine(this);

        WebView web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#040711"));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new Bridge(), "NahimCore");
        web.loadUrl("file:///android_asset/ui.html");
        setContentView(web);
    }

    public class Bridge {
        @JavascriptInterface public boolean getBool(String k) { return prefs.getBoolean(k, false); }
        @JavascriptInterface public void setBool(String k, boolean v) {
            prefs.edit().putBoolean(k, v).apply();
            if ("master_switch".equals(k)) {
                try { Intent i = new Intent(MainActivity.this, AssistantBackgroundService.class); if (v) startService(i); else stopService(i); } catch (Exception ignored) {}
            }
        }
        @JavascriptInterface public void setInt(String k, int v) { prefs.edit().putInt(k, v).apply(); }
        @JavascriptInterface public void setStr(String k, String v) { prefs.edit().putString(k, v).apply(); }
        @JavascriptInterface public int getBattery() { Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); return i != null ? i.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) : 100; }
        @JavascriptInterface public void action(String c) {
            runOnUiThread(() -> {
                if ("voice_ask".equals(c)) startActivity(new Intent(MainActivity.this, VoiceAssistActivity.class));
                else if ("bangla_date".equals(c)) SystemEventReceiver.speak(MainActivity.this, SmartCalendarWeather.getBanglaDateInfo(MainActivity.this));
                else if ("arabic_date".equals(c)) SystemEventReceiver.speak(MainActivity.this, SmartCalendarWeather.getHijriDateInfo(MainActivity.this));
                else if ("weather".equals(c)) SmartCalendarWeather.fetchAndSpeakWeather(MainActivity.this);
                else if ("lock_assist".equals(c)) startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                else if ("perm_sys".equals(c)) reqPerms();
                else if ("perm_notif".equals(c)) startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                else if ("pick_ramadan".equals(c)) pick(201);
                else if ("pick_bangla".equals(c)) pick(202);
                else if ("pick_arabic".equals(c)) pick(203);
                else if ("test_voice".equals(c)) SystemEventReceiver.speak(MainActivity.this, "আসসালামু আলাইকুম, আমি আপনার পার্সোনাল এআই অ্যাসিস্ট্যান্ট");
            });
        }
        @JavascriptInterface public void startAutoCall(String num, int sec) {
            runOnUiThread(() -> {
                prefs.edit().putBoolean("autocall_active", true).apply();
                Intent i = new Intent(MainActivity.this, AssistantBackgroundService.class); i.setAction("START_AUTOCALL"); i.putExtra("number", num); i.putExtra("seconds", sec); startService(i);
                Toast.makeText(MainActivity.this, "Auto-Call Active", Toast.LENGTH_SHORT).show();
            });
        }
        @JavascriptInterface public void stopAutoCall() { prefs.edit().putBoolean("autocall_active", false).apply(); runOnUiThread(() -> Toast.makeText(MainActivity.this, "Stopped", Toast.LENGTH_SHORT).show()); }
    }

    private void pick(int c) { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, c); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null && req >= 201 && req <= 203 && data.getData() != null) {
            String f = (req == 201) ? "ramadan.txt" : (req == 202 ? "bangla_cal.txt" : "arabic_cal.txt");
            try (InputStream in = getContentResolver().openInputStream(data.getData()); FileOutputStream out = new FileOutputStream(new File(getExternalFilesDir(null), f))) {
                int b; while ((b = in.read()) != -1) out.write(b);
                Toast.makeText(this, "Imported!", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        }
    }

    private void reqPerms() {
        List<String> p = new ArrayList<>(Arrays.asList(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA));
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS);
        String[] a = new String[p.size()]; for (int i = 0; i < p.size(); i++) a[i] = p.get(i);
        ActivityCompat.requestPermissions(this, a, 101);
    }
}
