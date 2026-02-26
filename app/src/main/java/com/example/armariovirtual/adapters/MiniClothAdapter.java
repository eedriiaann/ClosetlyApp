package com.example.armariovirtual.adapters;

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

import java.util.List;

public class MiniClothAdapter extends RecyclerView.Adapter<MiniClothAdapter.MiniViewHolder> {

    private List<Cloth> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cloth cloth);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MiniClothAdapter(List<Cloth> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MiniViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_cloth_mini, p, false);
        return new MiniViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniViewHolder h, int p) {
        Cloth c = list.get(p);
        h.name.setText(c.getName());
        h.details.setText(c.getMarca() + " - " + c.getCategoria());

        Glide.with(h.itemView.getContext()).load(c.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).into(h.image);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MiniViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, details;

        public MiniViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.ivClothMini);
            name = v.findViewById(R.id.tvClothNameMini);
            details = v.findViewById(R.id.tvClothDetailsMini);
        }
    }
}