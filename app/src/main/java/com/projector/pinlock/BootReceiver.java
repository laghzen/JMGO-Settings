package com.projector.pinlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Отправляем команду сразу при старте системы
        sendEnableCommand(context);

        // 2. И страхуемся: повторяем через 5 секунд, когда все системные процессы JMGO точно инициализировались
        final PendingResult result = goAsync();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                sendEnableCommand(context);
            } finally {
                result.finish();
            }
        }, 5000);
    }

    private void sendEnableCommand(Context context) {
        try {
            Intent jmgoIntent = new Intent("action.jmgo.request.accessibility.service");
            jmgoIntent.putExtra("compontentNameStr", context.getPackageName() + "/" + KeyService.class.getName());
            jmgoIntent.putExtra("enabled", true);
            context.sendBroadcast(jmgoIntent);
        } catch (Exception ignored) {}
    }
}
