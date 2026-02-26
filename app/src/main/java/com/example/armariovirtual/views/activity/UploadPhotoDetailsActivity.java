package com.example.armariovirtual.views.activity;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.utils.PublicationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UploadPhotoDetailsActivity extends AppCompatActivity {

    private Uri imageUri;
    private List<Cloth> userClothes = new ArrayList<>();
    private List<String> selectedClothIds = new ArrayList<>();

    // Añadimos la referencia al ProgressBar
    private ProgressBar progressBar;
    private Button btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_publication_upload_fragment);

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
        }

        initViews();

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into((ImageView) findViewById(R.id.photo_view));
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        btnUpload = findViewById(R.id.btn_upload);
        EditText edtDescripcion = findViewById(R.id.edt_descripcion);
        Button btnSelectClothes = findViewById(R.id.btn_select_clothes);

        btnSelectClothes.setOnClickListener(v -> showClothSelectionDialog());

        btnUpload.setOnClickListener(v -> {
            String descripcion = edtDescripcion.getText().toString().trim();
            if (descripcion.isEmpty()) {
                Toast.makeText(this, "Escribe una descripción", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- INICIO DE CARGA ---
            showLoading();

            PublicationUtils.uploadPhoto(imageUri, descripcion, selectedClothIds, new PublicationUtils.UploadCallback() {
                @Override
                public void onSuccess() {
                    // --- FIN DE CARGA (ÉXITO) ---
                    hideLoading();
                    Toast.makeText(UploadPhotoDetailsActivity.this, "Publicación subida", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // --- FIN DE CARGA (ERROR) ---
                    hideLoading();
                    Toast.makeText(UploadPhotoDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Métodos solicitados
    public void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnUpload != null) btnUpload.setEnabled(false); // Desactivamos el botón para evitar doble clic
    }

    public void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (btnUpload != null) btnUpload.setEnabled(true);
    }

    private void showClothSelectionDialog() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        FirebaseFirestore.getInstance().collection("clothes")
                .whereEqualTo("userId", currentUserId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    userClothes.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Cloth cloth = doc.toObject(Cloth.class);
                        if (cloth != null) {
                            cloth.setId(doc.getId());
                            userClothes.add(cloth);
                        }
                    }

                    if (userClothes.isEmpty()) {
                        Toast.makeText(this, "No tienes prendas registradas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] names = new String[userClothes.size()];
                    boolean[] checkedItems = new boolean[userClothes.size()];

                    for (int i = 0; i < userClothes.size(); i++) {
                        names[i] = userClothes.get(i).getName();
                        checkedItems[i] = selectedClothIds.contains(userClothes.get(i).getId());
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("¿Qué prendas llevas?")
                            .setMultiChoiceItems(names, checkedItems, (dialog, which, isChecked) -> {
                                String clothId = userClothes.get(which).getId();
                                if (isChecked) {
                                    if (!selectedClothIds.contains(clothId)) selectedClothIds.add(clothId);
                                } else {
                                    selectedClothIds.remove(clothId);
                                }
                            })
                            .setPositiveButton("Listo", null)
                            .show();
                });
    }

    // Método para el botón de volver atrás del XML
    public void goBack(View view) {
        finish();
    }
}