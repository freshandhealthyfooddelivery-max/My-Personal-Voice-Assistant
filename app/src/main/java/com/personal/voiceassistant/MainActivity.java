package com.personal.voiceassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
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
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#040711"));
        getWindow().setNavigationBarColor(Color.parseColor("#040711"));

        prefs = getSharedPreferences("NahimUltraV5", MODE_PRIVATE);
        SystemEventReceiver.initEngine(this);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#040711"));
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new NativeBridge(), "NahimCore");
        webView.loadDataWithBaseURL(null, buildFlagshipHtml(), "text/html", "UTF-8", null);
        setContentView(webView);
    }

    public class NativeBridge {
        @JavascriptInterface public boolean getBool(String k) { return prefs.getBoolean(k, false); }
        @JavascriptInterface public void setBool(String k, boolean v) {
            prefs.edit().putBoolean(k, v).apply();
            if ("master_switch".equals(k)) {
                try {
                    Intent i = new Intent(MainActivity.this, AssistantBackgroundService.class);
                    if (v) startService(i); else stopService(i);
                } catch (Exception ignored) {}
            }
        }
        @JavascriptInterface public int getInt(String k) { return prefs.getInt(k, 0); }
        @JavascriptInterface public void setInt(String k, int v) { prefs.edit().putInt(k, v).apply(); }
        @JavascriptInterface public String getStr(String k, String d) { return prefs.getString(k, d); }
        @JavascriptInterface public void setStr(String k, String v) { prefs.edit().putString(k, v).apply(); }
        @JavascriptInterface public int getBattery() {
            Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return i != null ? i.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) : 100;
        }
        @JavascriptInterface public void action(String cmd) {
            runOnUiThread(() -> {
                if ("voice_ask".equals(cmd)) startActivity(new Intent(MainActivity.this, VoiceAssistActivity.class));
                else if ("bangla_date".equals(cmd)) SystemEventReceiver.speak(MainActivity.this, SmartCalendarWeather.getBanglaDateInfo(MainActivity.this));
                else if ("arabic_date".equals(cmd)) SystemEventReceiver.speak(MainActivity.this, SmartCalendarWeather.getHijriDateInfo(MainActivity.this));
                else if ("weather".equals(cmd)) SmartCalendarWeather.fetchAndSpeakWeather(MainActivity.this);
                else if ("lock_assist".equals(cmd)) startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                else if ("perm_sys".equals(cmd)) reqPerms();
                else if ("perm_notif".equals(cmd)) startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                else if ("pick_ramadan".equals(cmd)) pick(201);
                else if ("pick_bangla".equals(cmd)) pick(202);
                else if ("pick_arabic".equals(cmd)) pick(203);
                else if ("test_voice".equals(cmd)) SystemEventReceiver.speak(MainActivity.this, "নমস্কার ও আসসালামু আলাইকুম, আমি আপনার পার্সোনাল এআই অ্যাসিস্ট্যান্ট");
            });
        }
        @JavascriptInterface public void startAutoCall(String num, int sec) {
            runOnUiThread(() -> {
                prefs.edit().putBoolean("autocall_active", true).apply();
                Intent i = new Intent(MainActivity.this, AssistantBackgroundService.class);
                i.setAction("START_AUTOCALL"); i.putExtra("number", num); i.putExtra("seconds", sec);
                startService(i);
                Toast.makeText(MainActivity.this, "Auto-Call Active", Toast.LENGTH_SHORT).show();
            });
        }
        @JavascriptInterface public void stopAutoCall() {
            prefs.edit().putBoolean("autocall_active", false).apply();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Auto-Call Stopped", Toast.LENGTH_SHORT).show());
        }
    }

    private void pick(int c) { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("text/plain"); startActivityForResult(i, c); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null && req >= 201 && req <= 203 && data.getData() != null) {
            String f = (req == 201) ? "ramadan.txt" : (req == 202 ? "bangla_cal.txt" : "arabic_cal.txt");
            try (InputStream in = getContentResolver().openInputStream(data.getData()); FileOutputStream out = new FileOutputStream(new File(getExternalFilesDir(null), f))) {
                int b; while ((b = in.read()) != -1) out.write(b);
                Toast.makeText(this, "Calendar File Imported!", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        }
    }

    private void reqPerms() {
        List<String> p = new ArrayList<>(Arrays.asList(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA));
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS);
        String[] a = new String[p.size()]; for (int i = 0; i < p.size(); i++) a[i] = p.get(i);
        ActivityCompat.requestPermissions(this, a, 101);
    }

    private String buildFlagshipHtml() {
        return "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'><style>"
            + "*{box-sizing:border-box;margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;-webkit-tap-highlight-color:transparent}"
            + "body{background:radial-gradient(circle at 20% 10%,#0f1e3d 0%,#040711 55%,#02040a 100%);color:#f8fafc;min-height:100vh;padding:20px 16px 40px}"
            + ".hdr{display:flex;justify-content:space-between;align-items:center;margin-bottom:22px}"
            + ".brand h1{font-size:24px;font-weight:800;background:linear-gradient(90deg,#fff,#38bdf8);-webkit-background-clip:text;-webkit-text-fill-color:transparent}"
            + ".brand p{font-size:11px;color:#38bdf8;font-weight:600;letter-spacing:1px;margin-top:2px}"
            + ".lang-btn{background:rgba(255,255,255,0.08);border:1px solid rgba(255,255,255,0.15);color:#fff;padding:8px 14px;border-radius:20px;font-size:12px;font-weight:600;backdrop-filter:blur(12px)}"
            + ".glass{background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);border-radius:26px;padding:18px;margin-bottom:16px;backdrop-filter:blur(20px);box-shadow:0 8px 32px rgba(0,0,0,0.4)}"
            + ".orb-wrap{text-align:center;padding:14px 0}"
            + ".orb{width:110px;height:110px;border-radius:50%;margin:0 auto 14px;background:linear-gradient(135deg,#2563eb,#7c3aed,#06b6d4);display:flex;align-items:center;justify-content:center;box-shadow:0 0 35px rgba(56,189,248,0.45);animation:pulse 2.2s infinite}"
            + "@keyframes pulse{0%,100%{transform:scale(1);box-shadow:0 0 28px rgba(56,189,248,0.4)}50%{transform:scale(1.06);box-shadow:0 0 48px rgba(124,58,237,0.7)}}"
            + ".row{display:flex;align-items:center;justify-content:space-between;padding:13px 0;border-bottom:1px solid rgba(255,255,255,0.06)}"
            + ".row:last-child{border:none}"
            + ".ico{width:38px;height:38px;border-radius:12px;display:flex;align-items:center;justify-content:center;margin-right:12px;flex-shrink:0}"
            + ".lbl{flex:1;font-size:14px;font-weight:500}.sub{font-size:11px;color:#94a3b8;margin-top:2px}"
            + ".sw{position:relative;width:48px;height:28px}.sw input{opacity:0;width:0;height:0}"
            + ".sl{position:absolute;inset:0;background:#1e293b;border-radius:30px;transition:.3s;border:1px solid rgba(255,255,255,0.1)}"
            + ".sl:before{content:'';position:absolute;height:22px;width:22px;left:2px;bottom:2px;background:#fff;border-radius:50%;transition:.3s}"
            + "input:checked+.sl{background:linear-gradient(90deg,#2563eb,#38bdf8)}input:checked+.sl:before{transform:translateX(20px)}"
            + ".grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px}"
            + ".tile{background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.09);border-radius:22px;padding:16px;transition:.2s}"
            + ".tile:active{transform:scale(0.97);background:rgba(56,189,248,0.12)}"
            + ".tile h3{font-size:14px;margin-top:10px;font-weight:600}.tile p{font-size:11px;color:#94a3b8;margin-top:3px}"
            + ".page{display:none}.page.active{display:block;animation:fade .25s ease}@keyframes fade{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}"
            + ".back{display:inline-flex;align-items:center;gap:6px;background:rgba(255,255,255,0.08);padding:8px 14px;border-radius:16px;font-size:13px;font-weight:600;margin-bottom:16px}"
