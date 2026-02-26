package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.armariovirtual.R;
import com.example.armariovirtual.services.auth.SplashService;

public class SplashActivity extends AppCompatActivity {
    private SplashService controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_splash_activivity);

        controller = new SplashService(this);
        controller.checkSessionAndNavigate();
    }

    public void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void runDelayed(Runnable action, int delay) {
        findViewById(R.id.main).postDelayed(action, delay);
    }
}
