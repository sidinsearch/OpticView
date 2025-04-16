package com.example.opticview.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.opticview.R;
import com.example.opticview.camera.CameraActivity;
import com.example.opticview.sensor.ShakeService;

public class MainActivity extends AppCompatActivity {

    private int currentLanguageMode = 0; // 0 = English, 1 = Marathi, 2 = Hindi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ✅ Start ShakeService
        Intent shakeServiceIntent = new Intent(this, ShakeService.class);
        startService(shakeServiceIntent);

        // ✅ Ask user to disable battery optimization only once
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean askedBatteryOpt = prefs.getBoolean("asked_battery_optimization", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !askedBatteryOpt) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                // ✅ Save preference so we don't ask again
                prefs.edit().putBoolean("asked_battery_optimization", true).apply();
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startButton = findViewById(R.id.btn_start_capture);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("language_mode", currentLanguageMode);
            startActivity(intent);
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentLanguageMode = (currentLanguageMode + 1) % 3;

            String modeText;
            switch (currentLanguageMode) {
                case 1:
                    modeText = "Language Mode: Marathi";
                    break;
                case 2:
                    modeText = "Language Mode: Hindi";
                    break;
                default:
                    modeText = "Language Mode: English";
                    break;
            }

            Toast.makeText(this, modeText, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
