package com.azoft.injectorlib;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.AbsSavedState;
import android.view.View;

public class InjectorViewSaveState extends AbsSavedState {

    private final Bundle mOurSaveState;

    public InjectorViewSaveState(final Injector injector, final View view, final Parcelable superState) {
        super(superState);

        mOurSaveState = new Bundle();

        injector.applyOnSaveInstanceState(view, mOurSaveState);
    }

    private InjectorViewSaveState(final Parcel source) {
        super(source);

        mOurSaveState = source.readBundle(Bundle.class.getClassLoader());
    }

    public Bundle getOurSaveState() {
        return mOurSaveState;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);

        dest.writeBundle(mOurSaveState);
    }

    public static final Parcelable.Creator<InjectorViewSaveState> CREATOR = new Parcelable.Creator<InjectorViewSaveState>() {

        @Override
        public InjectorViewSaveState createFromParcel(final Parcel source) {
            return new InjectorViewSaveState(source);
        }

        @Override
        public InjectorViewSaveState[] newArray(final int size) {
            return new InjectorViewSaveState[size];
        }
    };
}