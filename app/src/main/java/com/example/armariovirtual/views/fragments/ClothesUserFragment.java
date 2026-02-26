package com.example.armariovirtual.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.adapters.ClothAdapter;
import com.example.armariovirtual.views.activity.ClothesDetailsActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ClothesUserFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private RecyclerView recyclerViewPrendas;
    private ClothAdapter adapter;
    private List<Cloth> prendas;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private String userId;

    public static ClothesUserFragment newInstance(String userId) {
        ClothesUserFragment fragment = new ClothesUserFragment();
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
        View view = inflater.inflate(R.layout.profile_clothes_fragment, container, false);

        recyclerViewPrendas = view.findViewById(R.id.recyclerPrendas);
        recyclerViewPrendas.setLayoutManager(new GridLayoutManager(getContext(), 3));

        prendas = new ArrayList<>();
        adapter = new ClothAdapter(getContext(), prendas);
        recyclerViewPrendas.setAdapter(adapter);

        adapter.setOnClothClickListener(cloth -> {
            Intent intent = new Intent(getActivity(), ClothesDetailsActivity.class);
            intent.putExtra("CLOTH_ID", cloth.getId());
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        startListening();

        return view;
    }

    private void startListening() {
        if (userId == null) return;

        listenerRegistration = db.collection("clothes").whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) {
                Log.e("ClothesUserFragment", "Error cargando prendas", e);
                return;
            }

            prendas.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Cloth cloth = doc.toObject(Cloth.class);
                if (cloth != null) {
                    cloth.setId(doc.getId());
                    prendas.add(cloth);
                }
            }

            adapter.notifyDataSetChanged();
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
