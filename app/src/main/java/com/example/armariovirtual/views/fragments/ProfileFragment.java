package com.example.armariovirtual.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.views.activity.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        View btnSettings = view.findViewById(R.id.btnSettings);

        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                String name = doc.getString("name");
                String username = doc.getString("username");
                String profileImageUrl = doc.getString("profileImageUrl");

                if (name != null) {
                    tvName.setText(name);
                } else {
                    tvName.setText("name_not_found");
                }

                if (username != null) {
                    tvUsername.setText("@" + username);
                } else {
                    tvUsername.setText("@username_not_found");
                }


                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this).load(profileImageUrl).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(imgProfile);
                }
            }
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getContext().getString(R.string.publications));
                    break;
                case 1:
                    tab.setText(getContext().getString(R.string.clothes));
                    break;
                default:
                    tab.setText(getContext().getString(R.string.outfits));
                    break;
            }
        }).attach();

        return view;
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PublicationsFragment();
                case 1:
                    return new ClothesFragment();
                default:
                    return new OutfitsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
