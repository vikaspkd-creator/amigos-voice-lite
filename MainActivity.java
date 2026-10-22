package com.amigos.voicelite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 100;
    private final String[] names = {"Hindi", "Kannada", "English", "Tamil", "Telugu", "Marathi", "Russian"};
    private final String[] codes = {"hi", "kn", "en", "ta", "te", "mr", "ru"};
    private Spinner source, target;
    private TextView original, translated, status;
    private TextToSpeech tts;
    private Translator activeTranslator;
    private String lastTranslation = "";
    private int lastTarget = 1;
    private final int green = Color.rgb(24, 69, 56);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, result -> {
            if (result != TextToSpeech.SUCCESS) setStatus("Voice output unavailable. Install or enable a Text-to-Speech engine.");
        });
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(35), dp(22), dp(30));
        root.setBackgroundColor(Color.rgb(249, 247, 241));
        scroll.addView(root);
        TextView heading = label("AMIGOS VOICE", 28, true);
        heading.setTextColor(green);
        root.addView(heading);
        root.addView(label("Speak naturally. Be understood.", 15, false));
        root.addView(space(25));
        root.addView(label("SPEAK IN", 13, true));
        source = spinner(); root.addView(source);
        root.addView(space(8));
        Button swap = button("⇅ Swap languages"); root.addView(swap);
        swap.setOnClickListener(v -> {
            int old = source.getSelectedItemPosition();
            source.setSelection(target.getSelectedItemPosition());
            target.setSelection(old);
        });
        root.addView(space(12));
        root.addView(label("TRANSLATE TO", 13, true));
        target = spinner(); target.setSelection(1); root.addView(target);
        root.addView(space(22));
        Button speak = button("🎤  TAP TO SPEAK");
        speak.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
        speak.setTextColor(Color.WHITE);
        root.addView(speak);
        speak.setOnClickListener(v -> startSpeech());
        root.addView(space(22));
        root.addView(label("YOU SAID", 12, true));
        original = label("Your words will appear here.", 18, false); root.addView(original);
        root.addView(space(20));
        root.addView(label("TRANSLATION", 12, true));
        translated = label("Translated text will appear here.", 22, true);
        translated.setTextColor(green); root.addView(translated);
        root.addView(space(15));
        Button replay = button("🔊  Play translation again"); root.addView(replay);
        replay.setOnClickListener(v -> speakText(lastTranslation, lastTarget));
        root.addView(space(18));
        status = label("Ready. First translation downloads free language models.", 13, false);
        root.addView(status);
        root.addView(space(20));
        root.addView(label("Free internal pilot • No login • No advertisements", 12, false));
        setContentView(scroll);
    }
    private Spinner spinner() {
        Spinner spin = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spin.setAdapter(adapter);
        return spin;
    }
    private TextView label(String content, int sp, boolean bold) {
        TextView text = new TextView(this);
        text.setText(content); text.setTextSize(sp); text.setTextColor(Color.rgb(40, 45, 42));
        if (bold) text.setTypeface(null, Typeface.BOLD);
        text.setPadding(0, dp(6), 0, dp(6)); return text;
    }
    private Button button(String text) {
        Button button = new Button(this); button.setText(text); button.setAllCaps(false);
        return button;
    }
    private View space(int h) { View view = new View(this); view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return view; }
    private int dp(int value) { return (int)(getResources().getDisplayMetrics().density * value + 0.5f); }
    private void setStatus(String message) { status.setText(message); }

    private void startSpeech() {
        int selection = source.getSelectedItemPosition();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, codes[selection] + "-" + countryFor(codes[selection]));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in " + names[selection]);
        try { startActivityForResult(intent, SPEECH_REQUEST); setStatus("Listening..."); }
        catch (ActivityNotFoundException e) { setStatus("No speech recognition app is installed. Install Google's Speech Recognition & Synthesis or Google app."); }
    }
    private String countryFor(String code) {
        if (code.equals("ru")) return "RU";
        if (code.equals("en")) return "IN";
        return "IN";
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SPEECH_REQUEST) return;
        if (resultCode != RESULT_OK || data == null) { setStatus("Speech cancelled or not recognised. Try again."); return; }
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) { setStatus("No words recognised. Try again."); return; }
        String words = results.get(0);
        original.setText(words);
        translate(words, source.getSelectedItemPosition(), target.getSelectedItemPosition());
    }
    private void translate(String words, int from, int to) {
        if (activeTranslator != null) { activeTranslator.close(); activeTranslator = null; }
        if (from == to) { translated.setText(words); lastTranslation=words; lastTarget=to; speakText(words,to); setStatus("Done."); return; }
        String fromLang = TranslateLanguage.fromLanguageTag(codes[from]);
        String toLang = TranslateLanguage.fromLanguageTag(codes[to]);
        if (fromLang == null || toLang == null) { setStatus("This language pair is unavailable."); return; }
        TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(fromLang).setTargetLanguage(toLang).build();
        Translator translator = Translation.getClient(options);
        activeTranslator = translator;
        setStatus("Preparing translation. First use may download language models over the internet...");
        translator.downloadModelIfNeeded(new DownloadConditions.Builder().build())
            .addOnSuccessListener(unused -> {
                if (activeTranslator != translator) return;
                setStatus("Translating...");
                translator.translate(words)
                    .addOnSuccessListener(result -> {
                        if (activeTranslator != translator) return;
                        translated.setText(result); lastTranslation=result; lastTarget=to;
                        setStatus("Translation ready. Tap microphone for the next sentence.");
                        speakText(result, to);
                    })
                    .addOnFailureListener(err -> setStatus("Translation error: " + err.getMessage()));
            }).addOnFailureListener(err -> setStatus("Language download error. Connect to internet and retry. " + err.getMessage()));
    }
    private void speakText(String words, int languageIndex) {
        if (words == null || words.isEmpty()) return;
        Locale locale = Locale.forLanguageTag(codes[languageIndex] + "-" + countryFor(codes[languageIndex]));
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            setStatus("Text translated. No " + names[languageIndex] + " voice installed on this device. Add the language voice in Android Text-to-Speech settings.");
            return;
        }
        tts.setSpeechRate(0.92f);
        tts.speak(words, TextToSpeech.QUEUE_FLUSH, null, "amigos-translation");
    }
    @Override protected void onDestroy() {
        if (activeTranslator != null) activeTranslator.close();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
