package com.example.armariovirtual.utils;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicationUtils {


    public interface UploadCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public static void showPublicationMenu(FirebaseUser currentUser, Publication publication, View anchorView) {
        Context context = anchorView.getContext();
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_publicacion, popup.getMenu());

        if (currentUser == null) {
            popup.getMenu().findItem(R.id.opcion_editar).setVisible(false);
            popup.getMenu().findItem(R.id.opcion_eliminar).setVisible(false);
            popup.show();
            return;
        }

        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            String role = snapshot.getString("role");
            boolean isAdmin = "admin".equals(role);
            boolean isOwner = uid.equals(publication.getUserId());

            popup.getMenu().findItem(R.id.opcion_editar).setVisible(isAdmin || isOwner);
            popup.getMenu().findItem(R.id.opcion_eliminar).setVisible(isAdmin || isOwner);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.opcion_editar) {
                    modifyPublication(context, publication.getId(), publication.getImageUrl());
                    return true;
                } else if (id == R.id.opcion_eliminar) {
                    deletePublication(context, publication.getId(), publication.getImageUrl());
                    return true;
                }
                return false;
            });

            popup.show();
        }).addOnFailureListener(e -> {
            popup.getMenu().findItem(R.id.opcion_editar).setVisible(false);
            popup.getMenu().findItem(R.id.opcion_eliminar).setVisible(false);
            popup.show();
        });
    }

    public static void modifyPublication(Context context, String publicationId, String imageUrl) {
        Toast.makeText(context, "Aún no está implementada esta función", Toast.LENGTH_SHORT).show();
    }

    public static void deletePublication(Context context, String publicationId, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);
            storageRef.delete().addOnSuccessListener(aVoid -> {
                db.collection("images").document(publicationId).delete().addOnSuccessListener(aVoid1 -> Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> {
                db.collection("images").document(publicationId).delete();
            });
        } else {
            db.collection("images").document(publicationId).delete();
        }
    }

    public static void uploadPhoto(Uri imageUri, String descripcion, List<String> clothIds, UploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");
        List<String> validClothIds = new ArrayList<>();
        if (clothIds != null) {
            for (String id : clothIds) {
                if (id != null && !id.isEmpty()) {
                    validClothIds.add(id);
                }
            }
        }

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                String userId = FirebaseAuth.getInstance().getUid();
                long timestamp = System.currentTimeMillis();

                Map<String, Object> publication = new HashMap<>();
                publication.put("userId", userId);
                publication.put("imageUrl", imageUrl);
                publication.put("descripcion", descripcion);
                publication.put("timestamp", timestamp);
                publication.put("megusta", 0);
                publication.put("clothIds", validClothIds);

                FirebaseFirestore.getInstance().collection("images").add(publication).addOnSuccessListener(documentReference -> callback.onSuccess()).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            });
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

}