package com.example.armariovirtual.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;
import com.example.armariovirtual.adapters.ProfileAdapter;
import com.example.armariovirtual.models.Publication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PublicationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<Publication> publicaciones;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_clothes_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerPrendas);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        publicaciones = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), publicaciones);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        listenPublications();

        return view;
    }

    private void listenPublications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listenerRegistration = db.collection("images").whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((querySnapshot, e) -> {
            if (e != null || querySnapshot == null) return;

            publicaciones.clear();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Publication p = snapshotToPublication(doc);
                publicaciones.add(p);
            }

            adapter.notifyDataSetChanged();
        });
    }

    private Publication snapshotToPublication(DocumentSnapshot doc) {
        Publication p = new Publication();
        p.setId(doc.getId());
        p.setName(doc.getString("name"));
        p.setDescripcion(doc.getString("descripcion"));
        p.setImageUrl(doc.getString("imageUrl"));
        p.setPfpUrl(doc.getString("pfpUrl"));
        Long megusta = doc.getLong("megusta");
        if (megusta != null) {
            p.setMegusta(megusta.intValue());
        } else {
            p.setMegusta(0);
        }
        return p;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
