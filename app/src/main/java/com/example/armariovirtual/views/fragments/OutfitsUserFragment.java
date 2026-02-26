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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OutfitsUserFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private RecyclerView recyclerAtuendos;
    private OutfitAdapter adapter;
    private List<Outfit> outfitList;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private String userId;

    public static OutfitsUserFragment newInstance(String userId) {
        OutfitsUserFragment fragment = new OutfitsUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_outfits_fragment, container, false);

        recyclerAtuendos = root.findViewById(R.id.recyclerAtuendos);
        recyclerAtuendos.setLayoutManager(new LinearLayoutManager(getContext()));

        outfitList = new ArrayList<>();
        adapter = new OutfitAdapter(outfitList);
        recyclerAtuendos.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        startListening();

        return root;
    }

    private void startListening() {
        if (userId == null) return;
        listenerRegistration = db.collection("outfits").whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("OutfitsUserFragment", "Error escuchando outfits", e);
                return;
            }

            if (snapshots != null) {
                outfitList.clear();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Outfit outfit = doc.toObject(Outfit.class);
                    if (outfit != null) {
                        outfit.setId(doc.getId());
                        outfitList.add(outfit);
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d("OutfitsUserFragment", "Outfits cargados: " + outfitList.size());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}