package com.example.armariovirtual.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;
import com.example.armariovirtual.adapters.ClothAdapter;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.services.clothes.ClothService;
import com.example.armariovirtual.views.activity.ClothesDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class ClothesFragment extends Fragment {

    private RecyclerView recyclerViewPrendas;
    private ClothAdapter adapter;
    private List<Cloth> prendas;
    private ClothService controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_clothes_fragment, container, false);

        recyclerViewPrendas = view.findViewById(R.id.recyclerPrendas);
        recyclerViewPrendas.setLayoutManager(new GridLayoutManager(getContext(), 3));
        prendas = new ArrayList<>();
        adapter = new ClothAdapter(getContext(), prendas);
        recyclerViewPrendas.setAdapter(adapter);

        controller = new ClothService(this);

        adapter.setOnClothClickListener(cloth -> {
            Intent intent = new Intent(getActivity(), ClothesDetailsActivity.class);
            intent.putExtra("CLOTH_ID", cloth.getId());
            startActivity(intent);
        });

        controller.startListening();
        return view;
    }


    public void updateList(List<Cloth> newClothes) {
        prendas.clear();
        prendas.addAll(newClothes);
        adapter.notifyDataSetChanged();
    }

    public void showError(String message) {
        Log.e("ClothesFragment", message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        controller.stopListening();
    }
}
