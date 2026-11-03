package com.jmgo.pinlock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String MASTER_PIN = "3270";
    private static final String SETTINGS_PACKAGE = "com.jmgo.setting.clone";

    private SharedPreferences prefs;
    private final StringBuilder enteredPin = new StringBuilder();
    private String tempSetupPin;
    private TextView statusText;
    private TextView pinDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestJmgoAccessibilityService();
        prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        pinDisplay = findViewById(R.id.pinDisplay);
        updatePrompt();
        setupButtons();
    }

    private void requestJmgoAccessibilityService() {
        try {
            Intent intent = new Intent("action.jmgo.request.accessibility.service");
            intent.putExtra("compontentNameStr", getPackageName() + "/" + KeyService.class.getName());
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
        } catch (Exception ignored) {
            // На прошивках, не поддерживающих этот JMGO broadcast, службу включают вручную.
        }
    }

    private void updatePrompt() {
        if (!prefs.contains("saved_pin")) {
            statusText.setText(tempSetupPin == null ? "Задайте 4-значный PIN" : "Повторите PIN");
        } else {
            statusText.setText("Введите PIN");
        }
        renderDots();
    }

    private void setupButtons() {
        int[] numIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int id : numIds) {
            Button button = findViewById(id);
            button.setOnClickListener(v -> {
                if (enteredPin.length() < 4) {
                    enteredPin.append(button.getText().toString());
                    renderDots();
                }
                if (enteredPin.length() == 4) {
                    String pin = enteredPin.toString();
                    enteredPin.setLength(0);
                    handlePin(pin);
                }
            });
        }

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            enteredPin.setLength(0);
            renderDots();
        });
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void renderDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            dots.append(i < enteredPin.length() ? "● " : "_ ");
        }
        pinDisplay.setText(dots.toString().trim());
    }

    private void handlePin(String pin) {
        // Мастер-код работает как при первом запуске, так и после установки личного PIN.
        if (MASTER_PIN.equals(pin)) {
            Toast.makeText(this, "Мастер-код принят", Toast.LENGTH_SHORT).show();
            openJmgoSettings();
            return;
        }

        if (!prefs.contains("saved_pin")) {
            if (tempSetupPin == null) {
                tempSetupPin = pin;
                updatePrompt();
                return;
            }
            if (tempSetupPin.equals(pin)) {
                prefs.edit().putString("saved_pin", pin).apply();
                Toast.makeText(this, "PIN сохранён", Toast.LENGTH_SHORT).show();
                openJmgoSettings();
            } else {
                Toast.makeText(this, "PIN не совпал. Повторите установку", Toast.LENGTH_SHORT).show();
                tempSetupPin = null;
                updatePrompt();
            }
            return;
        }

        if (prefs.getString("saved_pin", "").equals(pin)) {
            openJmgoSettings();
        } else {
            Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
            renderDots();
        }
    }

    private void openJmgoSettings() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(SETTINGS_PACKAGE);
            if (intent == null) {
                Toast.makeText(this, "Не найдено приложение настроек JMGO", Toast.LENGTH_LONG).show();
                return;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть настройки JMGO", Toast.LENGTH_LONG).show();
        }
    }
}
