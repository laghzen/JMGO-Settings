package com.projector.pinlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class KeyService extends AccessibilityService {

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // Ловим клавишу 605
        if (event.getKeyCode() == 605) {
            // Запускаем окно с паролем только в момент первого нажатия вниз
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            
            // ВАЖНО: возвращаем true ВСЕГДА для клавиши 605 (и для DOWN, и для UP, и для зажатий)
            // Это полностью глушит сигнал, и система проектора вообще не узнает, что кнопка нажималась!
            return true; 
        }
        
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
