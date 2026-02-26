package com.example.armariovirtual.views.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.armariovirtual.R;
import com.example.armariovirtual.databinding.HomeMainActivityBinding;
import com.example.armariovirtual.utils.UploadOptionsBottomSheet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private HomeMainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = HomeMainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);


        NavigationUI.setupWithNavController(navView, navController);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_upload) {
                UploadOptionsBottomSheet bottomSheet = new UploadOptionsBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "UploadOptionsBottomSheet");
                return false;
            } else {
                NavigationUI.onNavDestinationSelected(item, navController);
                return true;
            }
        });
    }
}
