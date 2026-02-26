package com.example.armariovirtual.services.auth;

import com.example.armariovirtual.views.activity.SplashActivity;
import com.example.armariovirtual.database.AuthDB;

public class SplashService {
    private final AuthDB repository;
    private final SplashActivity view;

    public SplashService(SplashActivity view) {
        this.view = view;
        this.repository = new AuthDB();
    }

    public void checkSessionAndNavigate() {
        view.runDelayed(() -> {
            if (repository.isUserLoggedIn()) {
                view.navigateToHome();
            } else {
                view.navigateToLogin();
            }
        }, 500);
    }
}
