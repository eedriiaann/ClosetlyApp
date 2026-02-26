package com.example.armariovirtual.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthDB {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public interface AuthCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException() != null ? task.getException().getMessage() : "Error login");
            }
        });
    }

    public void registerUser(String email, String password, String username, String name, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        saveUserToFirestore(mAuth.getCurrentUser().getUid(), email, username, name, callback);
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Error registro");
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String username, String name, AuthCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("uid", uid);
        user.put("username", username);
        user.put("name", name);

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error de guardado en BD"));
    }


}