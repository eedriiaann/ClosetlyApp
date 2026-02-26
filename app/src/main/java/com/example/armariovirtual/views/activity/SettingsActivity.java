package com.example.armariovirtual.views.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.armariovirtual.R;
import com.example.armariovirtual.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_settings_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void bSettingsGoBack(View view) {
        Utils.goBack(this);
    }

    public void bSettingsGoEditProfile(View view) {
        intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    public void bSettingsGoFavs(View view) {
        intent = new Intent(SettingsActivity.this, LikedPublicationsActivity.class);
        startActivity(intent);
    }

    public void bSettingsGoHelp(View view) {
        intent = new Intent(SettingsActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void bLogOut(View view) {
        Dialog dlgConfirm = new Dialog(view.getContext());
        dlgConfirm.show();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}