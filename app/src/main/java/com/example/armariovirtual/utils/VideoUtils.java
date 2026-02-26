package com.example.armariovirtual.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class VideoUtils {

    public interface UploadCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    //      Este metodo era el que subia el video a la BD pero he decido posponer esta funcion ya que era extremadamente ineficiente la carga de los reels
    //      y para que tenga la posibilidad de explotarte prefiero posponerlo para cuando ya trabaje yo por mi cuenta
    //      (Aunque probablemente deseche un monton de codigo porque voy a recodear todo pero de forma mas ordenada porque es un caos)
    public static void uploadVideo(Uri fileUri, @Nullable String descripcion, UploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://closetlyapp-176d8.firebasestorage.app");
        StorageReference storageRef = storage.getReference().child("videos/" + System.currentTimeMillis() + ".mp4");

        storageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            saveMetadata(uri.toString(), descripcion);
            callback.onSuccess();
        })).addOnFailureListener(e -> {
            Log.e("VideoUploadUtils", "Error subiendo video: " + e.getMessage());
            callback.onFailure(e.getMessage());
        });
    }

    private static void saveMetadata(String downloadUrl, @Nullable String descripcion) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            String username = doc.getString("username");
            if (username == null) username = "@username";

            Map<String, Object> data = new HashMap<>();
            data.put("videoUrl", downloadUrl);
            data.put("descripcion", descripcion != null ? descripcion : "");
            data.put("timestamp", System.currentTimeMillis());
            data.put("userId", uid);
            data.put("username", username);
            data.put("megusta", 0);

            db.collection("videos").add(data);
            Log.d("VideoUploadUtils", "Video subido y metadatos guardados.");
        }).addOnFailureListener(e -> Log.e("VideoUploadUtils", "Error obteniendo usuario: " + e.getMessage()));
    }
}
