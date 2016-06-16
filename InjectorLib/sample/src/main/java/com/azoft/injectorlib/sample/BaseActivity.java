package com.azoft.injectorlib.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azoft.injectorlib.Injector;

public class BaseActivity extends AppCompatActivity {

    private Injector mInjector;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInjector = Injector.init(this);
        mInjector.applyOnCreate(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mInjector.applyOnSaveInstanceState(this, outState);
    }
}