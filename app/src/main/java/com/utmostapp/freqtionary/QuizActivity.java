package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;


public class QuizActivity extends SingleFragmentActivity
{
    private static final String TAG = "QuizActivity";
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    protected Fragment createFragment()
    {
        Log.d(TAG, "createFragment");
        return new QuizFragment();
    }
}
