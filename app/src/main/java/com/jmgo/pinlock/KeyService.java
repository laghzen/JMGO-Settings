package com.jmgo.pinlock;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class KeyService extends AccessibilityService {

    private final BroadcastReceiver wakeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            triggerJmgoHook();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        registerReceiver(wakeReceiver, filter);

        triggerJmgoHook();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(wakeReceiver);
        } catch (Exception ignored) {}
    }

    private void triggerJmgoHook() {
        Handler handler = new Handler(Looper.getMainLooper());
        int[] delays = {300, 1500, 4000};
        for (int delay : delays) {
            handler.postDelayed(() -> {
                try {
                    Intent jmgoIntent = new Intent("action.jmgo.request.accessibility.service");
                    jmgoIntent.putExtra("compontentNameStr", getPackageName() + "/" + KeyService.class.getName());
                    jmgoIntent.putExtra("enabled", true);
                    jmgoIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
                    sendBroadcast(jmgoIntent);
                } catch (Exception ignored) {}
            }, delay);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 605) {
            if (SystemClock.uptimeMillis() < 25000) {
                return true;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                handleSettingsButton();
            }
            return true;
        }
        return super.onKeyEvent(event);
    }

    private void handleSettingsButton() {
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                CharSequence pkg = root.getPackageName();
                if (pkg != null) {
                    String current = pkg.toString();

                    if ("com.jmgo.setting.clone".equals(current)) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        return;
                    }

                    if (getPackageName().equals(current)) {
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from_remote", true);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {}
}
