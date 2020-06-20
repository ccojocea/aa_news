package com.ccojocea.aanews.ui.bookmarks;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.TimeUtil;
import com.ccojocea.aanews.databinding.LayoutBookmarkNewsItemBinding;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;
import com.ccojocea.aanews.ui.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class BookmarksRecyclerAdapter extends RecyclerView.Adapter<BookmarksRecyclerAdapter.BookmarksHolder> {

    private List<SavedArticleEntity> items = new ArrayList<>();

    @NonNull
    @Override
    public BookmarksHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookmarksHolder(LayoutBookmarkNewsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarksHolder holder, int position) {
        SavedArticleEntity article = items.get(position);

        holder.binding.share.setVisibility(GONE);
        holder.binding.bookmark.setVisibility(GONE);

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(holder.binding.getRoot().getContext(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.KEY_SOURCE_NAME, article.getSource().getName());
            intent.putExtra(WebViewActivity.KEY_URL, article.getUrl());
            intent.putExtra(WebViewActivity.KEY_SAVED, article.isSaved());
            holder.binding.getRoot().getContext().startActivity(intent);
        });

        holder.binding.author.setText(article.getAuthor());
        holder.binding.description.setText(article.getDescription() != null ? article.getDescription() : article.getContent());
        holder.binding.publishedAt.setText(TimeUtil.convertToReadableTime(article.getPublishedAt()));
        holder.binding.sourceName.setText(article.getSource().getName());
        holder.binding.title.setText(article.getTitle());
        if (article.getUrlToImage() != null) {
            Glide.with(holder.binding.getRoot().getContext())
                    .load(article.getUrlToImage())
                    .error(R.drawable.placeholder)
                    .into(holder.binding.articleImage);
        } else {
            Glide.with(holder.binding.getRoot().getContext())
                    .load(R.drawable.placeholder)
                    .into(holder.binding.articleImage);
        }
    }

    public void setItems(List<SavedArticleEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class BookmarksHolder extends RecyclerView.ViewHolder {

        LayoutBookmarkNewsItemBinding binding;

        public BookmarksHolder(LayoutBookmarkNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
