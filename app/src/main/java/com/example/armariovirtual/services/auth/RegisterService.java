package com.example.armariovirtual.services.auth;

import com.example.armariovirtual.views.activity.RegisterActivity;
import com.example.armariovirtual.database.AuthDB;

public class RegisterService {
    private final AuthDB repository;
    private final RegisterActivity view;

    public RegisterService(RegisterActivity view) {
        this.view = view;
        this.repository = new AuthDB();
    }

    public void processRegistration(String email, String user, String name, String pass, String confirm) {
        if (email.isEmpty() || user.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            view.showErrorMessage("Rellena todos los campos");
            return;
        }

        if (!pass.equals(confirm)) {
            view.showErrorMessage("Las contraseñas no coinciden");
            return;
        }

        if (pass.length() < 6) {
            view.showErrorMessage("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        repository.registerUser(email, pass, user, name, new AuthDB.AuthCallback() {
            @Override
            public void onSuccess() {
                view.onRegisterSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                view.showErrorMessage(errorMessage);
            }
        });
    }
}