package com.utmostapp.freqtionary;

import android.app.Fragment;
import android.util.Log;

/**
 * Created by plee on 11/3/14.
 */
public class CardListActivity extends SingleFragmentActivity
{
    private static final String TAG = "CardListActivity";

    //factory method for parent
    protected Fragment createFragment()
    {
        Log.d(TAG, "create CardListFragment");

        return new CardListFragment();
    }
}