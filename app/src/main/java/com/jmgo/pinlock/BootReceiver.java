package com.jmgo.pinlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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
            jmgoIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
            context.sendBroadcast(jmgoIntent);
        } catch (Exception ignored) {}
    }
}
