package com.personal.voiceassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
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
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED) && prefs.getBoolean("battery_switch", true)) {
            speak(context, "চার্জার রিমুভড");
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED") && prefs.getBoolean("sms_switch", true)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
                    String sender = msg.getDisplayOriginatingAddress();
                    String body = msg.getMessageBody();
                    boolean fullRead = prefs.getBoolean("sms_full_read", false);
                    if (fullRead) {
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
                if (name != null) {
                    speak(context, "কল ফ্রম " + name + ", প্লিজ রিসিভ");
                } else if (number != null) {
                    speak(context, "কল ফ্রম " + number + ", প্লিজ রিসিভ");
                }
            }
        }
    }

    private String getContactName(Context context, String phoneNumber) {
        if (phoneNumber == null) return null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                String contactName = null;
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0);
                }
                cursor.close();
                return contactName;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void speak(Context context, String text) {
        SharedPreferences prefs = context.getSharedPreferences("AssistantPrefs", Context.MODE_PRIVATE);
        String lang = prefs.getString("voice_lang", "bn");
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                if ("en_uk".equals(lang)) tts.setLanguage(Locale.UK);
                else if ("en_us".equals(lang)) tts.setLanguage(Locale.US);
                else tts.setLanguage(new Locale("bn", "BD"));
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, "EventSpeak");
            }
        });
    }
                                           }
