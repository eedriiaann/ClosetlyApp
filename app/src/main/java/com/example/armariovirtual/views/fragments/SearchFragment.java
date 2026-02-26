package com.example.armariovirtual.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.armariovirtual.adapters.UserAdapter;
import com.example.armariovirtual.databinding.SearchFragmentBinding;
import com.example.armariovirtual.models.User;
import com.example.armariovirtual.views.activity.ProfileUserActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchFragmentBinding binding;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SearchFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(userList, user -> {
            Intent intent = new Intent(getActivity(), ProfileUserActivity.class);
            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("USER_NAME", user.getName());
            intent.putExtra("USER_PHOTO_URL", user.getProfileImageUrl());
            startActivity(intent);
        });
        binding.recyclerView.setAdapter(adapter);

        binding.searchView.onActionViewExpanded();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    searchUsers(newText);
                }
                return true;
            }
        });

        return view;
    }

    private void searchUsers(String text) {
        db.collection("users").orderBy("username").startAt(text).endAt(text + "\uf8ff").limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            userList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setId(doc.getId());
                    userList.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}