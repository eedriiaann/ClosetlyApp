package com.example.armariovirtual.adapters;

import static com.example.armariovirtual.utils.Utils.formatTimestamp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Cloth;
import com.example.armariovirtual.models.Publication;
import com.example.armariovirtual.utils.PublicationUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.ViewHolder> {
    private List<Publication> publicaciones;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(String userId, String userName, String userPhotoUrl);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    public PublicationAdapter(List<Publication> publicaciones) {
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_publications_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPublicacion, ivPfp, ivMoreSettings;
        ImageButton imageButton, clothesButton;
        TextView tvNMeGusta, tvUsuario, tvUsuario1, tvDescripcion, tvTiempo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPublicacion = itemView.findViewById(R.id.ivPublicacion);
            ivPfp = itemView.findViewById(R.id.ivPfp);
            ivMoreSettings = itemView.findViewById(R.id.ivMoreSettings);
            imageButton = itemView.findViewById(R.id.imageButton);
            clothesButton = itemView.findViewById(R.id.clothesButton);
            tvNMeGusta = itemView.findViewById(R.id.tvNMeGusta);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvUsuario1 = itemView.findViewById(R.id.tvUsuario1);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Publication publication = publicaciones.get(position);
        Context context = holder.itemView.getContext();

        Glide.with(context).load(publication.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).into(holder.ivPublicacion);
        Glide.with(context).load(publication.getPfpUrl()).placeholder(R.drawable.ic_profile_placeholder).transform(new CropCircleWithBorderTransformation(6, Color.BLACK)).into(holder.ivPfp);

        holder.tvUsuario.setText(publication.getName());
        holder.tvUsuario1.setText(publication.getName() + ":");
        holder.tvDescripcion.setText(publication.getDescripcion());
        holder.tvNMeGusta.setText(publication.getMegusta() + " " + context.getString(R.string.like));
        holder.tvTiempo.setText(formatTimestamp(context, publication.getTimestamp()));

        holder.clothesButton.setOnClickListener(v -> {
            if (publication.getClothIds() != null && !publication.getClothIds().isEmpty()) {
                showClothesBottomSheet(context, publication.getClothIds());
            } else {
                Toast.makeText(context, "No hay prendas adjuntadas", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener userClick = v -> {
            if (onUserClickListener != null && publication.getUserId() != null) {
                onUserClickListener.onUserClick(publication.getUserId(), publication.getName(), publication.getPfpUrl());
            }
        };
        holder.ivPfp.setOnClickListener(userClick);
        holder.tvUsuario.setOnClickListener(userClick);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("images").document(publication.getId());

        docRef.get().addOnSuccessListener(snapshot -> {
            Map<String, Boolean> likesBy = (snapshot.contains("likesBy")) ? (Map<String, Boolean>) snapshot.get("likesBy") : new HashMap<>();
            boolean liked = currentUser != null && likesBy.containsKey(currentUser.getUid());
            holder.imageButton.setSelected(liked);
            holder.imageButton.setColorFilter(liked ? Color.RED : Color.GRAY);
        });

        holder.ivMoreSettings.setOnClickListener(v -> PublicationUtils.showPublicationMenu(currentUser, publication, v));
        holder.imageButton.setOnClickListener(v -> handleLikeAction(holder, currentUser, docRef, context));
    }

    private void showClothesBottomSheet(Context context, List<String> clothIds) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_clothes, null);

        RecyclerView rvClothes = view.findViewById(R.id.rvClothesBottomSheet);
        rvClothes.setLayoutManager(new LinearLayoutManager(context));

        List<Cloth> clothesList = new ArrayList<>();
        MiniClothAdapter miniAdapter = new MiniClothAdapter(clothesList);

        miniAdapter.setOnItemClickListener(cloth -> {
            Toast.makeText(context, "Prenda: " + cloth.getName(), Toast.LENGTH_SHORT).show();
        });

        rvClothes.setAdapter(miniAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String id : clothIds) {
            if (id == null) continue;
            db.collection("clothes").document(id).get().addOnSuccessListener(doc -> {
                Cloth c = doc.toObject(Cloth.class);
                if (c != null) {
                    c.setId(doc.getId());
                    clothesList.add(c);
                    miniAdapter.notifyDataSetChanged();
                }
            });
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void handleLikeAction(ViewHolder holder, FirebaseUser currentUser, DocumentReference docRef, Context context) {
        if (currentUser == null) return;
        String uid = currentUser.getUid();
        boolean wasLiked = holder.imageButton.isSelected();
        boolean nowLiked = !wasLiked;

        holder.imageButton.setSelected(nowLiked);
        holder.imageButton.setColorFilter(nowLiked ? Color.RED : Color.GRAY);

        int currentLikes = Integer.parseInt(holder.tvNMeGusta.getText().toString().split(" ")[0]);
        int newLikesCount = nowLiked ? currentLikes + 1 : currentLikes - 1;
        holder.tvNMeGusta.setText(newLikesCount + " " + context.getString(R.string.like));

        if (nowLiked) {
            holder.imageButton.setScaleX(0.7f);
            holder.imageButton.setScaleY(0.7f);
            holder.imageButton.animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new OvershootInterpolator()).start();
        }

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            long likes = snapshot.contains("megusta") ? snapshot.getLong("megusta") : 0;
            Map<String, Boolean> likesBy = snapshot.contains("likesBy") ? (Map<String, Boolean>) snapshot.get("likesBy") : new HashMap<>();

            if (nowLiked) {
                likesBy.put(uid, true);
                transaction.update(docRef, "megusta", likes + 1);
            } else {
                likesBy.remove(uid);
                transaction.update(docRef, "megusta", likes - 1);
            }
            transaction.update(docRef, "likesBy", likesBy);
            return null;
        }).addOnFailureListener(e -> {
            holder.imageButton.setSelected(wasLiked);
            holder.imageButton.setColorFilter(wasLiked ? Color.RED : Color.GRAY);
            holder.tvNMeGusta.setText(currentLikes + " " + context.getString(R.string.like));
        });
    }

}