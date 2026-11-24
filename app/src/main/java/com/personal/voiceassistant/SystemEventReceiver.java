package com.personal.voiceassistant;

import android.content.*;
import android.database.Cursor;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.speech.tts.*;
import android.telephony.*;
import java.util.*;

public class SystemEventReceiver extends BroadcastReceiver {
    private static TextToSpeech tts;
    private static boolean ttsReady = false;

    public static void initEngine(Context ctx) {
        if (tts != null && ttsReady) return;
        tts = new TextToSpeech(ctx.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
                applyVoiceConfig(ctx);
            }
        });
    }

    private static void applyVoiceConfig(Context ctx) {
        if (tts == null || !ttsReady) return;
        SharedPreferences p = ctx.getSharedPreferences("NahimUltraV5", Context.MODE_PRIVATE);
        String lang = p.getString("voice_lang", "bn");
        boolean male = p.getBoolean("voice_male", false);
        Locale loc = "en_uk".equals(lang) ? Locale.UK : ("en_us".equals(lang) ? Locale.US : ("hi".equals(lang) ? new Locale("hi", "IN") : new Locale("bn", "BD")));
        tts.setLanguage(loc);
        tts.setSpeechRate(1.02f);
        tts.setPitch(1.0f);
        try {
            if (tts.getVoices() != null) {
                for (Voice v : tts.getVoices()) {
                    String n = v.getName().toLowerCase();
                    if (v.getLocale().getLanguage().equals(loc.getLanguage()) && !v.isNetworkConnectionRequired()) {
                        if (male && (n.contains("male") || n.contains("-b-") || n.contains("-d-"))) { tts.setVoice(v); break; }
                        if (!male && (n.contains("female") || n.contains("-a-") || n.contains("-c-"))) { tts.setVoice(v); break; }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void speak(Context ctx, String text) {
        if (tts == null || !ttsReady) {
            tts = new TextToSpeech(ctx.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true;
                    applyVoiceConfig(ctx);
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FastAI");
                }
            });
        } else {
            applyVoiceConfig(ctx);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FastAI");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences p = context.getSharedPreferences("NahimUltraV5", Context.MODE_PRIVATE);
        if (!p.getBoolean("master_switch", false)) return;
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            try { context.startService(new Intent(context, AssistantBackgroundService.class)); } catch (Exception ignored) {}
        } else if (action.equals(Intent.ACTION_POWER_CONNECTED) && p.getBoolean("battery_switch", false)) {
            speak(context, "চার্জার সংযুক্ত হয়েছে");
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            if (p.getBoolean("theft_alarm", false)) {
                alarm(context, "সতর্কতা! অনুমতি ছাড়া চার্জার খোলা হয়েছে");
            } else if (p.getBoolean("battery_switch", false)) {
                speak(context, "চার্জার খোলা হয়েছে");
            }
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (msgs != null && msgs.length > 0) {
                SmsMessage msg = Arrays.asList(msgs).get(0);
                String sender = msg.getDisplayOriginatingAddress();
                String body = msg.getMessageBody();
                if (body != null && body.trim().equalsIgnoreCase("FINDMYPHONE")) {
                    alarm(context, "আমি এখানে আছি, আপনার ফোন এখানে");
                    return;
                }
                if (p.getBoolean("sms_switch", false)) {
                    speak(context, p.getBoolean("sms_full_read", false) ? ("মেসেজ এসেছে " + sender + " থেকে। " + body) : ("নতুন মেসেজ এসেছে " + sender + " থেকে"));
                }
            }
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && p.getBoolean("call_switch", false)) {
            String st = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(st)) {
                String num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                String name = getName(context, num);
                speak(context, (name != null ? name : (num != null ? num : "অজানা নাম্বার")) + " থেকে কল এসেছে");
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(st) && p.getBoolean("riding_mode", false)) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (am != null) am.setSpeakerphoneOn(true);
            }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && p.getBoolean("network_alert", false)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = cm != null ? cm.getActiveNetworkInfo() : null;
            speak(context, (net != null && net.isConnected()) ? "ইন্টারনেট সংযোগ চালু হয়েছে" : "ইন্টারনেট সংযোগ বিচ্ছিন্ন হয়েছে");
        } else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) && p.getBoolean("auto_unsilent", false)) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try { am.setRingerMode(AudioManager.RINGER_MODE_NORMAL); speak(context, "ফোনের সাউন্ড পুনরায় চালু হলো"); } catch (Exception ignored) {}
                }, 3600000L);
            }
        }
    }

    private void alarm(Context ctx, String msg) {
        try {
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) { am.setRingerMode(AudioManager.RINGER_MODE_NORMAL); am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0); }
            new ToneGenerator(AudioManager.STREAM_ALARM, 100).startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 4000);
            speak(ctx, msg);
        } catch (Exception ignored) {}
    }

    private String getName(Context ctx, String num) {
        if (num == null) return null;
        try (Cursor c = ctx.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num)), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {}
        return null;
    }
}
