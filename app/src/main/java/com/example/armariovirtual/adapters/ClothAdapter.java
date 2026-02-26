package com.example.armariovirtual.adapters;

import android.content.Context;
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

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ViewHolder> {

    private List<Cloth> clothes;
    private final Context context;
    private OnClothClickListener onClothClickListener;

    public interface OnClothClickListener {
        void onClothClick(Cloth cloth);
    }

    public void setOnClothClickListener(OnClothClickListener listener) {
        this.onClothClickListener = listener;
    }

    public ClothAdapter(Context context, List<Cloth> clothes) {
        this.context = context;
        this.clothes = clothes;
    }

    public void setClothes(List<Cloth> filteredClothes) {
        this.clothes = filteredClothes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cloth, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cloth cloth = clothes.get(position);

        Glide.with(context).load(cloth.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).into(holder.ivClothImage);

        holder.tvName.setText(cloth.getName());
        holder.tvDescripcion.setText(cloth.getDescripcion());
        holder.tvMarca.setText(cloth.getMarca());

        holder.tvPrecio.setText(String.format("%.2f €", cloth.getPrecio()));

        holder.itemView.setOnClickListener(v -> {
            if (onClothClickListener != null) {
                onClothClickListener.onClothClick(cloth);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clothes != null ? clothes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivClothImage;
        TextView tvName, tvDescripcion, tvMarca, tvPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivClothImage = itemView.findViewById(R.id.ivClothImage);
            tvName = itemView.findViewById(R.id.tvClothName);
            tvDescripcion = itemView.findViewById(R.id.tvClothDescripcion);
            tvMarca = itemView.findViewById(R.id.tvClothMarca);
            tvPrecio = itemView.findViewById(R.id.tvClothPrecio);
        }
    }
}