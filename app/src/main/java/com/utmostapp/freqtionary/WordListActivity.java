package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.util.Log;

/**
 * Created by plee on 11/3/14.
 */
public class WordListActivity extends SingleFragmentActivity
{
    private static final String TAG = "WordListActivity";

    //factory method for parent
    protected Fragment createFragment()
    {
        Log.d(TAG, "create WordListFragment");

        return new WordListFragment();
    }
}