package com.example.armariovirtual.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.armariovirtual.R;
import com.example.armariovirtual.views.activity.UploadClothDetailsActivity;
import com.example.armariovirtual.views.activity.UploadPhotoDetailsActivity;
import com.example.armariovirtual.views.activity.CreateOutfitActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UploadOptionsBottomSheet extends BottomSheetDialogFragment {

    private ActivityResultLauncher<Intent> pickVideo;
    private ActivityResultLauncher<Intent> pickImage;
    private ActivityResultLauncher<Intent> pickCloth;

    //no necesito uno de estos para los outfits porque no necesito archivos del usuario ya que uso las prendas ya subidas

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.home_upload_options_bottom_sheet, container, false);

        Button btnSubirFoto = root.findViewById(R.id.btn_subir_foto);
        Button btnSubirPrenda = root.findViewById(R.id.btn_subir_prenda);
        Button btnCrearOutfit = root.findViewById(R.id.btn_crear_outfit);


//
//        pickVideo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                Uri fileUri = result.getData().getData();
//                if (fileUri != null) {
//                    Toast.makeText(getContext(), "Subiendo vídeo...", Toast.LENGTH_SHORT).show();
//                    VideoUtils.uploadVideo(fileUri, null, new VideoUtils.UploadCallback() {
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(getContext(), "Vídeo subido correctamente", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailure(String errorMessage) {
//                            Toast.makeText(getContext(), "Error al subir vídeo: " + errorMessage, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        });

        pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri fileUri = result.getData().getData();
                if (fileUri != null) {
                    Intent intent = new Intent(getContext(), UploadPhotoDetailsActivity.class);
                    intent.putExtra("imageUri", fileUri.toString());
                    startActivity(intent);
                    dismiss();
                }
            }
        });

        pickCloth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri fileUri = result.getData().getData();
                if (fileUri != null) {
                    Intent intent = new Intent(getContext(), UploadClothDetailsActivity.class);
                    intent.putExtra("imageUri", fileUri.toString());
                    startActivity(intent);
                    dismiss();
                }
            }
        });

        btnSubirFoto.setOnClickListener(v -> openGalleryForImage());

        btnSubirPrenda.setOnClickListener(v -> openGalleryForCloth());

        btnCrearOutfit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateOutfitActivity.class);
            startActivity(intent);
            dismiss();
        });

        return root;
    }

    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImage.launch(Intent.createChooser(intent, "Selecciona una foto"));
    }

    private void openGalleryForCloth() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickCloth.launch(Intent.createChooser(intent, "Selecciona una prenda"));
    }
}