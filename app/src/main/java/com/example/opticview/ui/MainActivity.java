package com.example.opticview.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.opticview.sensor.ShakeService; // ✅ Make sure this import is here

public class MainActivity extends AppCompatActivity {

    private int currentLanguageMode = 0; // 0 = English, 1 = Marathi, 2 = Hindi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ✅ Start ShakeService here
        Intent shakeServiceIntent = new Intent(this, ShakeService.class);
        startService(shakeServiceIntent);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startButton = findViewById(R.id.btn_start_capture);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("language_mode", currentLanguageMode); // Pass the selected mode
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
            return true; // consume the event
        }
        return super.onKeyDown(keyCode, event);
    }
}
