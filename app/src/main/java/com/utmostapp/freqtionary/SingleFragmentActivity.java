package com.utmostapp.freqtionary;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.MenuItem;

import com.utmostapp.freqtionary.R;

/**
 * Created by plee on 9/19/14.
 * Allows a Fragment to be substituted for an Activity
 */
public abstract class SingleFragmentActivity extends Activity
{
    //factory method for child class
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        FragmentManager fragmentManager = getFragmentManager();

        //fragmentContainer is defined in activity_fragment.xml layout
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        //add a fragment to this activity if it wasn't already
        if(fragment == null)
        {
            //call child factory - abstract method defined above
            fragment = createFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

}
