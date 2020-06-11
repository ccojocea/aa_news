package com.ccojocea.aanews.localnews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.NewsViewModel;
import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.data.models.entity.ArticleEntity;
import com.ccojocea.aanews.databinding.LayoutLocalFragmentBinding;

import java.util.List;

public class LocalFragment extends BaseFragment {

    private LayoutLocalFragmentBinding binding;
    private NewsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ViewModel - use requireActivity() so this is bound to the activity (otherwise it would be recreated on config changes)
        viewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
        setFragmentTitle(getString(R.string.tab_local_news));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutLocalFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLiveData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupLiveData() {
        viewModel.getTopHeadlinesLiveData().observe(getViewLifecycleOwner(), new Observer<List<ArticleEntity>>() {
            @Override
            public void onChanged(List<ArticleEntity> articleEntities) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Number of retrieved articles: " + articleEntities.size(), Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null && aBoolean) {
                    viewModel.resetError();
                    //TODO
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "ERROR!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
