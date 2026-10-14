package com.ccojocea.aanews.ui.mynews;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.data.Topics;
import com.ccojocea.aanews.databinding.DialogAddTopicBinding;
import com.ccojocea.aanews.databinding.FragmentMyNewsBinding;
import com.ccojocea.aanews.ui.common.BaseActivity;
import com.ccojocea.aanews.ui.common.BaseFragment;
import com.ccojocea.aanews.ui.common.SharedRecyclerViewAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MyNewsFragment extends BaseFragment {

    private FragmentMyNewsBinding binding;
    private MyNewsViewModel viewModel;
    private SharedRecyclerViewAdapter adapter;

    private boolean isLoading;
    private boolean isEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MyNewsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SharedRecyclerViewAdapter();
        setupArticleList(binding.recyclerView, adapter);

        binding.refreshLayout.setOnRefreshListener(viewModel::refreshData);
        // the list is wrapped in a layout, tell the refresh layout when the list itself can still scroll up
        binding.refreshLayout.setOnChildScrollUpCallback((parent, child) -> binding.recyclerView.canScrollVertically(-1));

        setupLiveData();
    }

    private void setupLiveData() {
        viewModel.getArticlesLiveData().observe(getViewLifecycleOwner(), articleEntities -> {
            adapter.submitList(articleEntities);
            isEmpty = articleEntities.isEmpty();
            updateEmptyView();
        });

        viewModel.getTopicsLiveData().observe(getViewLifecycleOwner(), this::showTopics);

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), loading -> {
            isLoading = Boolean.TRUE.equals(loading);
            binding.refreshLayout.setRefreshing(isLoading);
            updateEmptyView();
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::showError);
    }

    private void updateEmptyView() {
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        // the cache is empty on the first start, don't say there is no news while it is being loaded
        binding.emptyView.setVisibility(isEmpty && !isLoading ? View.VISIBLE : View.GONE);
        binding.emptyText.setText(viewModel.hasTopics() ? R.string.my_news_empty : R.string.my_news_no_topics);
    }

    private void showTopics(@NonNull List<String> topics) {
        binding.topicChips.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (String topic : topics) {
            Chip chip = (Chip) inflater.inflate(R.layout.layout_topic_chip, binding.topicChips, false);
            chip.setText(topic);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removeTopic(topic);
                showMessage(getString(R.string.my_news_topic_removed, topic));
            });
            binding.topicChips.addView(chip);
        }
        if (topics.size() < Topics.MAX_TOPICS) {
            Chip addChip = (Chip) inflater.inflate(R.layout.layout_topic_chip, binding.topicChips, false);
            addChip.setText(R.string.my_news_add_topic);
            addChip.setCloseIconVisible(false);
            addChip.setOnClickListener(v -> showAddTopicDialog());
            binding.topicChips.addView(addChip);
        }
    }

    private void showAddTopicDialog() {
        DialogAddTopicBinding dialogBinding = DialogAddTopicBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.my_news_add_topic_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.my_news_add, (d, which) -> addTopic(dialogBinding.topicInput.getText().toString()))
                .setNegativeButton(R.string.my_news_cancel, null)
                .create();
        dialogBinding.topicInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTopic(v.getText().toString());
                dialog.dismiss();
                return true;
            }
            return false;
        });
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialogBinding.topicInput.requestFocus();
        dialog.show();
    }

    private void addTopic(@NonNull String topic) {
        switch (viewModel.addTopic(topic)) {
            case INVALID:
                showMessage(getString(R.string.my_news_topic_invalid, Topics.MAX_TOPIC_LENGTH));
                break;
            case DUPLICATE:
                showMessage(getString(R.string.my_news_topic_duplicate, topic.trim()));
                break;
            case LIMIT_REACHED:
                showMessage(getString(R.string.my_news_topic_limit, Topics.MAX_TOPICS));
                break;
            case ADDED:
            default:
                if (binding != null) {
                    binding.recyclerView.scrollToPosition(0);
                }
                break;
        }
    }

    private void showMessage(@NonNull String message) {
        BaseActivity activity = getBaseActivity();
        if (activity != null && activity.getRoot() != null) {
            Utils.showSnackBar(activity.getRoot(), message, Snackbar.LENGTH_SHORT, false);
        }
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.tab_my_news);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
