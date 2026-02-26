package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.armariovirtual.R;
import com.example.armariovirtual.services.auth.LoginService;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private LoginService controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_login_activity);

        controller = new LoginService(this);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
    }

    public void bLoginActionPerformed(View view) {
        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPassword.getText().toString().trim();

        controller.handleLogin(email, pass);
    }

    public void onLoginSuccess() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void bGoRegisterActionPerformed(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}