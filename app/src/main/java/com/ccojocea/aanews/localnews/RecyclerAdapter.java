package com.ccojocea.aanews.localnews;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.TimeUtil;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.models.entity.ArticleEntity;
import com.ccojocea.aanews.databinding.LayoutAddItemBinding;
import com.ccojocea.aanews.databinding.LayoutDefaultNewsItemBinding;
import com.ccojocea.aanews.databinding.LayoutLargeNewsItemBinding;
import com.ccojocea.aanews.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

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
                return new SmallItemViewHolder(LayoutAddItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default:
                throw new RuntimeException("No handling matches that type");
        }
    }

    //https://medium.com/@hanru.yeh/recyclerviews-views-are-blinking-when-notifydatasetchanged-c7b76d5149a2
//    @Override
//    public long getItemId(int position) {
//        return items.get(position).getUrl().hashCode();
//    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ArticleEntity article = items.get(position);

        switch (getItemViewType(position)) {
            case VIEW_TYPE_DEFAULT:
                LayoutDefaultNewsItemBinding defaultHolderBinding = ((DefaultItemViewHolder) holder).binding;
                defaultHolderBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(defaultHolderBinding.getRoot().getContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.KEY_SOURCE_NAME, article.getSource().getName());
                    intent.putExtra(WebViewActivity.KEY_URL, article.getUrl());
                    intent.putExtra(WebViewActivity.KEY_SAVED, article.isSaved());
                    defaultHolderBinding.getRoot().getContext().startActivity(intent);
                });

                defaultHolderBinding.share.setOnClickListener(v -> {
                    Utils.shareLink(defaultHolderBinding.getRoot().getContext(), article.getUrl());
                });

                defaultHolderBinding.bookmark.setOnClickListener(v -> {
                    //TODO Bookmark/Un-bookmark and toggle icon based on db success
                    if (article.isSaved()) {
                        article.setSaved(false);
                        defaultHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark);
                    } else {
                        article.setSaved(true);
                        defaultHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark_selected);
                    }
                    notifyItemChanged(position);
                });

                if (article.isSaved()) {
                    defaultHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark_selected);
                } else {
                    defaultHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark);
                }
                defaultHolderBinding.author.setText(article.getAuthor());
                defaultHolderBinding.description.setText(article.getDescription() != null ? article.getDescription() : article.getContent());
                defaultHolderBinding.publishedAt.setText(TimeUtil.convertToReadableTime(article.getPublishedAt()));
                defaultHolderBinding.sourceName.setText(article.getSource().getName());
                defaultHolderBinding.title.setText(article.getTitle());
                if (article.getUrlToImage() != null) {
                    Glide.with(defaultHolderBinding.getRoot().getContext())
                            .load(article.getUrlToImage())
                            .error(R.drawable.placeholder)
                            .into(defaultHolderBinding.imageView);
                } else {
                    Glide.with(defaultHolderBinding.getRoot().getContext())
                            .load(R.drawable.placeholder)
                            .into(defaultHolderBinding.imageView);
                }
                break;
            case VIEW_TYPE_LARGE:
                LayoutLargeNewsItemBinding largeHolderBinding = ((LargeItemViewHolder) holder).binding;
                largeHolderBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(largeHolderBinding.getRoot().getContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.KEY_SOURCE_NAME, article.getSource().getName());
                    intent.putExtra(WebViewActivity.KEY_URL, article.getUrl());
                    intent.putExtra(WebViewActivity.KEY_SAVED, article.isSaved());
                    largeHolderBinding.getRoot().getContext().startActivity(intent);
                });

                largeHolderBinding.share.setOnClickListener(v -> {
                    Utils.shareLink(largeHolderBinding.getRoot().getContext(), article.getUrl());
                });

                largeHolderBinding.bookmark.setOnClickListener(v -> {
                    //TODO Bookmark/Un-bookmark and toggle icon based on db success
                    if (article.isSaved()) {
                        article.setSaved(false);
                        largeHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark);
                    } else {
                        article.setSaved(true);
                        largeHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark_selected);
                    }
                    notifyItemChanged(position);
                });

                if (article.isSaved()) {
                    largeHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark_selected);
                } else {
                    largeHolderBinding.bookmark.setImageResource(R.drawable.ic_bookmark);
                }
                largeHolderBinding.author.setText(article.getAuthor());
                largeHolderBinding.publishedAt.setText(TimeUtil.convertToReadableTime(article.getPublishedAt()));
                largeHolderBinding.sourceName.setText(article.getSource().getName());
                largeHolderBinding.title.setText(article.getTitle());
                if (article.getUrlToImage() != null) {
                    Glide.with(largeHolderBinding.getRoot().getContext())
                            .load(article.getUrlToImage())
                            .error(R.drawable.placeholder)
                            .into(largeHolderBinding.imageView);
                } else {
                    Glide.with(largeHolderBinding.getRoot().getContext())
                            .load(R.drawable.placeholder)
                            .into(largeHolderBinding.imageView);
                }
                break;
            case VIEW_TYPE_ADD:
                break;
        }
    }

    //TODO for ADD_VIEW
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

    public void addItems(@NonNull List<ArticleEntity> newItems) {
        int lastIndex = items.size() - 1;
        this.items.addAll(newItems);
        notifyItemRangeInserted(lastIndex, newItems.size());
    }

    public static class DefaultItemViewHolder extends RecyclerView.ViewHolder {

        private LayoutDefaultNewsItemBinding binding;

        public DefaultItemViewHolder(LayoutDefaultNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public static class SmallItemViewHolder extends RecyclerView.ViewHolder {

        private LayoutAddItemBinding binding;

        public SmallItemViewHolder(LayoutAddItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public static class LargeItemViewHolder extends RecyclerView.ViewHolder {

        private LayoutLargeNewsItemBinding binding;

        public LargeItemViewHolder(LayoutLargeNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}
