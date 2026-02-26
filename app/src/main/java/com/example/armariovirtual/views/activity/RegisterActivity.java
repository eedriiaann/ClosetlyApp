package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.armariovirtual.R;
import com.example.armariovirtual.services.auth.RegisterService;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextUsername, editTextName, editTextPassword, editTextConfirmPassword;
    private RegisterService controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_register_activity);

        controller = new RegisterService(this);
        initViews();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
    }

    public void bRegisterActionPerformed(View view) {
        controller.processRegistration(editTextEmail.getText().toString().trim(), editTextUsername.getText().toString().trim(), editTextName.getText().toString().trim(), editTextPassword.getText().toString().trim(), editTextConfirmPassword.getText().toString().trim());
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onRegisterSuccess() {
        Toast.makeText(this, "Te has registrado correctamente", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void bGoLoginActionPerformed(View view) {
        finish();
    }
}