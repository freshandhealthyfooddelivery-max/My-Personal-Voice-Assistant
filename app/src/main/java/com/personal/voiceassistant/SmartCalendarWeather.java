package com.personal.voiceassistant;

import android.content.Context;
import android.icu.util.IslamicCalendar;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SmartCalendarWeather {

    public static String getEnglishAndDayInfo() {
        Calendar cal = Calendar.getInstance();
        String[] daysBn = {"রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"};
        String dayName = daysBn[cal.get(Calendar.DAY_OF_WEEK) - 1];
        String dateStr = new SimpleDateFormat("dd MMMM yyyy", Locale.US).format(new Date());
        return "আজ " + dayName + ", ইংরেজি তারিখ " + dateStr;
    }

    public static String getHijriDateInfo(Context context) {
        String custom = readCustomCalendar(context, "arabic_cal.txt");
        if (custom != null) return custom;

        IslamicCalendar ic = new IslamicCalendar();
        int day = ic.get(IslamicCalendar.DATE);
        int month = ic.get(IslamicCalendar.MONTH);
        int year = ic.get(IslamicCalendar.YEAR);
        String[] hijriMonths = {"মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"};
        String mName = (month >= 0 && month < 12) ? hijriMonths[month] : "";
        return "আজ আরবি হিজরি " + day + " " + mName + " " + year + " হিজরি সন";
    }

    public static String getBanglaDateInfo(Context context) {
        String custom = readCustomCalendar(context, "bangla_cal.txt");
        if (custom != null) return custom;

        Calendar cal = Calendar.getInstance();
        int gDay = cal.get(Calendar.DAY_OF_MONTH);
        int gMonth = cal.get(Calendar.MONTH) + 1;
        int gYear = cal.get(Calendar.YEAR);
        int bYear = (gMonth < 4 || (gMonth == 4 && gDay < 14)) ? gYear - 594 : gYear - 593;
        String[] bMonths = {"বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন", "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"};
        int mIdx = (gMonth + 8) % 12;
        int bDay = (gDay >= 14) ? (gDay - 13) : (gDay + 17);
        String[] daysBn = {"রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"};
        String dayName = daysBn[cal.get(Calendar.DAY_OF_WEEK) - 1];
        return "আজ " + dayName + ", বাংলা " + bDay + " " + bMonths[mIdx] + " " + bYear + " বঙ্গাব্দ";
    }

    public static String readCustomCalendar(Context context, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null), fileName);
            if (!file.exists()) return null;
            String today = new SimpleDateFormat("dd-MM", Locale.US).format(new Date());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(today)) {
                    br.close();
                    return line.substring(today.length()).trim();
                }
            }
            br.close();
        } catch (Exception ignored) {}
        return null;
    }

    public static void fetchAndSpeakWeather(Context context) {
        new Thread(() -> {
            try {
                URL url = new URL("https://wttr.in");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String result = in.readLine();
                in.close();
                if (result != null && !result.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> SystemEventReceiver.speak(context, "Live Weather Report. " + result));
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> SystemEventReceiver.speak(context, "আবহাওয়ার তথ্য পাওয়া যায়নি, ইন্টারনেট সংযোগ পরীক্ষা করুন"));
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> SystemEventReceiver.speak(context, "আবহাওয়া জানতে ইন্টারনেট চালু রাখুন"));
            }
        }).start();
    }
                }
