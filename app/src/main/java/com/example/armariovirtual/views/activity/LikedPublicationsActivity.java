package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;
import com.example.armariovirtual.adapters.LikedPublicationAdapter;
import com.example.armariovirtual.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LikedPublicationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LikedPublicationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_publications);

        recyclerView = findViewById(R.id.recyclerViewLiked);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        final List<Publication> publicacionesLikeadas = new ArrayList<>();
        adapter = new LikedPublicationAdapter(publicacionesLikeadas);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(publication -> {
            Intent intent = new Intent(LikedPublicationsActivity.this, PublicationDetailsActivity.class);
            intent.putExtra("publicacionId", publication.getId());
            startActivity(intent);
        });

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("images").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            publicacionesLikeadas.clear();

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                if (doc.exists()) {
                    Map<String, Boolean> likesBy = (Map<String, Boolean>) doc.get("likesBy");
                    if (likesBy != null && likesBy.containsKey(currentUser.getUid())) {
                        Publication pub = doc.toObject(Publication.class);
                        if (pub != null) {
                            pub.setId(doc.getId());
                            publicacionesLikeadas.add(pub);
                        }
                    }
                }
            }

            adapter.setPublicaciones(publicacionesLikeadas);
        });
    }

    public void bGoSettings(View view) {
        Utils.goBack(this);
    }
}
