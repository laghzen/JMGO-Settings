package com.projector.pinlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Проекторы JMGO прогружают свои службы медленно (до 30 сек).
        // Поэтому отправляем команду серией: через 1, 4, 10, 20 и 35 секунд после старта.
        int[] delays = {1000, 4000, 10000, 20000, 35000};
        final PendingResult result = goAsync();

        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < delays.length; i++) {
            final boolean isLast = (i == delays.length - 1);
            handler.postDelayed(() -> {
                sendEnableCommand(context);
                if (isLast) {
                    try {
                        result.finish();
                    } catch (Exception ignored) {}
                }
            }, delays[i]);
        }
    }

    public static void sendEnableCommand(Context context) {
        try {
            Intent jmgoIntent = new Intent("action.jmgo.request.accessibility.service");
            jmgoIntent.putExtra("compontentNameStr", context.getPackageName() + "/" + KeyService.class.getName());
            jmgoIntent.putExtra("enabled", true);
            jmgoIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            jmgoIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            context.sendBroadcast(jmgoIntent);
        } catch (Exception ignored) {}
    }
}
