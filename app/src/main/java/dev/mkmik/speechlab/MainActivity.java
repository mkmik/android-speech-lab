package dev.mkmik.speechlab;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
  SpeechRecognizer recognizer;
  TextView output;
  long started;
  boolean onDevice;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(32, 48, 32, 24);
    Button standard = new Button(this); standard.setText("Default recognizer");
    Button local = new Button(this); local.setText("On-device recognizer");
    Button stop = new Button(this); stop.setText("Stop");
    output = new TextView(this); output.setText("Select a recognizer to begin.");
    root.addView(standard); root.addView(local); root.addView(stop); root.addView(output);
    setContentView(root);
    standard.setOnClickListener(v -> start(false));
    local.setOnClickListener(v -> start(true));
    stop.setOnClickListener(v -> { if (recognizer != null) recognizer.stopListening(); });
  }
  void start(boolean local) {
    onDevice = local;
    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
      output.setText("Grant microphone permission, then tap again.");
      return;
    }
    if (recognizer != null) { recognizer.destroy(); recognizer = null; }
    if (local && !SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
      output.setText("On-device recognition unavailable."); return;
    }
    if (!local && !SpeechRecognizer.isRecognitionAvailable(this)) {
      output.setText("Speech recognition unavailable."); return;
    }
    recognizer = local ? SpeechRecognizer.createOnDeviceSpeechRecognizer(this) : SpeechRecognizer.createSpeechRecognizer(this);
    recognizer.setRecognitionListener(new RecognitionListener() {
      @Override public void onReadyForSpeech(Bundle b) { output.setText("Listening (" + (onDevice ? "on-device" : "default") + ")"); }
      @Override public void onBeginningOfSpeech() {}
      @Override public void onRmsChanged(float rms) {}
      @Override public void onBufferReceived(byte[] bytes) {}
      @Override public void onEndOfSpeech() {}
      @Override public void onError(int code) { output.setText("Recognition error: " + code); }
      @Override public void onResults(Bundle b) { show("Final", b); }
      @Override public void onPartialResults(Bundle b) { show("Partial", b); }
      @Override public void onEvent(int type, Bundle b) {}
    });
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    started = SystemClock.elapsedRealtime();
    output.setText("Starting...");
    recognizer.startListening(intent);
  }
  void show(String stage, Bundle b) {
    java.util.ArrayList<String> words = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    long elapsed = SystemClock.elapsedRealtime() - started;
    output.setText(stage + " (" + elapsed + " ms since start):\n" +
      (words == null || words.isEmpty() ? "(no text)" : words.get(0)));
  }
  @Override protected void onDestroy() { if (recognizer != null) recognizer.destroy(); super.onDestroy(); }
}
