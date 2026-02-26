package com.example.armariovirtual.services.clothes;

import com.example.armariovirtual.database.ClothDB;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.views.fragments.ClothesFragment;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ClothService {
    private final ClothDB repository;
    private final ClothesFragment view;
    private ListenerRegistration registration;

    public ClothService(ClothesFragment view) {
        this.view = view;
        this.repository = new ClothDB();
    }

    public void startListening() {
        registration = repository.listenToUserClothes(new ClothDB.ClothesListener() {
            @Override
            public void onClothesUpdated(List<Cloth> clothes) {
                view.updateList(clothes);
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
        }
    }
}
