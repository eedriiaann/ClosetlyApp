package com.example.armariovirtual.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.armariovirtual.R;

import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private final Context context;
    private final List<String> videos;

    public ReelAdapter(Context context, List<String> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reels_item, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout playerContainer;
        public ImageView pauseIcon;
        public ImageView playIcon;
        public ImageView btnLike, btnComment, btnShare;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerContainer = itemView.findViewById(R.id.playerContainer);
            playIcon = itemView.findViewById(R.id.playIcon);
            pauseIcon = itemView.findViewById(R.id.pauseIcon);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}
