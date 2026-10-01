package com.projector.pinlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class KeyService extends AccessibilityService {

    private String currentPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Отслеживаем, какое приложение сейчас открыто на экране
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                currentPackage = event.getPackageName().toString();
            }
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 605) {
            // 1. Если русские настройки УЖЕ открыты — закрываем их по нажатию (кнопка назад), не прося пароль
            if ("com.jmgo.setting.clone".equals(currentPackage)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
                return true;
            }

            // 2. Если уже открыт наш экран ввода PIN — игнорируем повторные нажатия
            if (getPackageName().equals(currentPackage)) {
                return true;
            }

            // 3. В остальных случаях — показываем окно ввода PIN-кода
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {}
}
