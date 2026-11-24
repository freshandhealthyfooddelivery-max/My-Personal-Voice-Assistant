package com.personal.voiceassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SystemEventReceiver extends BroadcastReceiver {
    private static TextToSpeech tts;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("master_switch", false)) return;

        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, AssistantBackgroundService.class));
            speak(context, "ফোন চালু হয়েছে, ভয়েস অ্যাসিস্ট্যান্ট সক্রিয় আছে");
        } else if (action.equals(Intent.ACTION_POWER_CONNECTED) && prefs.getBoolean("battery_switch", true)) {
            speak(context, "চার্জার কানেক্টেড");
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            if (prefs.getBoolean("theft_alarm", false)) {
                triggerLoudAlarm(context, "সতর্কতা! অনুমতি ছাড়া চার্জার খোলা হয়েছে");
            } else if (prefs.getBoolean("battery_switch", true)) {
                speak(context, "চার্জার রিমুভড");
            }
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (msgs != null && msgs.length > 0) {
                List<SmsMessage> msgList = Arrays.asList(msgs);
                SmsMessage msg = msgList.get(0);
                String sender = msg.getDisplayOriginatingAddress();
                String body = msg.getMessageBody();

                if (body != null && body.trim().equalsIgnoreCase("FINDMYPHONE")) {
                    triggerLoudAlarm(context, "আমি এখানে আছি, আপনার ফোন এখানে");
                    return;
                }

                if (prefs.getBoolean("sms_switch", true)) {
                    if (prefs.getBoolean("sms_full_read", false)) {
                        speak(context, "মেসেজ ফ্রম " + sender + ". " + body);
                    } else {
                        speak(context, "মেসেজ ফ্রম " + sender + ", ইউ ক্যান রিড ইট অর ইগনোর");
                    }
                }
            }
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && prefs.getBoolean("call_switch", true)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                String name = getContactName(context, number);
                String caller = (name != null) ? name : (number != null ? number : "অজানা নাম্বার");
                speak(context, "কল ফ্রম " + caller + ", প্লিজ রিসিভ");
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && prefs.getBoolean("riding_mode", false)) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (am != null) am.setSpeakerphoneOn(true);
            }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && prefs.getBoolean("network_alert", false)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNet = cm != null ? cm.getActiveNetworkInfo() : null;
            if (activeNet != null && activeNet.isConnected()) {
                speak(context, activeNet.getTypeName() + " সংযোগ চালু হয়েছে");
            } else {
                speak(context, "ইন্টারনেট সংযোগ বিচ্ছিন্ন হয়েছে");
            }
        } else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) && prefs.getBoolean("auto_unsilent", false)) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        speak(context, "এক ঘণ্টা পূর্ণ হয়েছে, ফোনের সাউন্ড পুনরায় চালু করা হলো");
                    } catch (Exception ignored) {}
                }, 3600000L);
            }
        }
    }

    private void triggerLoudAlarm(Context context, String voiceMsg) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            }
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 4000);
            speak(context, voiceMsg);
        } catch (Exception ignored) {}
    }

    private String getContactName(Context context, String phoneNumber) {
        if (phoneNumber == null) return null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                String contactName = null;
                if (cursor.moveToFirst()) contactName = cursor.getString(0);
                cursor.close();
                return contactName;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void speak(Context context, String text) {
        SharedPreferences prefs = context.getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE);
        String lang = prefs.getString("voice_lang", "bn");
        boolean isMale = prefs.getBoolean("voice_male", false);
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                if ("en_uk".equals(lang)) tts.setLanguage(Locale.UK);
                else if ("en_us".equals(lang)) tts.setLanguage(Locale.US);
                else tts.setLanguage(new Locale("bn", "BD"));
                tts.setPitch(isMale ? 0.75f : 1.15f);
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, "EventSpeak");
            }
        });
    }
                                                                                              }
