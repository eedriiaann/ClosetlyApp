package com.example.armariovirtual.services.clothes;

import com.example.armariovirtual.database.ClothDB;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.views.activity.ClothesDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class ClothDetailsService {
    private final ClothDB repository;
    private final ClothesDetailsActivity view;
    private final String currentUserId;

    public ClothDetailsService(ClothesDetailsActivity view) {
        this.view = view;
        this.repository = new ClothDB();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadDetails(String clothId) {
        view.showLoading();
        repository.getClothDetails(clothId, new ClothDB.ClothDetailCallback() {
            @Override
            public void onClothLoaded(Cloth cloth) {
                view.hideLoading();
                view.displayCloth(cloth);
                view.setOwnerActionsVisible(cloth.getUserId().equals(currentUserId));
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }

    public void deleteCloth(String clothId) {
        repository.deleteCloth(clothId, new ClothDB.ActionCallback() {
            @Override
            public void onSuccess() {
                view.onActionSuccess("Prenda eliminada");
                view.finish();
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }

    public void updateCloth(String clothId, Map<String, Object> updates) {
        repository.updateCloth(clothId, updates, new ClothDB.ActionCallback() {
            @Override
            public void onSuccess() {
                view.onActionSuccess("Prenda actualizada");
                loadDetails(clothId);
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }
}
