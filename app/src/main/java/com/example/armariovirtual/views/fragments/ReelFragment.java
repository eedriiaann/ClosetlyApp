package com.example.armariovirtual.views.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.armariovirtual.R;
import com.example.armariovirtual.adapters.ReelAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class ReelFragment extends Fragment {

    private ViewPager2 viewPager;
    private ReelAdapter adapter;
    private List<String> videoUrls;
    private ExoPlayer exoPlayer;
    private PlayerView sharedPlayerView;
    private boolean isLooping = false;
    private Handler handler = new Handler();

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.reels_fragment, container, false);

        return root;
    }

    private void fetchVideosFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("videos").orderBy("timestamp").get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String url = doc.getString("videoUrl");
                if (url != null) {
                    videoUrls.add(url);
                }
            }
            setupViewPager();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error cargando vídeos", Toast.LENGTH_SHORT).show();
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupViewPager() {
        if (videoUrls.size() > 1) {
            videoUrls.add(0, videoUrls.get(videoUrls.size() - 1));
            videoUrls.add(videoUrls.get(1));
        }

        adapter = new ReelAdapter(requireContext(), videoUrls);
        viewPager.setAdapter(adapter);

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        sharedPlayerView = new PlayerView(requireContext());
        sharedPlayerView.setUseController(false);
        sharedPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        sharedPlayerView.setKeepScreenOn(true);
        sharedPlayerView.setPlayer(exoPlayer);

        viewPager.setCurrentItem(1, false);
        viewPager.postDelayed(() -> attachPlayerToPosition(1), 100);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPager.postDelayed(() -> attachPlayerToPosition(position), 100);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isLooping) {
                    int position = viewPager.getCurrentItem();
                    if (position == 0) {
                        isLooping = true;
                        viewPager.setCurrentItem(videoUrls.size() - 2, false);
                    } else if (position == videoUrls.size() - 1) {
                        isLooping = true;
                        viewPager.setCurrentItem(1, false);
                    }
                } else if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isLooping = false;
                }
            }
        });
    }

    private void attachPlayerToPosition(int position) {
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

        if (holder instanceof ReelAdapter.ReelViewHolder) {
            ReelAdapter.ReelViewHolder reelHolder = (ReelAdapter.ReelViewHolder) holder;

            ViewGroup parent = (ViewGroup) sharedPlayerView.getParent();
            if (parent != null) parent.removeView(sharedPlayerView);
            reelHolder.playerContainer.addView(sharedPlayerView, 0);

            String url = videoUrls.get(position);
            MediaItem currentItem = exoPlayer.getCurrentMediaItem();
            if (currentItem == null || !currentItem.mediaId.equals(url)) {
                MediaItem item = new MediaItem.Builder().setUri(url).setMediaId(url).build();
                exoPlayer.setMediaItem(item);
                exoPlayer.prepare();
            }

            exoPlayer.play();

            reelHolder.playerContainer.setOnClickListener(v -> togglePlayPause(reelHolder));
            final boolean[] liked = {false};

            reelHolder.btnLike.setOnClickListener(v -> {
                liked[0] = !liked[0];
                reelHolder.btnLike.setImageTintList(ColorStateList.valueOf(liked[0] ? Color.RED : Color.WHITE));
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120));
            });

            reelHolder.btnComment.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Aun no esta implementada esta funcion", Toast.LENGTH_SHORT).show();
            });

            reelHolder.btnShare.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Aun no esta implementada esta funcion", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void togglePlayPause(ReelAdapter.ReelViewHolder holder) {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
            showCenterIcon(holder.pauseIcon);
        } else {
            exoPlayer.play();
            showCenterIcon(holder.playIcon);
        }
    }

    private void showCenterIcon(View icon) {
        icon.setAlpha(0f);
        icon.setVisibility(View.VISIBLE);
        icon.animate().alpha(1f).setDuration(150).withEndAction(() -> handler.postDelayed(() -> icon.animate().alpha(0f).setDuration(300).withEndAction(() -> icon.setVisibility(View.GONE)), 400));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) exoPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) exoPlayer.play();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        sharedPlayerView = null;
    }


}
