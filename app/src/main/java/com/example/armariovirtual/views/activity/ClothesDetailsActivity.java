package com.example.armariovirtual.views.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.services.clothes.ClothDetailsService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothesDetailsActivity extends AppCompatActivity {

    private ImageView ivClothImage;
    private TextView tvName, tvDescription, tvCategory, tvBrand, tvPrice, tvSize, tvSeason;
    private ChipGroup chipGroupTags;
    private LinearLayout layoutOwnerActions;
    private ProgressBar progressBar;

    private ClothDetailsService controller;
    private String clothId;
    private Cloth currentCloth;
    private List<String> tagsList = new ArrayList<>();

    private final String[] categories = {"Camisetas", "Pantalones", "Vestidos", "Faldas", "Chaquetas", "Abrigos", "Sudaderas", "Jerseys", "Camisas", "Zapatos", "Accesorios", "Ropa interior", "Trajes", "Baño", "Deporte"};
    private final String[] sizes = {"XS", "S", "M", "L", "XL", "XXL", "Única"};
    private final String[] seasons = {"Primavera", "Verano", "Otoño", "Invierno", "Todo el año"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothes_details);

        clothId = getIntent().getStringExtra("CLOTH_ID");
        if (clothId == null) {
            showError("Error: No se especificó la prenda");
            finish();
            return;
        }

        controller = new ClothDetailsService(this);
        initViews();
        controller.loadDetails(clothId);
    }

    private void initViews() {
        ivClothImage = findViewById(R.id.ivClothImage);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvBrand = findViewById(R.id.tvBrand);
        tvPrice = findViewById(R.id.tvPrice);
        tvSize = findViewById(R.id.tvSize);
        tvSeason = findViewById(R.id.tvSeason);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        layoutOwnerActions = findViewById(R.id.layoutOwnerActions);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnEdit).setOnClickListener(v -> showEditDialog());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmation());
    }


    public void displayCloth(Cloth cloth) {
        this.currentCloth = cloth;
        this.tagsList = new ArrayList<>(cloth.getTags());

        if (cloth.getImageUrl() != null && !cloth.getImageUrl().isEmpty()) {
            Glide.with(this).load(cloth.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).fitCenter().into(ivClothImage);
        }

        tvName.setText(cloth.getName());

        if (cloth.getDescripcion() != null) {
            tvDescription.setText(cloth.getDescripcion());
        } else {
            tvDescription.setText("Sin descripción");
        }

        if (cloth.getCategoria() != null) {
            tvCategory.setText(cloth.getCategoria());
        } else {
            tvCategory.setText("Sin categoría");
        }

        if (cloth.getMarca() != null) {
            tvBrand.setText(cloth.getMarca());
        } else {
            tvBrand.setText("Sin marca");
        }

        tvPrice.setText(String.format("%.2f €", cloth.getPrecio()));

        if (cloth.getTalla() != null) {
            tvSize.setText(cloth.getTalla());
        } else {
            tvSize.setText("No especificada");
        }

        if (cloth.getTemporada() != null) {
            tvSeason.setText(cloth.getTemporada());
        } else {
            tvSeason.setText("Todo el año");
        }


        chipGroupTags.removeAllViews();
        for (String tag : tagsList) {
            addChipToGroup(tag, chipGroupTags, false);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(cloth.getName());
        }
    }

    public void setOwnerActionsVisible(boolean visible) {
        layoutOwnerActions.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    public void onActionSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void showEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_cloth, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDescription = dialogView.findViewById(R.id.etEditDescription);
        EditText etBrand = dialogView.findViewById(R.id.etEditBrand);
        EditText etPrice = dialogView.findViewById(R.id.etEditPrice);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvEditCategory);
        AutoCompleteTextView actvSize = dialogView.findViewById(R.id.actvEditSize);
        AutoCompleteTextView actvSeason = dialogView.findViewById(R.id.actvEditSeason);
        ChipGroup chipGroupEditTags = dialogView.findViewById(R.id.chipGroupEditTags);
        EditText etNewTag = dialogView.findViewById(R.id.etEditNewTag);
        Button btnAddTag = dialogView.findViewById(R.id.btnEditAddTag);


        setupAutoComplete(actvCategory, categories);
        setupAutoComplete(actvSize, sizes);
        setupAutoComplete(actvSeason, seasons);

        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvSize.setOnClickListener(v -> actvSize.showDropDown());
        actvSeason.setOnClickListener(v -> actvSeason.showDropDown());

        setupAutoComplete(actvCategory, categories);
        setupAutoComplete(actvSize, sizes);
        setupAutoComplete(actvSeason, seasons);

        if (currentCloth != null) {
            etName.setText(currentCloth.getName());
            etDescription.setText(currentCloth.getDescripcion());
            etBrand.setText(currentCloth.getMarca());
            etPrice.setText(String.valueOf(currentCloth.getPrecio()));
            actvCategory.setText(currentCloth.getCategoria(), false);
            actvSize.setText(currentCloth.getTalla(), false);
            actvSeason.setText(currentCloth.getTemporada(), false);

            tagsList = new ArrayList<>(currentCloth.getTags());
            for (String tag : tagsList) addChipToGroup(tag, chipGroupEditTags, true);
        }

        btnAddTag.setOnClickListener(v -> {
            String newTag = etNewTag.getText().toString().trim();
            if (!TextUtils.isEmpty(newTag) && !tagsList.contains(newTag)) {
                tagsList.add(newTag);
                addChipToGroup(newTag, chipGroupEditTags, true);
                etNewTag.setText("");
            }
        });

        new MaterialAlertDialogBuilder(this).setTitle(getString(R.string.dialog_edit_cloth_title)).setView(dialogView).setPositiveButton(getString(R.string.save), (dialog, which) -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", etName.getText().toString().trim());
            updates.put("descripcion", etDescription.getText().toString().trim());
            updates.put("marca", etBrand.getText().toString().trim());
            updates.put("precio", Float.parseFloat(etPrice.getText().toString().isEmpty() ? "0" : etPrice.getText().toString()));
            updates.put("categoria", actvCategory.getText().toString());
            updates.put("talla", actvSize.getText().toString());
            updates.put("temporada", actvSeason.getText().toString());
            updates.put("tags", tagsList);

            controller.updateCloth(clothId, updates);
        }).setNegativeButton(getString(R.string.cancel), null).show();
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_delete_cloth_title).setMessage(R.string.dialog_delete_cloth_message).setPositiveButton(R.string.delete, (d, w) -> controller.deleteCloth(clothId)).setNegativeButton(R.string.cancel, null).show();
    }

    private void setupAutoComplete(AutoCompleteTextView actv, String[] items) {
        actv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items));

    }

    private void addChipToGroup(String tag, ChipGroup group, boolean removable) {
        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setCloseIconVisible(removable);
        if (removable) {
            chip.setOnCloseIconClickListener(v -> {
                group.removeView(chip);
                tagsList.remove(tag);
            });
        }
        group.addView(chip);
    }
}