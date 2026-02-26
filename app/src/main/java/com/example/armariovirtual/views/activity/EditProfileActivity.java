package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.utils.Utils;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private Intent intent;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri imageUri;
    private ImageView imgProfile;
    private String currentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_edit_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgProfile = findViewById(R.id.imgProfile);
        EditText etChangeName = findViewById(R.id.etChangeName);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentName = doc.getString("name");
                    if (currentName != null) {
                        etChangeName.setText(currentName);
                    }

                    String profileImageUrl = doc.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(imgProfile);
                    }
                }
            });
        }

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                if (imageUri != null) {
                    imgProfile.setImageURI(imageUri);
                    uploadProfileImage(imageUri);
                }
            }
        });
    }

    public void bGoSettings(View view) {
        Utils.goBack(this);
    }

    public void selectProfileImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen"));
    }

    private void uploadProfileImage(Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        StorageReference storageRef = storage.getReference().child("profile_images/" + uid + ".jpg");

        storageRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> saveProfileImageUrl(uri.toString()))).addOnFailureListener(e -> Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
    }

    private void saveProfileImageUrl(String downloadUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).update("profileImageUrl", downloadUrl).addOnSuccessListener(aVoid -> Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void bConfirmChanges(View view) {
        EditText etCurrent = findViewById(R.id.etPasswordActual);
        EditText etNew = findViewById(R.id.etPasswordNew);
        EditText etConfirm = findViewById(R.id.etPasswordConfirm);
        String current = etCurrent.getText().toString().trim();
        String newPass = etNew.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        EditText etName = findViewById(R.id.etChangeName);
        String newName = etName.getText().toString().trim();
        if (!newName.equals(currentName)) {
            changeName();
        }
        if (!current.isEmpty() && !newPass.isEmpty() && !confirm.isEmpty()) {
            changePassword();
        }
    }

    public void changePassword() {
        EditText etCurrent = findViewById(R.id.etPasswordActual);
        EditText etNew = findViewById(R.id.etPasswordNew);
        EditText etConfirm = findViewById(R.id.etPasswordConfirm);

        String currentPassword = etCurrent.getText().toString().trim();
        String newPassword = etNew.getText().toString().trim();
        String confirmPassword = etConfirm.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al actualizar: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    public void changeName() {
        EditText etName = findViewById(R.id.etChangeName);
        String newName = etName.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre válido", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).update("name", newName).addOnSuccessListener(aVoid -> Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


}