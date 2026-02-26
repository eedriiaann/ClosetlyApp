package com.example.armariovirtual.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;
import com.example.armariovirtual.views.activity.PublicationDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<Publication> publicaciones;
    private Context context;

    public ProfileAdapter(Context context, List<Publication> publicaciones) {
        this.context = context;
        this.publicaciones = publicaciones != null ? publicaciones : new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_publications_square_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publication publication = publicaciones.get(position);

        Glide.with(context).load(publication.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).centerCrop().into(holder.ivPublicacion);

        holder.ivPublicacion.setOnClickListener(v -> {
            Intent intent = new Intent(context, PublicationDetailsActivity.class);
            intent.putExtra("publicacionId", publication.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPublicacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPublicacion = itemView.findViewById(R.id.ivPublicacionPerfil);
        }
    }
}