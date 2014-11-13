package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.util.Log;


public class FlashActivity extends SingleFragmentActivity
{
    private static final String TAG = "FlashActivity";

    //factory method for parent
    protected Fragment createFragment()
    {
        Log.d(TAG, "createFragment");

        return new FlashFragment();
    }
}
