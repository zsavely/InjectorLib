package com.azoft.injectorlib.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.azoft.injectorlib.InjectSavedState;
import com.azoft.injectorlib.sample.databinding.ActivityInjectSampleBinding;

import java.security.SecureRandom;
import java.util.Random;

public class InjectSampleActivity extends BaseActivity {

    private final Random mRandom = new SecureRandom();

    private ActivityInjectSampleBinding mBinding;

    @InjectSavedState
    private Double mValue;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_inject_sample);

        setSupportActionBar(mBinding.toolbar);

        if (null != mValue) {
            setDisplayValue(mValue);
        }

        mBinding.fabRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setDisplayValue(mRandom.nextGaussian());
            }
        });

        if (null == getFragmentManager().findFragmentById(R.id.fl_container)) {
            getFragmentManager().beginTransaction().add(R.id.fl_container, new InjectSampleFragment(), null).commit();
        }
    }

    private void setDisplayValue(final double value) {
        mValue = value;
        mBinding.displayView.setText(String.valueOf(mValue));
    }
}