package com.projector.pinlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class KeyService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Ничего не сохраняем в переменные, чтобы они не зависали!
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 605) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                handleSettingsButton();
            }
            return true; // Всегда поглощаем клавишу 605
        }
        return super.onKeyEvent(event);
    }

    private void handleSettingsButton() {
        try {
            // В реальном времени проверяем, ЧТО СЕЙЧАС отображается на экране
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                CharSequence pkg = root.getPackageName();
                if (pkg != null) {
                    String current = pkg.toString();

                    // 1. Если русские настройки УЖЕ на экране прямо сейчас — закрываем их
                    if ("com.jmgo.setting.clone".equals(current)) {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        return;
                    }

                    // 2. Если уже открыт наш экран ввода PIN — ничего не делаем
                    if (getPackageName().equals(current)) {
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 3. Во всех остальных случаях открываем экран ввода пароля
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {}
}
