package com.utmostapp.freqtionary;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class LessonListActivity extends SingleFragmentActivity
{
    private static final String TAG = "LessonListActivity";

    //factory method for parent
    protected android.app.Fragment createFragment()
    {
        Log.d(TAG, "create LessonListActivity");

        return new LessonListFragment();
    }
}
