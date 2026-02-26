package com.example.armariovirtual.database;

import com.example.armariovirtual.models.Cloth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClothDB {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface ClothesListener {
        void onClothesUpdated(List<Cloth> clothes);

        void onError(String error);
    }

    public ListenerRegistration listenToUserClothes(ClothesListener listener) {
        String uid = mAuth.getCurrentUser().getUid();

        return db.collection("clothes").whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                listener.onError(e.getMessage());
                return;
            }

            List<Cloth> clothes = new ArrayList<>();
            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    clothes.add(snapshotToCloth(doc));
                }
            }
            listener.onClothesUpdated(clothes);
        });
    }

    private Cloth snapshotToCloth(DocumentSnapshot doc) {
        Cloth c = doc.toObject(Cloth.class);
        if (c != null) {
            c.setId(doc.getId());
        }
        return c;
    }

    public interface ClothDetailCallback {
        void onClothLoaded(Cloth cloth);

        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();

        void onError(String error);
    }

    public void getClothDetails(String clothId, ClothDetailCallback callback) {
        db.collection("clothes").document(clothId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Cloth c = doc.toObject(Cloth.class);
                if (c != null) c.setId(doc.getId());
                callback.onClothLoaded(c);
            } else {
                callback.onError("La prenda no existe");
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCloth(String clothId, Map<String, Object> updates, ActionCallback callback) {
        db.collection("clothes").document(clothId).update(updates).addOnSuccessListener(aVoid -> callback.onSuccess()).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteCloth(String clothId, ActionCallback callback) {
        db.collection("clothes").document(clothId).delete().addOnSuccessListener(aVoid -> callback.onSuccess()).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}