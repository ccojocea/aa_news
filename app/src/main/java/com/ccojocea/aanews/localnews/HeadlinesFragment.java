package com.ccojocea.aanews.localnews;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.BaseFragment;
import com.ccojocea.aanews.common.VerticalItemDecoration;
import com.ccojocea.aanews.databinding.FragmentHeadlinesBinding;

public class HeadlinesFragment extends BaseFragment {

    private FragmentHeadlinesBinding binding;
    private HeadlinesViewModel viewModel;
    private RecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ViewModel - use requireActivity() so this is bound to the activity (otherwise it would be recreated on config changes)
        viewModel = new ViewModelProvider(requireActivity()).get(HeadlinesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeadlinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLiveData();

        adapter = new RecyclerAdapter();
        //reduce blinking when changing item - https://medium.com/@hanru.yeh/recyclerviews-views-are-blinking-when-notifydatasetchanged-c7b76d5149a2
        //        adapter.setHasStableIds(true);
        if (binding.recyclerView.getItemAnimator() != null) {
            binding.recyclerView.getItemAnimator().setChangeDuration(0);
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new VerticalItemDecoration());
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public String getFragmentTitle(Context context) {
        return context.getString(R.string.title_headlines);
    }

    public void setupLiveData() {
        viewModel.getTopHeadlinesLiveData().observe(getViewLifecycleOwner(), articleEntities -> {
            if (getContext() != null) {
                adapter.setItems(articleEntities);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), isError -> {
            if (isError != null && isError) {
                viewModel.resetError();
                //TODO
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.error_please_try_again, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
