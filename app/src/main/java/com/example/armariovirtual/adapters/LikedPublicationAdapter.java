package com.example.armariovirtual.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;

import java.util.List;

public class LikedPublicationAdapter extends RecyclerView.Adapter<LikedPublicationAdapter.ViewHolder> {

    private List<Publication> publicaciones;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Publication publication);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public LikedPublicationAdapter(List<Publication> publicaciones) {
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion_likeada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Publication pub = publicaciones.get(position);

        Glide.with(holder.ivPublicacion.getContext()).load(pub.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).into(holder.ivPublicacion);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(pub);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    public void setPublicaciones(List<Publication> nuevasPublicaciones) {
        this.publicaciones = nuevasPublicaciones;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPublicacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPublicacion = itemView.findViewById(R.id.ivPublicacionPerfil);
        }
    }
}
