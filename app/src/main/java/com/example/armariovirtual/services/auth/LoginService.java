package com.example.armariovirtual.services.auth;

import com.example.armariovirtual.database.AuthDB;
import com.example.armariovirtual.views.activity.LoginActivity;

public class LoginService {
    private final AuthDB repository;
    private final LoginActivity view;

    public LoginService(LoginActivity view) {
        this.view = view;
        this.repository = new AuthDB();
    }

    public void handleLogin(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            view.showErrorMessage("Rellena todos los campos");
            return;
        }

        repository.login(email, pass, new AuthDB.AuthCallback() {
            @Override
            public void onSuccess() {
                view.onLoginSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                view.showErrorMessage(errorMessage);
            }
        });
    }
}
