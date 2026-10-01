package com.jmgo.pinlock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private StringBuilder enteredPin = new StringBuilder();
    private String tempSetupPin = null;

    private TextView statusText;
    private TextView pinDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableJmgoService();

        prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);

        boolean fromRemote = getIntent().getBooleanExtra("from_remote", false);

        if (prefs.contains("saved_pin") && !fromRemote && SystemClock.uptimeMillis() < 30000) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        pinDisplay = findViewById(R.id.pinDisplay);

        updatePrompt();
        setupButtons();
    }

    private void enableJmgoService() {
        try {
            Intent intent = new Intent("action.jmgo.request.accessibility.service");
            intent.putExtra("compontentNameStr", getPackageName() + "/" + KeyService.class.getName());
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
        } catch (Exception ignored) {}
    }

    private void updatePrompt() {
        if (!prefs.contains("saved_pin")) {
            if (tempSetupPin == null) {
                statusText.setText("Задайте 4-значный PIN");
            } else {
                statusText.setText("Повторите PIN");
            }
        } else {
            statusText.setText("Введите PIN");
        }
        renderDots();
    }

    private void setupButtons() {
        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

        for (int id : numIds) {
            Button b = findViewById(id);
            b.setOnClickListener(v -> {
                if (enteredPin.length() < 4) {
                    enteredPin.append(b.getText().toString());
                    renderDots();
                }
                if (enteredPin.length() == 4) {
                    handlePin(enteredPin.toString());
                    enteredPin.setLength(0);
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
            if (i < enteredPin.length()) dots.append("● ");
            else dots.append("_ ");
        }
        pinDisplay.setText(dots.toString().trim());
    }

    private void handlePin(String pin) {
        if (!prefs.contains("saved_pin")) {
            if (tempSetupPin == null) {
                tempSetupPin = pin;
                updatePrompt();
            } else {
                if (tempSetupPin.equals(pin)) {
                    prefs.edit().putString("saved_pin", pin).apply();
                    Toast.makeText(this, "Пароль сохранен!", Toast.LENGTH_SHORT).show();
                    openRussianSettings();
                } else {
                    Toast.makeText(this, "Не совпало, попробуйте снова", Toast.LENGTH_SHORT).show();
                    tempSetupPin = null;
                    updatePrompt();
                }
            }
        } else {
            if (prefs.getString("saved_pin", "").equals(pin)) {
                openRussianSettings();
            } else {
                Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                renderDots();
            }
        }
    }

    private void openRussianSettings() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.jmgo.setting.clone");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception ignored) {}
        finish();
    }
}
