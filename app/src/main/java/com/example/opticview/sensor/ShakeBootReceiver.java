package com.example.opticview.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ShakeBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_USER_PRESENT.equals(intent.getAction()) ||
                Intent.ACTION_SCREEN_ON.equals(intent.getAction()) ||
                Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {

            Intent serviceIntent = new Intent(context, ShakeService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
