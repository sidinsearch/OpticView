package com.example.opticview.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.opticview.R;
import com.example.opticview.api.GroqApiClient;
import com.example.opticview.tts.TextToSpeechManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Handler handler = new Handler();
    private static final long INTERVAL = 20000;

    private TextToSpeechManager ttsManager;

    private Bitmap lastCapturedBitmap = null;

    private int selectedLanguageMode = 0; // From MainActivity

    private long lastGroqCallTime = 0;
    private static final long GROQ_COOLDOWN_MS = 15000;

    private boolean canCallGroq() {
        return System.currentTimeMillis() - lastGroqCallTime > GROQ_COOLDOWN_MS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        ttsManager = new TextToSpeechManager(this);

        // 🔄 Get the selected language from MainActivity
        selectedLanguageMode = getIntent().getIntExtra("language_mode", 0);
        Toast.makeText(this, "Language Selected: " + getLangName(selectedLanguageMode), Toast.LENGTH_SHORT).show();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                handler.post(captureRunnable);

            } catch (Exception e) {
                Log.e("CameraActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private final Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            if (imageCapture == null) return;

            imageCapture.takePicture(ContextCompat.getMainExecutor(CameraActivity.this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            Bitmap bitmap = imageProxyToBitmap(image);
                            image.close();

                            Bitmap resizedBitmap = resizeBitmap(bitmap, 800);
                            lastCapturedBitmap = resizedBitmap;

                            if (canCallGroq()) {
                                lastGroqCallTime = System.currentTimeMillis();
                                GroqApiClient.sendImagetoGroq(resizedBitmap, selectedLanguageMode, new GroqApiClient.GroqResponseListener() {
                                    @Override
                                    public void onSuccess(String description) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(CameraActivity.this, "Description: " + description, Toast.LENGTH_LONG).show();
                                            ttsManager.speak(description);
                                        });
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        runOnUiThread(() ->
                                                Toast.makeText(CameraActivity.this, "Groq Error: " + error, Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                });
                            } else {
                                Toast.makeText(CameraActivity.this, "Rate limit: Try again later.", Toast.LENGTH_SHORT).show();
                            }

                            handler.postDelayed(captureRunnable, INTERVAL);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e("CameraCapture", "Capture failed", exception);
                            handler.postDelayed(captureRunnable, INTERVAL);
                        }
                    });
        }
    };

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private Bitmap resizeBitmap(Bitmap original, int maxWidth) {
        float aspectRatio = original.getWidth() / (float) original.getHeight();
        int height = Math.round(maxWidth / aspectRatio);
        return Bitmap.createScaledBitmap(original, maxWidth, height, false);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                && lastCapturedBitmap != null) {

            if (canCallGroq()) {
                lastGroqCallTime = System.currentTimeMillis();
                GroqApiClient.sendImagetoGroq(lastCapturedBitmap, selectedLanguageMode, new GroqApiClient.GroqResponseListener() {
                    @Override
                    public void onSuccess(String description) {
                        runOnUiThread(() -> {
                            Toast.makeText(CameraActivity.this, "🔁 Replaying with " + getLangName(selectedLanguageMode), Toast.LENGTH_SHORT).show();
                            ttsManager.speak(description);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(CameraActivity.this, "Groq Error: " + error, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } else {
                Toast.makeText(this, "Please wait before trying again.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getLangName(int langCode) {
        switch (langCode) {
            case 1: return "Marathi";
            case 2: return "Hindi";
            default: return "English";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(captureRunnable);
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
    }
}
