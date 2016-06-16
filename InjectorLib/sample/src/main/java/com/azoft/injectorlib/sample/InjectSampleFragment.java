package com.azoft.injectorlib.sample;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azoft.injectorlib.InjectSavedState;
import com.azoft.injectorlib.Injector;
import com.azoft.injectorlib.sample.databinding.FragmentInjectSampleBinding;

import java.io.Serializable;

public class InjectSampleFragment extends Fragment {

    private FragmentInjectSampleBinding mBinding;
    private Injector mInjector;

    @InjectSavedState
    private InnerDataClass mData;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInjector = Injector.init(this);

        mInjector.applyOnCreate(this, savedInstanceState);

        if (null == mData) {
            mData = new InnerDataClass();
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_inject_sample, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.displayView.setText(String.valueOf(mData.mValue));

        mBinding.fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mData.mValue += 1;
                mBinding.displayView.setText(String.valueOf(mData.mValue));
            }
        });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        mInjector.applyOnSaveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
    }

    private static class InnerDataClass implements Serializable {

        private static final long serialVersionUID = 1283013520874243839L;

        private int mValue;
    }
}