package com.example.armariovirtual.views.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.utils.ClothUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

public class UploadClothDetailsActivity extends AppCompatActivity {

    private Uri imageUri;
    private ImageView imagePreview;
    private EditText etNombre, etDescripcion, etMarca, etPrecio, etNuevaEtiqueta;
    private AutoCompleteTextView actvCategoria, actvTalla, actvTemporada;
    private ChipGroup chipGroupTags;
    private Button btnSubirPrenda, btnAddTag;
    private ProgressBar progressBar;

    private List<String> tagsList = new ArrayList<>();

    private final String[] categorias = {"Camisetas", "Pantalones", "Vestidos", "Faldas", "Chaquetas", "Abrigos", "Sudaderas", "Jerseys", "Camisas", "Zapatos", "Accesorios", "Ropa interior", "Trajes", "Baño", "Deporte"};
    private final String[] tallas = {"XS", "S", "M", "L", "XL", "XXL", "Única"};
    private final String[] temporadas = {"Primavera", "Verano", "Otoño", "Invierno", "Todo el año"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "No se pudo inicializar OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV inicializado correctamente");
        }

        setContentView(R.layout.home_clothes_upload_fragment);

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
        }

        initViews();
        setupDropdowns();
        loadImagePreview();
        setupListeners();
    }

    private void initViews() {
        imagePreview = findViewById(R.id.imagePreview);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etMarca = findViewById(R.id.etMarca);
        etPrecio = findViewById(R.id.etPrecio);
        etNuevaEtiqueta = findViewById(R.id.etNuevaEtiqueta);
        actvCategoria = findViewById(R.id.actvCategoria);
        actvTalla = findViewById(R.id.actvTalla);
        actvTemporada = findViewById(R.id.actvTemporada);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnSubirPrenda = findViewById(R.id.btnSubirPrenda);
        btnAddTag = findViewById(R.id.btnAddTag);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categorias);
        actvCategoria.setAdapter(catAdapter);

        ArrayAdapter<String> tallaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tallas);
        actvTalla.setAdapter(tallaAdapter);

        ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, temporadas);
        actvTemporada.setAdapter(tempAdapter);

        actvCategoria.setText(categorias[0], false);
        actvTalla.setText(tallas[2], false);
        actvTemporada.setText(temporadas[4], false);
    }

    private void loadImagePreview() {
        if (imageUri != null) {
            Glide.with(this).load(imageUri).placeholder(R.drawable.ic_profile_placeholder).into(imagePreview);
        }
    }

    private void setupListeners() {
        btnAddTag.setOnClickListener(v -> addTag());
        btnSubirPrenda.setOnClickListener(v -> validateAndUpload());
        actvCategoria.setOnClickListener(v -> actvCategoria.showDropDown());
        actvTalla.setOnClickListener(v -> actvTalla.showDropDown());
        actvTemporada.setOnClickListener(v -> actvTemporada.showDropDown());
    }

    private void addTag() {
        String tag = etNuevaEtiqueta.getText().toString().trim();
        if (!tag.isEmpty() && !tagsList.contains(tag)) {
            tagsList.add(tag);
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                chipGroupTags.removeView(chip);
                tagsList.remove(tag);
            });
            chipGroupTags.addView(chip);
            etNuevaEtiqueta.setText("");
        }
    }

    private void validateAndUpload() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        float precio = 0;
        String precioStr = etPrecio.getText().toString().trim();
        if (!precioStr.isEmpty()) {
            try {
                precio = Float.parseFloat(precioStr);
            } catch (Exception e) {
                return;
            }
        }

        processAndUploadCloth(nombre, etDescripcion.getText().toString().trim(), actvCategoria.getText().toString(), etMarca.getText().toString(), precio, actvTalla.getText().toString(), actvTemporada.getText().toString());
    }

    private void processAndUploadCloth(String nombre, String desc, String cat, String marca, float precio, String talla, String temp) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubirPrenda.setEnabled(false);

        new Thread(() -> {
            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap processedBitmap = ClothUtils.removeBackground(original);
                Uri processedUri = ClothUtils.saveBitmapToUri(this, processedBitmap);

                runOnUiThread(() -> {
                    ClothUtils.uploadClothToFirebase(this, processedUri, nombre, desc, cat, marca, precio, talla, temp, tagsList, new ClothUtils.UploadCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UploadClothDetailsActivity.this, "¡Prenda subida!", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            btnSubirPrenda.setEnabled(true);
                            Toast.makeText(UploadClothDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubirPrenda.setEnabled(true);
                });
            }
        }).start();
    }
}