package com.example.armariovirtual.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.views.fragments.OutfitsUserFragment;
import com.example.armariovirtual.views.fragments.PublicationsUserFragment;
import com.example.armariovirtual.views.fragments.ClothesUserFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileUserActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String userId;
    private String userName;
    private String userPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        Intent intent = getIntent();
        userId = intent.getStringExtra("USER_ID");
        userName = intent.getStringExtra("USER_NAME");
        userPhotoUrl = intent.getStringExtra("USER_PHOTO_URL");

        if (userId == null) {
            finish();
            return;
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        ImageView imgProfile = findViewById(R.id.imgProfile);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (userName != null) {
            tvName.setText(userName);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                if (name != null && !name.isEmpty()) {
                    tvName.setText(name);
                }

                String username = doc.getString("username");
                if (username != null && !username.isEmpty()) {
                    tvUsername.setText("@" + username);
                } else {
                    tvUsername.setText("@username_not_found");
                }

                String profileImageUrl = userPhotoUrl;
                if ((profileImageUrl == null || profileImageUrl.isEmpty()) && doc.contains("profileImageUrl")) {
                    profileImageUrl = doc.getString("profileImageUrl");
                }

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this).load(profileImageUrl).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(imgProfile);
                }
            } else {
                if (userName != null) {
                    tvName.setText(userName);
                }
                tvUsername.setText("@usuario");
            }
        }).addOnFailureListener(e -> {
            if (userName != null) {
                tvName.setText(userName);
            }
            tvUsername.setText("@usuario");

            if (userPhotoUrl != null && !userPhotoUrl.isEmpty()) {
                Glide.with(this).load(userPhotoUrl).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(imgProfile);
            }
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, userId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(this.getString(R.string.publications));
                    break;
                case 1:
                    tab.setText(this.getString(R.string.clothes));
                    break;
                case 2:
                    tab.setText(this.getString(R.string.outfits));
                    break;
            }
        }).attach();
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String userId;

        public ViewPagerAdapter(FragmentActivity fragmentActivity, String userId) {
            super(fragmentActivity);
            this.userId = userId;
        }

        @NonNull
        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return PublicationsUserFragment.newInstance(userId);
                case 1:
                    return ClothesUserFragment.newInstance(userId);
                case 2:
                    return OutfitsUserFragment.newInstance(userId);
                default:
                    return PublicationsUserFragment.newInstance(userId);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}