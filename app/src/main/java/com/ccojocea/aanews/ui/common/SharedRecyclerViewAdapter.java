package com.ccojocea.aanews.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.TimeUtil;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.databinding.LayoutAddItemBinding;
import com.ccojocea.aanews.databinding.LayoutDefaultNewsItemBinding;
import com.ccojocea.aanews.databinding.LayoutLargeNewsItemBinding;
import com.ccojocea.aanews.ui.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

public class SharedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_LARGE = 1;
    private static final int VIEW_TYPE_ADD = 2;

    @NonNull
    private List<ArticleEntity> items = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                return new DefaultItemViewHolder(LayoutDefaultNewsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_LARGE:
                return new LargeItemViewHolder(LayoutLargeNewsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_ADD:
                return new AddItemViewHolder(LayoutAddItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default:
                throw new RuntimeException("No handling matches that type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ArticleEntity article = items.get(position);
        switch (getItemViewType(position)) {
            case VIEW_TYPE_DEFAULT:
                LayoutDefaultNewsItemBinding layoutDefaultNewsItemBinding = ((DefaultItemViewHolder) holder).binding;
                layoutDefaultNewsItemBinding.description.setText(article.getDescription() != null ? article.getDescription() : article.getContent());
            case VIEW_TYPE_LARGE:
                ArticleBindingInterface binding = ((ArticleBindingInterface) holder);
                binding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(binding.getRoot().getContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.KEY_SOURCE_NAME, article.getSource().getName());
                    intent.putExtra(WebViewActivity.KEY_URL, article.getUrl());
                    intent.putExtra(WebViewActivity.KEY_SAVED, article.isSaved());
                    binding.getRoot().getContext().startActivity(intent);
                });

                binding.getShare().setOnClickListener(v -> {
                    Utils.shareLink(binding.getRoot().getContext(), article.getUrl());
                });

                binding.getBookmark().setOnClickListener(v -> {
                    if (Utils.shouldPreventMisClick()) {
                        return;
                    }
                    //this part causes flickering and moving of some items if used on first items in the list and recycler is scrolled at position 0
                    if (article.isSaved()) {
                        article.setSaved(false);
                        binding.getBookmark().setImageResource(R.drawable.ic_bookmark);
                        if (listener != null) {
                            listener.onBookmarkClicked(position, article.getUrl(), false);
                        }
                    } else {
                        article.setSaved(true);
                        binding.getBookmark().setImageResource(R.drawable.ic_bookmark_selected);
                        if (listener != null) {
                            listener.onBookmarkClicked(position, article.getUrl(), true);
                        }
                    }
                    //TODO Call notifyItemChanged with position / payload
//                    notifyItemChanged(position);
                    //TODO Test with DiffUtil
                    //TODO Test with RxJava
                });

                if (article.isSaved()) {
                    binding.getBookmark().setImageResource(R.drawable.ic_bookmark_selected);
                } else {
                    binding.getBookmark().setImageResource(R.drawable.ic_bookmark);
                }
                binding.getAuthorText().setText(article.getAuthor());
                binding.getPublishedAtText().setText(TimeUtil.convertToReadableTime(article.getPublishedAt()));
                binding.getSourceNameText().setText(article.getSource().getName());
                binding.getTitleText().setText(article.getTitle());
                if (article.getUrlToImage() != null) {
                    Glide.with(binding.getRoot().getContext())
                            .load(article.getUrlToImage())
                            .error(R.drawable.placeholder)
                            .into(binding.getArticleImage());
                } else {
                    Glide.with(binding.getRoot().getContext())
                            .load(R.drawable.placeholder)
                            .into(binding.getArticleImage());
                }
                break;
            case VIEW_TYPE_ADD:
                break;
        }
    }

    //TODO Implement this - call the same bind method in here to avoid rewriting code
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    //TODO VIEW_TYPE_ADD
    @Override
    public int getItemViewType(int position) {
        if (position % 5 == 0) {
            return VIEW_TYPE_LARGE;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<ArticleEntity> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @Nullable
    public ArticleEntity getItem(int position) {
        if (items.size() > position) {
            return items.get(position);
        }
        return null;
    }

    public static class DefaultItemViewHolder extends RecyclerView.ViewHolder implements ArticleBindingInterface {

        private LayoutDefaultNewsItemBinding binding;

        public DefaultItemViewHolder(LayoutDefaultNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public View getRoot() {
            return binding.getRoot();
        }

        @Override
        public ImageView getArticleImage() {
            return binding.articleImage;
        }

        @Override
        public ImageView getShare() {
            return binding.share;
        }

        @Override
        public ImageView getBookmark() {
            return binding.bookmark;
        }

        @Override
        public TextView getAuthorText() {
            return binding.author;
        }

        @Override
        public TextView getPublishedAtText() {
            return binding.publishedAt;
        }

        @Override
        public TextView getSourceNameText() {
            return binding.sourceName;
        }

        @Override
        public TextView getTitleText() {
            return binding.title;
        }

    }

    public static class LargeItemViewHolder extends RecyclerView.ViewHolder implements ArticleBindingInterface {

        private LayoutLargeNewsItemBinding binding;

        public LargeItemViewHolder(LayoutLargeNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public View getRoot() {
            return binding.getRoot();
        }

        @Override
        public ImageView getArticleImage() {
            return binding.articleImage;
        }

        @Override
        public ImageView getShare() {
            return binding.share;
        }

        @Override
        public ImageView getBookmark() {
            return binding.bookmark;
        }

        @Override
        public TextView getAuthorText() {
            return binding.author;
        }

        @Override
        public TextView getPublishedAtText() {
            return binding.publishedAt;
        }

        @Override
        public TextView getSourceNameText() {
            return binding.sourceName;
        }

        @Override
        public TextView getTitleText() {
            return binding.title;
        }

    }

    public static class AddItemViewHolder extends RecyclerView.ViewHolder {

        private LayoutAddItemBinding binding;

        public AddItemViewHolder(LayoutAddItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private interface ArticleBindingInterface {

        View getRoot();

        ImageView getArticleImage();

        ImageView getShare();

        ImageView getBookmark();

        TextView getAuthorText();

        TextView getPublishedAtText();

        TextView getSourceNameText();

        TextView getTitleText();

    }

    @Nullable
    private NewsAdapterListener listener;

    public void setAdapterListener(@Nullable NewsAdapterListener listener) {
        this.listener = listener;
    }

    public interface NewsAdapterListener {

        void onBookmarkClicked(int position, String url, boolean shouldSave);

    }

    //TODO TEST & REMOVE

    public void updateNewItems(@NonNull List<ArticleEntity> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NewsDiffUtilCallback(newItems, items));
        diffResult.dispatchUpdatesTo(this);
        items.clear();
        this.items.addAll(newItems);
    }

    public void addItems(@NonNull List<ArticleEntity> newItems) {
        int lastIndex = items.size() - 1;
        this.items.addAll(newItems);
        notifyItemRangeInserted(lastIndex, newItems.size());
    }

    public class NewsDiffUtilCallback extends DiffUtil.Callback {

        List<ArticleEntity> newList;
        List<ArticleEntity> oldList;

        public NewsDiffUtilCallback(List<ArticleEntity> newList, List<ArticleEntity> oldList) {
            this.newList = newList;
            this.oldList = oldList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return newList.get(newItemPosition).getUrl().equals(oldList.get(oldItemPosition).getUrl());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            int result = newList.get(newItemPosition).compareTo(oldList.get(oldItemPosition));
            return result == 0;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            ArticleEntity newArticle = newList.get(newItemPosition);
            ArticleEntity oldArticle = oldList.get(oldItemPosition);

            Bundle diffBundle = new Bundle();

            if (newArticle.isSaved() != oldArticle.isSaved()) {
                diffBundle.putBoolean("isSaved", newArticle.isSaved());
            }
            if (diffBundle.size() == 0) {
                return null;
            }
            return diffBundle;
        }
    }

}
