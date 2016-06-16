package com.azoft.injectorlib.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azoft.injectorlib.Injector;

public class BaseActivity extends AppCompatActivity {

    private final Injector mInjector = Injector.init(getClass());

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInjector.applyRestoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mInjector.applyOnSaveInstanceState(this, outState);
    }
}