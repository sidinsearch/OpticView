package com.example.opticview.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.opticview.R;
import com.example.opticview.api.GroqApiService;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Handler handler = new Handler();
    private static final long INTERVAL = 20000; // 20 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 101);
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

                Log.d("CameraActivity", "Camera started and capture loop initiated.");

            } catch (Exception e) {
                Log.e("CameraActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private final Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            if (imageCapture == null) return;

            Log.d("CameraCapture", "Attempting to capture image...");

            imageCapture.takePicture(ContextCompat.getMainExecutor(CameraActivity.this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            Log.d("CameraCapture", "Image captured successfully");

                            Bitmap bitmap = imageProxyToBitmap(image);
                            image.close();

                            // Resize
                            Bitmap resizedBitmap = resizeBitmap(bitmap, 800); // Resize to max width = 800px

                            // Compress
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
                            byte[] imageBytes = baos.toByteArray();

                            // Convert to Base64
                            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                            Log.d("CameraCapture", "Base64 Image Size: " + (base64Image.length() / 1024) + " KB");

                            // Send to API
                            GroqApiService.sendImageToApi(CameraActivity.this, base64Image);

                            runOnUiThread(() ->
                                    Toast.makeText(CameraActivity.this, "Captured & Sent", Toast.LENGTH_SHORT).show()
                            );

                            handler.postDelayed(captureRunnable, INTERVAL);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e("CameraCapture", "Capture failed", exception);
                            handler.postDelayed(captureRunnable, INTERVAL); // Retry anyway
                        }
                    });
        }
    };

    // Convert ImageProxy to Bitmap
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // Resize bitmap to a max width while maintaining aspect ratio
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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(captureRunnable);
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
        Log.d("CameraActivity", "CameraActivity destroyed and capture loop stopped.");
    }
}
