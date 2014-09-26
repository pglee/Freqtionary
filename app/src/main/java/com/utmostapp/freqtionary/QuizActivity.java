package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.util.Log;


public class QuizActivity extends SingleFragmentActivity
{
    private static final String TAG = "QuizActivity";

    protected Fragment createFragment()
    {
        Log.d(TAG, "createFragment");
        return new QuizFragment();
    }
}
