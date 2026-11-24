package com.personal.voiceassistant;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class AssistantTileService extends TileService {
    private TextToSpeech tts;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        boolean isActive = prefs.getBoolean("master_switch", false);
        boolean newState = !isActive;

        prefs.edit().putBoolean("master_switch", newState).apply();
        updateTileState();
        speakStatus(newState ? "ভয়েস অ্যাসিস্ট্যান্ট চালু হয়েছে" : "ভয়েস অ্যাসিস্ট্যান্ট বন্ধ করা হয়েছে");
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;
        SharedPreferences prefs = getSharedPreferences("AssistantPrefs", MODE_PRIVATE);
        boolean isActive = prefs.getBoolean("master_switch", false);

        tile.setState(isActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private void speakStatus(String text) {
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("bn", "BD"));
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TileSpeak");
            }
        });
    }
}
