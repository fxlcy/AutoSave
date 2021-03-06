package com.recover.autosavesample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.noober.api.NeedSave;
import com.noober.api.RecoverCall;
import com.noober.savehelper.SaveHelper;

/**
 * Created by Administrator on 2017/12/26.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @NeedSave
    int baseActivity;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SaveHelper.getInstance().recover(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SaveHelper.getInstance().save(this, outState);
        super.onSaveInstanceState(outState);
    }

    @RecoverCall
    void recoverCall() {

    }
}
