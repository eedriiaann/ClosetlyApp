package com.example.armariovirtual.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;
import com.example.armariovirtual.adapters.PublicationAdapter;
import com.example.armariovirtual.views.activity.ProfileUserActivity;
import com.example.armariovirtual.views.activity.NotificationsActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerHome;
    private PublicationAdapter adapter;
    private List<Publication> publicationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupRecyclerView();

        loadImagesFromFirebase();

        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerHome = view.findViewById(R.id.recyclerHome);

        View btnNotification = view.findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadImagesFromFirebase);
    }

    private void setupRecyclerView() {
        recyclerHome.setLayoutManager(new GridLayoutManager(getContext(), 1));
        publicationList = new ArrayList<>();
        adapter = new PublicationAdapter(publicationList);

        adapter.setOnUserClickListener((userId, userName, userPhotoUrl) -> navigateToUserProfile(userId, userName, userPhotoUrl));

        recyclerHome.setAdapter(adapter);
    }

    private void updateRecycler(List<Publication> list) {
        list.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        publicationList.clear();
        publicationList.addAll(list);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void checkLoadComplete(List<Publication> tempList, int loaded, int total) {
        if (loaded == total) {
            updateRecycler(tempList);
        }
    }

    private void navigateToUserProfile(String userId, String userName, String userPhotoUrl) {
        Intent intent = new Intent(getActivity(), ProfileUserActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_PHOTO_URL", userPhotoUrl);
        startActivity(intent);
    }

    private void loadImagesFromFirebase() {
        swipeRefreshLayout.setRefreshing(true);

        db.collection("images").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();

            if (docs.isEmpty()) {
                publicationList.clear();
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            List<Publication> tempList = new ArrayList<>();
            final int total = docs.size();
            final int[] loaded = {0};

            for (DocumentSnapshot doc : docs) {
                String userId = doc.getString("userId");

                if (userId == null) {
                    checkLoadComplete(tempList, ++loaded[0], total);
                    continue;
                }

                db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    String desc = doc.getString("descripcion");
                    String imageUrl = doc.getString("imageUrl");
                    String pfpUrl = userDoc.getString("profileImageUrl");
                    int likes = doc.contains("megusta") ? doc.getLong("megusta").intValue() : 0;
                    long timestamp = doc.contains("timestamp") ? doc.getLong("timestamp") : 0;

                    List<String> clothIds = (List<String>) doc.get("clothIds");
                    if (clothIds == null) {
                        clothIds = new ArrayList<>();
                    }

                    tempList.add(new Publication(doc.getId(), userId, name, desc, likes, imageUrl, pfpUrl, timestamp, clothIds));

                    checkLoadComplete(tempList, ++loaded[0], total);
                }).addOnFailureListener(e -> checkLoadComplete(tempList, ++loaded[0], total));
            }
        }).addOnFailureListener(e -> swipeRefreshLayout.setRefreshing(false));
    }


}