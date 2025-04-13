package com.example.opticview.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechManager {

    private TextToSpeech tts;
    private boolean isInitialized = false;

    public TextToSpeechManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported or missing data");
                } else {
                    isInitialized = true;
                    Log.d("TTS", "TextToSpeech initialized successfully");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    public void speak(String text) {
        if (isInitialized && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
        } else {
            Log.e("TTS", "TextToSpeech not ready or empty text");
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}