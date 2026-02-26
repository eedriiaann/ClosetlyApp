package com.example.armariovirtual.views.activity;

import static com.example.armariovirtual.utils.Utils.formatTimestamp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.armariovirtual.R;
import com.example.armariovirtual.models.Publication;
import com.example.armariovirtual.utils.PublicationUtils;
import com.example.armariovirtual.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PublicationDetailsActivity extends AppCompatActivity {

    private ImageView ivPublicacion;
    private ImageView ivPfp;
    private TextView tvUsuario;
    private TextView tvUsuario1;
    private TextView tvDescripcion;
    private TextView tvLikes;
    private TextView tvTiempo;
    private ImageButton btnLike;
    private Publication publication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_publication_details_activity);

        ivPublicacion = findViewById(R.id.ivPublicacionDetalle);
        ivPfp = findViewById(R.id.ivPfpDetalle);
        tvUsuario = findViewById(R.id.tvUsuarioDetalle);
        tvUsuario1 = findViewById(R.id.tvUsuarioDetalle1);
        tvDescripcion = findViewById(R.id.tvDescripcionDetalle);
        tvLikes = findViewById(R.id.tvLikesDetalle);
        tvTiempo = findViewById(R.id.tvTiempoDetalle);
        btnLike = findViewById(R.id.btnLikeDetalle);

        String publicacionId = getIntent().getStringExtra("publicacionId");
        if (publicacionId != null) {
            cargarPublicacion(publicacionId);
        }

        ImageButton btnDerecha = findViewById(R.id.btnDerecha);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        btnDerecha.setOnClickListener(v -> {
            PublicationUtils.showPublicationMenu(currentUser, publication, v);
        });
    }

    private void cargarPublicacion(final String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("images").document(id).get().addOnSuccessListener(snapshot -> {
            publication = snapshot.toObject(Publication.class);
            if (publication != null) {
                publication.setId(snapshot.getId());

                String userId = snapshot.getString("userId");
                if (userId != null) {
                    db.collection("users").document(userId).get().addOnSuccessListener(userSnap -> {
                        if (userSnap.exists()) {
                            publication.setName(userSnap.getString("name"));
                            publication.setPfpUrl(userSnap.getString("profileImageUrl"));
                        }
                        actualizarUI();
                    }).addOnFailureListener(e -> actualizarUI());
                } else {
                    actualizarUI();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(PublicationDetailsActivity.this, "Error al cargar publicación", Toast.LENGTH_SHORT).show());
    }

    private void actualizarUI() {
        Glide.with(this).load(publication.getImageUrl()).placeholder(R.drawable.ic_profile_placeholder).centerCrop().into(ivPublicacion);

        Glide.with(this).load(publication.getPfpUrl()).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(ivPfp);

        tvUsuario.setText(publication.getName());
        tvUsuario1.setText(publication.getName() + ": ");
        tvDescripcion.setText(publication.getDescripcion());
        tvLikes.setText(publication.getMegusta() + " Me gusta");
        String formatted = formatTimestamp(this, publication.getTimestamp());
        tvTiempo.setText(formatted);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("images").document(publication.getId());

        docRef.get().addOnSuccessListener(snapshot -> {
            Map<String, Boolean> likesBy;
            if (snapshot.contains("likesBy")) {
                likesBy = (Map<String, Boolean>) snapshot.get("likesBy");
            } else {
                likesBy = new HashMap<>();
            }

            if (currentUser != null) {
                boolean liked = likesBy.containsKey(currentUser.getUid());
                btnLike.setSelected(liked);
                if (liked) {
                    btnLike.setColorFilter(Color.RED);
                } else {
                    btnLike.setColorFilter(Color.GRAY);
                }

            }
        });

        btnLike.setOnClickListener(v -> {
            if (currentUser == null) return;

            String uid = currentUser.getUid();

            boolean wasLiked = btnLike.isSelected();
            boolean nowLiked = !wasLiked;

            btnLike.setSelected(nowLiked);
            if (nowLiked) {
                btnLike.setColorFilter(Color.RED);
            } else {
                btnLike.setColorFilter(Color.GRAY);
            }


            int currentLikes = Integer.parseInt(tvLikes.getText().toString().split(" ")[0]);
            if (nowLiked) {
                currentLikes += 1;
            } else {
                currentLikes -= 1;
            }

            tvLikes.setText(currentLikes + " Me gusta");

            if (nowLiked) {
                btnLike.setScaleX(0.7f);
                btnLike.setScaleY(0.7f);
                btnLike.animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new OvershootInterpolator()).start();
            }

            db.runTransaction((com.google.firebase.firestore.Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(docRef);

                Map<String, Boolean> likesBy;
                if (snapshot.contains("likesBy")) {
                    likesBy = (Map<String, Boolean>) snapshot.get("likesBy");
                } else {
                    likesBy = new HashMap<>();
                }


                long likes;
                if (snapshot.contains("megusta")) {
                    likes = snapshot.getLong("megusta");
                } else {
                    likes = 0;
                }


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
                Toast.makeText(PublicationDetailsActivity.this, "Error al actualizar like", Toast.LENGTH_SHORT).show();

                btnLike.setSelected(wasLiked);
                if (wasLiked) {
                    btnLike.setColorFilter(Color.RED);
                } else {
                    btnLike.setColorFilter(Color.GRAY);
                }


                int revertLikes = Integer.parseInt(tvLikes.getText().toString().split(" ")[0]);
                if (wasLiked) {
                    revertLikes += 1;
                } else {
                    revertLikes -= 1;
                }

                tvLikes.setText(revertLikes + " Me gusta");
            });
        });
    }

    public void goBack(View view) {
        Utils.goBack(this);
    }
}
