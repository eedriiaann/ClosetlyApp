package com.example.armariovirtual.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.views.activity.CreateOutfitActivity;
import com.example.armariovirtual.models.Outfit;
import com.example.armariovirtual.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {

    private List<Outfit> list;

    public OutfitAdapter(List<Outfit> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = list.get(position);
        Context context = holder.itemView.getContext();

        holder.tvDate.setText(Utils.formatTimestamp(context, outfit.getTimestamp()));
        holder.clearImages();

        if (outfit.getSlots() != null) {
            for (Map.Entry<String, String> entry : outfit.getSlots().entrySet()) {
                loadItemImage(context, entry.getValue(), entry.getKey(), holder);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CreateOutfitActivity.class);
            intent.putExtra("OUTFIT_ID", outfit.getId());
            intent.putExtra("CAN_EDIT", outfit.getUserId().equals(FirebaseAuth.getInstance().getUid()));
            context.startActivity(intent);
        });
    }

    private void loadItemImage(Context ctx, String clothId, String tag, ViewHolder h) {
        FirebaseFirestore.getInstance().collection("clothes").document(clothId).get().addOnSuccessListener(doc -> {
            Cloth c = doc.toObject(Cloth.class);
            if (c != null) {
                ImageView target = h.getImageViewByTag(tag);
                if (target != null) Glide.with(ctx).load(c.getImageUrl()).into(target);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView preHead, preJacket, preShirt, preScarf, prePants, preAcc1, preAcc2, preShoes, preSocks;

        ViewHolder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvOutfitDate);
            preHead = v.findViewById(R.id.preHead);
            preJacket = v.findViewById(R.id.preJacket);
            preShirt = v.findViewById(R.id.preShirt);
            preScarf = v.findViewById(R.id.preScarf);
            prePants = v.findViewById(R.id.prePants);
            preAcc1 = v.findViewById(R.id.preAcc1);
            preAcc2 = v.findViewById(R.id.preAcc2);
            preShoes = v.findViewById(R.id.preShoes);
            preSocks = v.findViewById(R.id.preSocks);
        }

        ImageView getImageViewByTag(String tag) {
            switch (tag) {
                case "btnHead":
                    return preHead;
                case "btnJacket":
                    return preJacket;
                case "btnShirt":
                    return preShirt;
                case "btnScarf":
                    return preScarf;
                case "btnPants":
                    return prePants;
                case "btnAcc1":
                    return preAcc1;
                case "btnAcc2":
                    return preAcc2;
                case "btnShoes":
                    return preShoes;
                case "btnSocks":
                    return preSocks;
                default:
                    return null;
            }
        }

        void clearImages() {
            ImageView[] imgs = {preHead, preJacket, preShirt, preScarf, prePants, preAcc1, preAcc2, preShoes, preSocks};
            for (ImageView i : imgs) if (i != null) i.setImageDrawable(null);
        }

    }
}