package com.projector.pinlock;

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

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
                // При пробуждении из сна отправляем команду системе JMGO серией повторов
                reEnableService();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(screenReceiver);
        } catch (Exception ignored) {}
    }

    private void reEnableService() {
        Handler handler = new Handler(Looper.getMainLooper());
        int[] delays = {500, 2000, 5000};
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
            // ФИЛЬТР: Первые 25 секунд после старта проектора игнорируем любые ложные сигналы
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

                    // Если русские настройки уже открыты - закрываем их
                    if ("com.jmgo.setting.clone".equals(current)) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        return;
                    }

                    // Если уже открыт ввод пароля - игнорируем
                    if (getPackageName().equals(current)) {
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Открываем экран ввода пароля с пометкой, что это физическая кнопка
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from_remote", true);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {}
}
