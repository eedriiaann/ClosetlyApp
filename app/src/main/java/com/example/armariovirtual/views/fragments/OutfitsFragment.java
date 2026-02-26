package com.example.armariovirtual.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Outfit;
import com.example.armariovirtual.adapters.OutfitAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OutfitsFragment extends Fragment {

    private RecyclerView recyclerAtuendos;
    private OutfitAdapter adapter;
    private List<Outfit> outfitList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_outfits_fragment, container, false);

        recyclerAtuendos = root.findViewById(R.id.recyclerAtuendos);
        recyclerAtuendos.setLayoutManager(new LinearLayoutManager(getContext()));

        outfitList = new ArrayList<>();
        adapter = new OutfitAdapter(outfitList);
        recyclerAtuendos.setAdapter(adapter);

        loadUserOutfits();

        return root;
    }

    private void loadUserOutfits() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("outfits").whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error escuchando cambios", error);
                return;
            }

            if (value != null) {
                outfitList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Outfit outfit = doc.toObject(Outfit.class);
                    if (outfit != null) {
                        outfit.setId(doc.getId());
                        outfitList.add(outfit);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}