package com.ccojocea.aanews.ui.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.TimeUtil;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.databinding.LayoutDefaultNewsItemBinding;
import com.ccojocea.aanews.databinding.LayoutLargeNewsItemBinding;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.webview.WebViewActivity;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class SharedRecyclerViewAdapter extends ListAdapter<ArticleEntity, SharedRecyclerViewAdapter.ArticleViewHolder> {

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_LARGE = 1;

    // every n-th article is shown with a large image
    private static final int LARGE_ITEM_INTERVAL = 5;

    // passed to onBindViewHolder when only the bookmark state of an article changed
    private static final Object PAYLOAD_BOOKMARK = new Object();

    // used by Bookmarks (just default type of view)
    private boolean isDefaultType;

    private boolean isItemViewSwipeEnabled;

    @Nullable
    private NewsAdapterListener listener;

    public SharedRecyclerViewAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ArticleViewHolder holder;
        if (viewType == VIEW_TYPE_LARGE) {
            holder = new LargeItemViewHolder(LayoutLargeNewsItemBinding.inflate(inflater, parent, false));
        } else {
            holder = new DefaultItemViewHolder(LayoutDefaultNewsItemBinding.inflate(inflater, parent, false));
        }

        // listeners are set once per view holder and look up the article when clicked, so they never use a stale position
        holder.getRoot().setOnClickListener(v -> {
            ArticleEntity article = getArticle(holder.getBindingAdapterPosition());
            if (article != null) {
                Context context = v.getContext();
                context.startActivity(WebViewActivity.newIntent(context, article));
            }
        });
        holder.getShare().setOnClickListener(v -> {
            ArticleEntity article = getArticle(holder.getBindingAdapterPosition());
            if (article != null) {
                Utils.shareLink(v.getContext(), article.getUrl());
            }
        });
        holder.getBookmark().setOnClickListener(v -> {
            ArticleEntity article = getArticle(holder.getBindingAdapterPosition());
            if (article != null) {
                toggleBookmark(article, holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_BOOKMARK)) {
            holder.bindBookmark(getItem(position).isSaved());
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isDefaultType || position % LARGE_ITEM_INTERVAL != 0) {
            return VIEW_TYPE_DEFAULT;
        }
        return VIEW_TYPE_LARGE;
    }

    public void setDefaultType(boolean isDefaultType) {
        this.isDefaultType = isDefaultType;
    }

    @Nullable
    public ArticleEntity getArticle(int position) {
        if (position > NO_POSITION && position < getItemCount()) {
            return getItem(position);
        }
        return null;
    }

    public void setupSwipeCallback(RecyclerView recyclerView, Context context) {
        SwipeCallback swipeCallback = new SwipeCallback(context) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                ArticleEntity article = getArticle(position);
                if (article != null) {
                    switch (direction) {
                        case ItemTouchHelper.LEFT:
                            Utils.shareLink(viewHolder.itemView.getContext(), article.getUrl());
                            break;
                        case ItemTouchHelper.RIGHT:
                            if (listener != null && !Utils.shouldPreventMisClick()) {
                                listener.onBookmarkClicked(article, !article.isSaved());
                            }
                            break;
                    }
                }
                // ItemTouchHelper keeps a swiped view off screen until its item changes, this brings it back
                if (position != NO_POSITION) {
                    notifyItemChanged(position);
                }
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return isItemViewSwipeEnabled;
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void setItemViewSwipeEnabled(boolean isItemViewSwipeEnabled) {
        this.isItemViewSwipeEnabled = isItemViewSwipeEnabled;
    }

    private void toggleBookmark(@NonNull ArticleEntity article, @NonNull ArticleViewHolder holder) {
        if (Utils.shouldPreventMisClick()) {
            return;
        }
        boolean shouldSave = !article.isSaved();
        // immediate feedback, the list itself is updated once the database changes
        holder.bindBookmark(shouldSave);
        if (listener != null) {
            listener.onBookmarkClicked(article, shouldSave);
        }
    }

    public void setAdapterListener(@Nullable NewsAdapterListener listener) {
        this.listener = listener;
    }

    public interface NewsAdapterListener {

        void onBookmarkClicked(@NonNull ArticleEntity article, boolean shouldSave);

    }

    public abstract static class ArticleViewHolder extends RecyclerView.ViewHolder {

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract View getRoot();

        abstract ImageView getArticleImage();

        abstract ImageView getShare();

        abstract ImageView getBookmark();

        abstract TextView getAuthorText();

        abstract TextView getPublishedAtText();

        abstract TextView getSourceNameText();

        abstract TextView getTitleText();

        void bind(@NonNull ArticleEntity article) {
            bindBookmark(article.isSaved());
            getAuthorText().setText(article.getAuthor());
            getPublishedAtText().setText(TimeUtil.convertToReadableTime(article.getPublishedAt()));
            getSourceNameText().setText(article.getSource().getName());
            getTitleText().setText(article.getTitle());
            if (article.getUrlToImage() != null) {
                Glide.with(getRoot())
                        .load(article.getUrlToImage())
                        .error(R.drawable.placeholder)
                        .listener(IMAGE_ERROR_LOGGER)
                        .into(getArticleImage());
            } else {
                Glide.with(getRoot())
                        .load(R.drawable.placeholder)
                        .into(getArticleImage());
            }
        }

        void bindBookmark(boolean isSaved) {
            getBookmark().setImageResource(isSaved ? R.drawable.ic_bookmark_selected : R.drawable.ic_bookmark);
        }

    }

    public static class DefaultItemViewHolder extends ArticleViewHolder {

        private final LayoutDefaultNewsItemBinding binding;

        public DefaultItemViewHolder(LayoutDefaultNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        void bind(@NonNull ArticleEntity article) {
            super.bind(article);
            binding.description.setText(article.getDescription() != null ? article.getDescription() : article.getContent());
        }

        @Override
        View getRoot() {
            return binding.getRoot();
        }

        @Override
        ImageView getArticleImage() {
            return binding.articleImage;
        }

        @Override
        ImageView getShare() {
            return binding.share;
        }

        @Override
        ImageView getBookmark() {
            return binding.bookmark;
        }

        @Override
        TextView getAuthorText() {
            return binding.author;
        }

        @Override
        TextView getPublishedAtText() {
            return binding.publishedAt;
        }

        @Override
        TextView getSourceNameText() {
            return binding.sourceName;
        }

        @Override
        TextView getTitleText() {
            return binding.title;
        }

    }

    public static class LargeItemViewHolder extends ArticleViewHolder {

        private final LayoutLargeNewsItemBinding binding;

        public LargeItemViewHolder(LayoutLargeNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        View getRoot() {
            return binding.getRoot();
        }

        @Override
        ImageView getArticleImage() {
            return binding.articleImage;
        }

        @Override
        ImageView getShare() {
            return binding.share;
        }

        @Override
        ImageView getBookmark() {
            return binding.bookmark;
        }

        @Override
        TextView getAuthorText() {
            return binding.author;
        }

        @Override
        TextView getPublishedAtText() {
            return binding.publishedAt;
        }

        @Override
        TextView getSourceNameText() {
            return binding.sourceName;
        }

        @Override
        TextView getTitleText() {
            return binding.title;
        }

    }

    // the placeholder is shown when an image fails, this tells why (in debug builds, see App)
    private static final RequestListener<Drawable> IMAGE_ERROR_LOGGER = new RequestListener<Drawable>() {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
            Timber.w(e, "Could not load image %s", model);
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
            return false;
        }

    };

    private static boolean isSameContentIgnoringSaved(@NonNull ArticleEntity oldArticle, @NonNull ArticleEntity newArticle) {
        return oldArticle.getTitle().equals(newArticle.getTitle())
                && oldArticle.getAuthor().equals(newArticle.getAuthor())
                && oldArticle.getPublishedAt().equals(newArticle.getPublishedAt())
                && oldArticle.getSource().getName().equals(newArticle.getSource().getName())
                && Objects.equals(oldArticle.getDescription(), newArticle.getDescription())
                && Objects.equals(oldArticle.getUrlToImage(), newArticle.getUrlToImage());
    }

    private static final DiffUtil.ItemCallback<ArticleEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ArticleEntity>() {

        @Override
        public boolean areItemsTheSame(@NonNull ArticleEntity oldItem, @NonNull ArticleEntity newItem) {
            return oldItem.getUrl().equals(newItem.getUrl());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ArticleEntity oldItem, @NonNull ArticleEntity newItem) {
            return oldItem.isSaved() == newItem.isSaved() && isSameContentIgnoringSaved(oldItem, newItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull ArticleEntity oldItem, @NonNull ArticleEntity newItem) {
            if (oldItem.isSaved() != newItem.isSaved() && isSameContentIgnoringSaved(oldItem, newItem)) {
                return PAYLOAD_BOOKMARK;
            }
            return null;
        }

    };

}
