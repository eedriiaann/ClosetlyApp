package com.example.armariovirtual.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.models.Outfit;
import com.example.armariovirtual.adapters.MiniClothAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateOutfitActivity extends AppCompatActivity {

    private Map<String, String> selectedSlots = new HashMap<>();
    private String currentSlotTag;
    private String outfitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_outfit);

        outfitId = getIntent().getStringExtra("OUTFIT_ID");
        boolean canEdit = getIntent().getBooleanExtra("CAN_EDIT", true);

        setupButtons();

        if (outfitId != null) {
            loadExistingOutfit(outfitId);
            if (canEdit) {
                ImageButton btnDelete = findViewById(R.id.btnDeleteOutfit);
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> confirmDelete());
            }
        }

        if (!canEdit) {
            findViewById(R.id.btnSaveOutfit).setVisibility(View.GONE);
            disableButtons();
        }
    }

    private void setupButtons() {
        int[] ids = {R.id.btnHead, R.id.btnJacket, R.id.btnShirt, R.id.btnScarf, R.id.btnPants, R.id.btnAcc1, R.id.btnAcc2, R.id.btnShoes, R.id.btnSocks};

        for (int id : ids) {
            findViewById(id).setOnClickListener(v -> {
                currentSlotTag = getResources().getResourceEntryName(v.getId());
                showClothPicker();
            });
        }

        findViewById(R.id.btnSaveOutfit).setOnClickListener(v -> saveOutfit());
    }

    private void showClothPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_select_cloth, null);
        View btnRemove = view.findViewById(R.id.btnRemoveCloth);
        RecyclerView rv = view.findViewById(R.id.rvClothesBottomSheet);
        rv.setLayoutManager(new LinearLayoutManager(this));

        btnRemove.setVisibility(View.VISIBLE);
        btnRemove.setOnClickListener(v -> {
            selectedSlots.remove(currentSlotTag);

            int id = getResources().getIdentifier(currentSlotTag, "id", getPackageName());
            ImageButton btn = findViewById(id);
            if (btn != null) {
                btn.setImageResource(R.drawable.ic_plus);
                btn.setPadding(60, 60, 60, 60);
            }

            dialog.dismiss();
            Toast.makeText(this, "Prenda quitada", Toast.LENGTH_SHORT).show();
        });
        List<Cloth> list = new ArrayList<>();
        MiniClothAdapter adapter = new MiniClothAdapter(list);
        adapter.setOnItemClickListener(cloth -> {
            selectedSlots.put(currentSlotTag, cloth.getId());
            updateButtonImage(currentSlotTag, cloth.getImageUrl());
            dialog.dismiss();
        });

        rv.setAdapter(adapter);
        loadUserClothes(list, adapter);
        dialog.setContentView(view);
        dialog.show();
    }

    private void updateButtonImage(String tag, String url) {
        int id = getResources().getIdentifier(tag, "id", getPackageName());
        ImageButton btn = findViewById(id);
        btn.setPadding(0, 0, 0, 0);
        Glide.with(this).load(url).into(btn);
    }

    private void loadUserClothes(List<Cloth> list, MiniClothAdapter adapter) {
        FirebaseFirestore.getInstance().collection("clothes").whereEqualTo("userId", FirebaseAuth.getInstance().getUid()).get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query.getDocuments()) {
                Cloth c = doc.toObject(Cloth.class);
                if (c != null) {
                    c.setId(doc.getId());
                    list.add(c);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void saveOutfit() {
        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Este outfit esta vacio", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("slots", selectedSlots);
        data.put("timestamp", System.currentTimeMillis());

        if (outfitId != null) {
            FirebaseFirestore.getInstance().collection("outfits").document(outfitId).set(data).addOnSuccessListener(v -> finish());
        } else {
            FirebaseFirestore.getInstance().collection("outfits").add(data).addOnSuccessListener(v -> finish());
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle("Eliminar conjunto").setMessage("¿Estás seguro de que quieres borrar este outfit?").setPositiveButton("Eliminar", (d, w) -> {
            FirebaseFirestore.getInstance().collection("outfits").document(outfitId).delete().addOnSuccessListener(v -> finish());
        }).setNegativeButton("Cancelar", null).show();
    }

    private void loadExistingOutfit(String id) {
        FirebaseFirestore.getInstance().collection("outfits").document(id).get().addOnSuccessListener(doc -> {
            Outfit o = doc.toObject(Outfit.class);
            if (o != null && o.getSlots() != null) {
                selectedSlots = o.getSlots();
                for (Map.Entry<String, String> entry : selectedSlots.entrySet()) {
                    fetchAndPopulate(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    private void fetchAndPopulate(String tag, String clothId) {
        FirebaseFirestore.getInstance().collection("clothes").document(clothId).get().addOnSuccessListener(d -> {
            Cloth c = d.toObject(Cloth.class);
            if (c != null) updateButtonImage(tag, c.getImageUrl());
        });
    }

    private void disableButtons() {
        int[] ids = {R.id.btnHead, R.id.btnJacket, R.id.btnShirt, R.id.btnScarf, R.id.btnPants, R.id.btnAcc1, R.id.btnAcc2, R.id.btnShoes, R.id.btnSocks};
        for (int id : ids) findViewById(id).setEnabled(false);
    }
}