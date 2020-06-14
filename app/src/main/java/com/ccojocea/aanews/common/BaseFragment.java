package com.ccojocea.aanews.common;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (getActivity() != null && args != null) {
            //TODO
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public abstract String getFragmentTitle(Context context);

    /**
     * This method returns BaseActivity.
     * Since the app is using activities that extend BaseActivity there should not be a problem using this method.
     * BaseActivity might be null if the activity finished.
     */
    @Nullable
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

}
